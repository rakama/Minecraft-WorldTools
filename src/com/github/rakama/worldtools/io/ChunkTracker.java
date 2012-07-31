package com.github.rakama.worldtools.io;

import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.io.ChunkManager;

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

class ChunkTracker
{
    private final Chunk chunk;
    private final ChunkManager manager;
    private boolean dirtyBlocks, dirtyLights;
    
    public ChunkTracker(Chunk chunk, ChunkManager manager)
    {
        this.chunk = chunk;
        this.manager = manager;
        this.dirtyBlocks = false;
        this.dirtyLights = false;
    }
    
    public Chunk getChunk()
    {
        return chunk;
    }
        
    public boolean hasDirtyBlocks()
    {
        return dirtyBlocks;
    }

    public boolean hasDirtyLights()
    {
        return dirtyLights;
    }
    
    public void setDirtyBlocks(boolean dirty)
    {
        this.dirtyBlocks = dirty;
    }

    public void setDirtyLights(boolean dirty)
    {
        this.dirtyLights = dirty;
    }

    public synchronized void flushChanges()
    {        
        if(dirtyBlocks || dirtyLights)
        {
            manager.relight(chunk);
            manager.writeChunk(chunk);
        }

        dirtyBlocks = false;
        dirtyLights = false;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        flushChanges();
        manager.unloadCache(chunk);
    }
}