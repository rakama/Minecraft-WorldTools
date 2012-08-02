package com.github.rakama.worldtools.io;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    protected final static int default_window_scale = 2;
    protected final boolean debug = false;
    
    private final ChunkAccess access;
    private final TrackedChunk[] window;
    private final Map<ChunkID, ChunkReference> cache; // TODO: turn old refs into weakreferences
    private final ChunkRelighter relighter;
    
    private final int windowSize, windowScale, windowMask;
    private int windowMinX, windowMinZ;
    
    public ChunkManager(ChunkAccess access)
    {
        this(access, default_window_scale);
    }
        
    protected ChunkManager(ChunkAccess access, int windowScale)
    {
        this.access = access;
        this.windowScale = windowScale;
        this.windowSize = 1 << windowScale;
        this.windowMask = bitmask(windowScale);
        this.window = new TrackedChunk[windowSize * windowSize];
        this.cache = Collections.synchronizedMap(new HashMap<ChunkID, ChunkReference>());
        this.relighter = new ChunkRelighter();
        
        Runtime.getRuntime().addShutdownHook(new CloseOpenChunks(this));
    }
    
    public Chunk getChunk(int x, int z)
    {
        return getChunk(x, z, true, false);
    }
    
    public Chunk getChunk(int x, int z, boolean create)
    {
        TrackedChunk chunk = getChunk(x, z, true, create);
        
        if(chunk == null)
            return null;
        
        // TODO: don't set to dirty unless it's actually creating a chunk
        if(create)
            chunk.invalidateBlocks();
        
        return chunk;
    }
    
    protected synchronized TrackedChunk getChunk(int x, int z, boolean moveWindow, boolean create)
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
                if(winIndex > -1) 
                    window[winIndex] = chunk;
                return chunk;
            }
            else
                cache.remove(id);
        }

        if(debug)
            log("CACHE_MISS " + x + " " + z);
        
        // load from chunk access
        
        TrackedChunk chunk = readChunk(x, z);
        cache.put(id, new ChunkReference(chunk));
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
        
        getChunk(x - 1, z - 1, false, false).invalidateLights();
        getChunk(x, z - 1, false, false).invalidateLights();
        getChunk(x + 1, z - 1, false, false).invalidateLights();        
        getChunk(x - 1, z, false, false).invalidateLights();
        getChunk(x + 1, z, false, false).invalidateLights();
        getChunk(x - 1, z + 1, false, false).invalidateLights();
        getChunk(x, z + 1, false, false).invalidateLights();
        getChunk(x + 1, z + 1, false, false).invalidateLights();
        
        chunk.validateNeighborNotify();
    }
    
    private final boolean inWindow(int x, int z)
    {
        return (x & windowMask) == x && (z & windowMask) == z;
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

    protected ChunkAccess getChunkAccess()
    {
        return access;
    }

    protected synchronized boolean hasDirtyChunks()
    {
        for(ChunkReference ref : cache.values())
        {
            TrackedChunk chunk = ref.get();
            
            if(chunk == null)
                continue;
            
            if(chunk.isDirty())
                return true;
        }
        
        return false;
    }

    protected synchronized boolean flushChanges(TrackedChunk chunk)
    {
        if(debug)
            log("FLUSH_CHANGES " + chunk.getX() + " " + chunk.getZ());
    
        boolean modified = false;

        if(chunk.needsNeighborNotify())
        {
            notifyNeighbors(chunk);
            modified = true;
        }
        
        if(chunk.needsRelight())
        {
            relightChunk(chunk);
            modified = true;
        }
        
        if(chunk.needsWrite())
        {
            writeChunk(chunk);
            modified = true;
        }

        return modified;
    }
    
    protected synchronized void deleteReferences(Chunk chunk)
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
    
    protected synchronized void relightChunk(TrackedChunk chunk)
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
        Collection<ChunkReference> values = cache.values();
        
        boolean dirty = true;
        
        while(dirty)
        {
            Iterator<ChunkReference> iter = new ArrayList<ChunkReference>(values).iterator();            
            dirty = false;
            
            while(iter.hasNext())
            {
                ChunkReference ref = iter.next();
                TrackedChunk chunk = ref.get();
                if(chunk != null)
                    dirty |= flushChanges(chunk);
             }
        }        

        cache.clear();
        Arrays.fill(window, null);

        if(debug)
            log("CACHE_UNLOADED");
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
    
    protected TrackedChunk readChunk(int x, int z)
    {
        try
        {
            return access.readTrackedChunk(x, z, this);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    protected boolean writeChunk(TrackedChunk chunk)
    {
        try
        {
            access.writeChunk(chunk);
            chunk.validateFile();
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
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

final class ChunkReference extends SoftReference<TrackedChunk>
{
    public ChunkReference(TrackedChunk chunk)
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
        
        manager.closeAll();
    }    
}