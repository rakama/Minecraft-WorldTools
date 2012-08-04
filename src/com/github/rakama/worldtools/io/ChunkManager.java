package com.github.rakama.worldtools.io;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.light.ChunkRelighter;
import com.github.rakama.worldtools.util.PriorityCache;

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
    protected final static int default_max_cache_size = 2048;
    protected final static int default_window_scale = 2;
    protected final static int minimum_cleanup_size = 32;
    protected final static int priority_access = 10000;
    protected final static int priority_light = 5000;
    protected final static int priority_read = 100;
    protected final boolean debug = false;
    
    private final ChunkAccess access;
    private final TrackedChunk[] window;
    private final ChunkCache cache;
    private final ChunkRelighter relighter;
    private final List<TrackedChunk> cleanup;
    
    private final int windowSize, windowScale, windowMask;
    private int windowMinX, windowMinZ, reads, writes;
    private boolean lightingEnabled;
    private Thread shutdownHook;
    
    public ChunkManager(ChunkAccess access)
    {
        this(access, default_window_scale, default_max_cache_size);
    }
        
    protected ChunkManager(ChunkAccess access, int windowScale, int cacheSize)
    {
        this.access = access;
        this.windowScale = windowScale;
        this.windowSize = 1 << windowScale;
        this.windowMask = bitmask(windowScale);
        this.window = new TrackedChunk[windowSize * windowSize];
        this.cache = new ChunkCache(cacheSize);
        this.relighter = new ChunkRelighter();
        this.cleanup = new LinkedList<TrackedChunk>();
        this.lightingEnabled = true;
        
        shutdownHook = new CloseOpenChunks(this);
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public void setLightingEnabled(boolean enabled)
    {
        lightingEnabled = enabled;
    }
    
    public boolean isLightingEnabled()
    {
        return lightingEnabled;
    }
    
    public Chunk getChunk(int x, int z)
    {
        Chunk chunk = getChunk(x, z, priority_access, true, false);
        doCleanup(minimum_cleanup_size);
        return chunk;
    }
    
    public Chunk getChunk(int x, int z, boolean create)
    {
        Chunk chunk = getChunk(x, z, priority_access, true, create);
        doCleanup(minimum_cleanup_size);
        return chunk;
    }
    
    protected TrackedChunk getChunk(int x, int z, int priority, boolean moveWindow, boolean create)
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

        cache.decay(1);        
        TrackedChunk chunk = cache.get(x, z, priority);
        
        if(chunk != null)
        {
            // place chunk in window
            if(winIndex > -1) 
                window[winIndex] = chunk;     
            
            return chunk;
        }

        if(debug)
            log("CACHE_MISS " + x + " " + z);
        
        // try chunk access
        
        chunk = readChunk(x, z);
        
        if(chunk == null)
        {    
            if(!create)
                return null;     
            
            if(debug)
                log("NEW_CHUNK " + x + " " + z);
          
            chunk = new TrackedChunk(x, z, this);
            chunk.invalidateFile();
        }
        
        // place chunk in cache
        cache.put(chunk, priority);

        // place chunk in window
        if(winIndex > -1) 
            window[winIndex] = chunk;
        
        return chunk;
    }
    
    private void setWindow(int x0, int z0)
    {        
        if(lightingEnabled)
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
        
        notifyIfExists(getChunk(x - 1, z - 1, priority_light, false, false));
        notifyIfExists(getChunk(x, z - 1, priority_light, false, false));
        notifyIfExists(getChunk(x + 1, z - 1, priority_light, false, false));
        notifyIfExists(getChunk(x - 1, z, priority_light, false, false));
        notifyIfExists(getChunk(x + 1, z, priority_light, false, false));
        notifyIfExists(getChunk(x - 1, z + 1, priority_light, false, false));
        notifyIfExists(getChunk(x, z + 1, priority_light, false, false));
        notifyIfExists(getChunk(x + 1, z + 1, priority_light, false, false));
        
        cache.refresh(chunk, priority_light);
        chunk.validateNeighborNotify();
    }
    
    private void notifyIfExists(TrackedChunk chunk)
    {
        if(chunk != null)
            chunk.invalidateLights();
    }
    
    private final boolean inWindow(int x, int z)
    {
        return (x & windowMask) == x && (z & windowMask) == z;
    }

    protected void requestCleanup(TrackedChunk chunk)
    {
        if(chunk == null)
            return;
        
        synchronized(cleanup)
        {
            cleanup.add(chunk);
        }
    }
    
    protected void doCleanup(int minimumQueueSize)
    {
        if(cleanup.size() < minimumQueueSize)
            return;

        synchronized(cleanup)
        {
            List<TrackedChunk> remove = new ArrayList<TrackedChunk>(cleanup);
            cleanup.clear();
            
            for(TrackedChunk chunk : remove)
                flushChanges(chunk);
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
        
        if(chunk.needsRelight() && lightingEnabled)
            relightChunk(chunk);
        
        if(chunk.needsWrite())
            writeChunk(chunk);

        return pendingChanges;
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
                
                local[index] = getChunk(x + x0 - 1, z + z0 - 1, priority_read, false, false);
            }
        }

        relighter.lightChunks(local);
        chunk.validateLights();
    }
    
    public void closeAll()
    {
        unloadAll();
        access.closeAll();
    }

    protected synchronized void unloadAll()
    {        
        if(debug)
            log("UNLOADING_CACHE *");

        invalidateLights();
            
        while(!cache.isEmpty())
        {
            cache.clear();
            flushWeakReferences();
            doCleanup(0);
        }

        Arrays.fill(window, null);

        if(debug)
            log("CACHE_UNLOADED");
    }
    
    private void flushWeakReferences()
    {        
        List<TrackedChunk> flush = new ArrayList<TrackedChunk>();
        
        for(WeakReference<TrackedChunk> ref : cache.getWeakReferences())
        {
            TrackedChunk chunk = ref.get();
            if(chunk != null)
                flush.add(chunk);
        }
        
        for(TrackedChunk chunk : flush)
            flushChanges(chunk);
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

    @Override
    protected void finalize() throws Exception
    {
        unloadAll();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
    
    final class ChunkCache extends PriorityCache<ChunkID, TrackedChunk>
    {            
        public ChunkCache(int maxCapacity)
        {
            super(maxCapacity);
        }

        public TrackedChunk get(int x, int z, int priority)
        {
            return super.get(new ChunkID(x, z), priority);
        }

        public void put(TrackedChunk chunk, int priority)
        {
            super.put(chunk.getID(), chunk, priority);
        }
            
        public void refresh(TrackedChunk chunk, int priority)
        {
            super.refresh(chunk.getID(), priority);
        }
        
        protected void expired(ChunkID key, TrackedChunk value)
        {
            if(value.isDirty())
                requestCleanup(value);
        }        
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