package com.github.rakama.worldtools.io;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.github.rakama.worldtools.coord.Coordinate2D;
import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.light.ChunkRelighter;

/**
 * Copyright (c) 2012, RamsesA <ramsesakama@gmail.com>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */

public class ChunkManager
{
    protected final static int soft_cache_size = 512;
    protected final static int minimum_cleanup_size = 8;    
    protected final static int default_window_scale = 3;
    protected final boolean debug = false;
    
    private final ChunkAccess access;
    private final TrackedChunk[] window;
    private final ChunkCache cache;
    private final ChunkRelighter relighter;
    private final List<TrackedChunk> cleanup;
    
    private final int windowSize, windowScale, windowMask;
    private int windowMinX, windowMinZ, reads, writes;
    
    public ChunkManager(ChunkAccess access)
    {
        this(access, default_window_scale, soft_cache_size);
    }
        
    protected ChunkManager(ChunkAccess access, int windowScale, int cacheSize)
    {
        this.access = access;
        this.windowScale = windowScale;
        this.windowSize = 1 << windowScale;
        this.windowMask = bitmask(windowScale);
        this.window = new TrackedChunk[windowSize * windowSize];
        this.cache = new ChunkCache(this, cacheSize);
        this.relighter = new ChunkRelighter();
        this.cleanup = new LinkedList<TrackedChunk>();
        
        Runtime.getRuntime().addShutdownHook(new CloseOpenChunks(this));
    }
    
    public Chunk getChunk(int x, int z)
    {
        Chunk chunk = getChunk(x, z, true, false);
        doCleanup(minimum_cleanup_size);
        return chunk;
    }
    
    public Chunk getChunk(int x, int z, boolean create)
    {
        Chunk chunk = getChunk(x, z, true, create);
        doCleanup(minimum_cleanup_size);
        return chunk;
    }
    
    protected TrackedChunk getChunk(int x, int z, boolean moveWindow, boolean create)
    {      
        // try window
        
        int winX = x - windowMinX;
        int winZ = z - windowMinZ;
        int winIndex;

        if(inWindow(winX, winZ))
        {
            winIndex = winX + (winZ << windowScale);       
            TrackedChunk chunk = window[winIndex];       
            if(chunk != null)
                return chunk;
        }
        else if(moveWindow)
        {
            setWindow(x, z);
            winX = x - windowMinX;
            winZ = z - windowMinZ;
            winIndex = winX + (winZ << windowScale);
        }
        else
            winIndex = -1;

        if(debug)
            log("WINDOW_MISS " + x + " " + z);
        
        // try soft cache
        
        ChunkID id = new ChunkID(x, z);
        ChunkReference ref = cache.get(id);
        
        if(ref != null)
        {
            TrackedChunk chunk = ref.get();
            if(chunk != null)
            {
                // place chunk in window
                if(winIndex > -1) 
                    window[winIndex] = chunk;     
                
                return chunk;
            }
            else
                cache.remove(id);
        }

        if(debug)
            log("CACHE_MISS " + x + " " + z);
        
        // try chunk access
        
        TrackedChunk chunk = readChunk(x, z);
        
        if(chunk == null && create)
        {         
            if(debug)
                log("NEW_CHUNK " + x + " " + z);
          
            chunk = new TrackedChunk(x, z, this);
            chunk.invalidateFile();
        }
        
        // place chunk in cache
        cache.put(id, new ChunkReference(chunk));

        // place chunk in window
        if(winIndex > -1) 
            window[winIndex] = chunk;
        
        return chunk;
    }
    
    private void setWindow(int x0, int z0)
    {        
        invalidateLights();
        int offset = windowSize >> 1;
        windowMinX = x0 - offset;
        windowMinZ = z0 - offset;
        Arrays.fill(window, null);
    }
    
    private void invalidateLights()
    {
        for(TrackedChunk chunk : window)
            if(chunk != null && chunk.needsNeighborNotify())
                notifyNeighbors(chunk);
    }
    
    private void notifyNeighbors(TrackedChunk chunk)
    {        
        int x = chunk.getX();
        int z = chunk.getZ();
        
        getChunk(x - 1, z - 1, false, true).invalidateLights();
        getChunk(x, z - 1, false, true).invalidateLights();
        getChunk(x + 1, z - 1, false, true).invalidateLights();        
        getChunk(x - 1, z, false, true).invalidateLights();
        getChunk(x + 1, z, false, true).invalidateLights();
        getChunk(x - 1, z + 1, false, true).invalidateLights();
        getChunk(x, z + 1, false, true).invalidateLights();
        getChunk(x + 1, z + 1, false, true).invalidateLights();
        
        chunk.validateNeighborNotify();
    }
    
    private final boolean inWindow(int x, int z)
    {
        return (x & windowMask) == x && (z & windowMask) == z;
    }

    protected void requestCleanup(TrackedChunk chunk)
    {
        synchronized(cleanup)
        {
            cleanup.add(chunk);
        }
    }
    
    protected boolean doCleanup(int minimumQueueSize)
    {
        if(cleanup.size() < minimumQueueSize)
            return false;
        
        synchronized(cleanup)
        {
            List<TrackedChunk> remove = new ArrayList<TrackedChunk>(cleanup);
            cleanup.clear();
            
            boolean pendingChanges = false;
            
            for(TrackedChunk chunk : remove)
                pendingChanges = flushChanges(chunk);

            return pendingChanges;
        }
    }

    protected boolean flushChanges(TrackedChunk chunk)
    {
        if(debug)
            log("FLUSH_CHANGES " + chunk.getX() + " " + chunk.getZ());
    
        boolean pendingChanges = false;

        if(chunk.needsNeighborNotify())
        {
            notifyNeighbors(chunk);
            pendingChanges = true;
        }
        
        if(chunk.needsRelight())
            relightChunk(chunk);
        
        if(chunk.needsWrite())
            writeChunk(chunk);

        return pendingChanges;
    }
    
    protected void deleteReferences(Chunk chunk)
    {
        cache.remove(new ChunkID(chunk.getX(), chunk.getZ()));
        
        int winX = chunk.getX() - windowMinX;
        int winZ = chunk.getZ() - windowMinZ;
        
        if(inWindow(winX, winZ))
        {
            int index = winX + (winZ << windowScale);       
            window[index] = null;
        }
    }
    
    protected void relightChunk(TrackedChunk chunk)
    {
        int x0 = chunk.getX();
        int z0 = chunk.getZ();
        
        Chunk[] local = new Chunk[9];
        
        local[4] = chunk;
        
        for(int x = 0; x < 3; x++)
        {
            for(int z = 0; z < 3; z++)
            {
                int index = x + z * 3;                
                if(index == 4)
                    continue;
                
                TrackedChunk neighbor = getChunk(x + x0 - 1, z + z0 - 1, false, false);
                local[index] = neighbor;
            }
        }

        relighter.lightChunks(local);
        chunk.validateLights();
    }

    protected synchronized void unloadAll()
    {        
        if(debug)
            log("UNLOADING_CACHE *");

        invalidateLights();
        
        boolean pendingChanges = true;        
        while(pendingChanges)
        {
            pendingChanges = flushCache();
            pendingChanges |= doCleanup(0);
        }

        cache.clear();
        Arrays.fill(window, null);

        if(debug)
            log("CACHE_UNLOADED");
    }
    
    private boolean flushCache()
    {        
        boolean pendingChanges = false;    
        
        Iterator<WeakChunkReference> iter = cache.getWeakIterator();        
        while(iter.hasNext())
        {
            TrackedChunk chunk = iter.next().get();
            if(chunk != null)
                pendingChanges |= flushChanges(chunk);
        }
        
        Iterator<ChunkReference> witer = cache.getSoftIterator();         
        while(witer.hasNext())
        {
            TrackedChunk chunk = witer.next().get();
            if(chunk != null)
                pendingChanges |= flushChanges(chunk);
        }
        
        return pendingChanges;
    }
    
    public void closeAll()
    {
        unloadAll();
        access.closeAll();
    }

    @Override
    protected void finalize() throws Exception
    {
        unloadAll();
    }
    
    private TrackedChunk readChunk(int x, int z)
    {
        try
        {
            TrackedChunk chunk = access.readChunk(x, z, this);
            if(chunk != null)
                reads++;
            return chunk;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private boolean writeChunk(TrackedChunk chunk)
    {
        try
        {
            access.writeChunk(chunk);
            chunk.validateFile();
            writes++;
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private static int bitmask(int bits)
    {
        int mask = 0;

        for(int i = 0; i < bits; i++)
            mask |= 1 << i;

        return mask;
    }

    public Collection<RegionInfo> getRegions()
    {
        return access.getRegions();
    }

    public int getCacheSize()
    {
        return cache.size();
    }
    
    public int getReads()
    {
        return reads;
    }

    public int getWrites()
    {
        return writes;
    }

    protected ChunkAccess getChunkAccess()
    {
        return access;
    }
    
    protected final void log(String str)
    {
        System.out.println(str);
    }
}

final class ChunkID extends Coordinate2D
{
    public ChunkID(int x, int z)
    {
        super(x, z);
    }
}

@SuppressWarnings("serial")
class ChunkCache extends LinkedHashMap<ChunkID, ChunkReference>
{
    ChunkManager manager;
    HashMap<ChunkID, WeakChunkReference> weak;
    
    int capacity;

    public ChunkCache(ChunkManager manager, int capacity)
    {
        this.manager = manager;
        this.capacity = capacity;
        this.weak = new HashMap<ChunkID, WeakChunkReference>();
    }
    
    @Override
    public ChunkReference get(Object key)
    {
        ChunkReference ref = super.get(key);
        
        if(ref == null)
            ref = checkWeakReferences((ChunkID)key);
        
        return ref;
    }

    protected ChunkReference checkWeakReferences(ChunkID key)
    {
        WeakChunkReference wref = weak.remove(key);
        if(wref == null)
            return null;
                        
        TrackedChunk chunk = wref.get();                
        if(chunk == null)
            return null;
        
        // promote to soft reference
        ChunkReference ref = new ChunkReference(chunk);                
        put(key, ref);
        
        return ref;
    }
    
    @Override
    public void clear()
    {
        for(Entry<ChunkID, ChunkReference> entry : entrySet())
        {
            TrackedChunk chunk = entry.getValue().get();                
            if(chunk != null)
                weak.put(entry.getKey(), new WeakChunkReference(chunk));
        }
        
        cleanupWeakReferences();            
        super.clear();
    }
    
    protected void cleanupWeakReferences()
    {
        Iterator<WeakChunkReference> iter = weak.values().iterator();
        while(iter.hasNext())
            if(iter.next().get() == null)
                iter.remove();
    }

    protected Iterator<ChunkReference> getSoftIterator()
    {
        return new ArrayList<ChunkReference>(values()).iterator(); 
    }        

    protected Iterator<WeakChunkReference> getWeakIterator()
    {
        return new ArrayList<WeakChunkReference>(weak.values()).iterator(); 
    }   
    
    @Override
    protected boolean removeEldestEntry(Entry<ChunkID, ChunkReference> eldest)
    {
        if(size() > capacity)
        {
            TrackedChunk chunk = eldest.getValue().get();
            
            if(chunk != null)
            {
                weak.put(eldest.getKey(), new WeakChunkReference(chunk));
                manager.requestCleanup(chunk);
            }
            
            return true;
        }
        
        return false;            
    }
}

final class ChunkReference extends SoftReference<TrackedChunk>
{
    public ChunkReference(TrackedChunk chunk)
    {
        super(chunk);
    }
}

final class WeakChunkReference extends WeakReference<TrackedChunk>
{
    public WeakChunkReference(TrackedChunk chunk)
    {
        super(chunk);
    }
}

final class CloseOpenChunks extends Thread
{
    WeakReference<ChunkManager> ref;
    
    public CloseOpenChunks(ChunkManager manager)
    {
        this.ref = new WeakReference<ChunkManager>(manager);
    }

    public void run()
    {
        ChunkManager manager = ref.get();        
        if(manager == null)
            return;
        
        System.gc();        
        manager.closeAll();
    }    
}