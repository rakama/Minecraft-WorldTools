package com.github.rakama.worldtools.io;

import com.github.rakama.worldtools.data.Block;
import com.github.rakama.worldtools.data.Chunk;
import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.IntArrayTag;
import com.mojang.nbt.IntTag;
import com.mojang.nbt.ListTag;

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

class TrackedChunk extends Chunk
{
    private ChunkManager manager;
    private boolean dirtyBlocks, dirty;
    
    public TrackedChunk(int x, int z, int[] heightmap, byte[] biomes, ChunkManager manager)
    {
        super(x, z, heightmap, biomes);
        this.manager = manager;
        this.dirtyBlocks = false;
        this.dirty = false;
    }
    
    public boolean hasDirtyBlocks()
    {
        return dirtyBlocks;
    }

    public boolean isDirty()
    {
        return dirty;
    }
    
    public void setDirtyBlocks(boolean dirty)
    {
        this.dirtyBlocks = dirty;
        this.dirty |= dirty;
    }

    public void setDirty(boolean dirty)
    {
        this.dirty = dirty;
        this.dirtyBlocks &= dirty;
    }
    
    public boolean isOrphaned()
    {
        return manager == null;
    }

    @Override
    public void setHeight(int x, int z, int val)
    {
        setDirty(true);
        super.setHeight(x, z, val);
    }

    @Override
    public void setBiome(int x, int z, int val)
    {
        setDirty(true);
        super.setBiome(x, z, val);
    }

    @Override
    public void setBlock(int x, int y, int z, Block block)
    {
        setDirtyBlocks(true);
        super.setBlock(x, y, z, block);
    }

    @Override
    public void setBlockID(int x, int y, int z, int val)
    {
        setDirtyBlocks(true);
        super.setBlockID(x, y, z, val);
    }

    @Override
    public void setMetaData(int x, int y, int z, int val)
    {
        setDirtyBlocks(true);
        super.setMetaData(x, y, z, val);
    }

    @Override
    public void setBlockLight(int x, int y, int z, int val)
    {
        setDirty(true);
        super.setBlockLight(x, y, z, val);
    }

    @Override
    public void setSkyLight(int x, int y, int z, int val)
    {
        setDirty(true);
        super.setSkyLight(x, y, z, val);
    }

    public synchronized void flushChanges()
    {
        if(manager == null)
            return;
        
        if(dirtyBlocks || dirty)
        {
            manager.relight(this);
            manager.writeChunk(this);
        }

        dirtyBlocks = false;
        dirty = false;
    }
    
    protected void close()
    {
        flushChanges();
        manager = null;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        if(manager == null)
            return;
        
        flushChanges();
        manager.unloadCache(this);
        manager = null;
    }    
    
    public static TrackedChunk loadChunk(CompoundTag tag, ChunkManager manager)
    {
        CompoundTag level = (CompoundTag) tag.get("Level");

        @SuppressWarnings("unchecked")
        ListTag<CompoundTag> sections = (ListTag<CompoundTag>) level.get("Sections");
        IntArrayTag heightmap = (IntArrayTag) level.get("HeightMap");
        ByteArrayTag biome = (ByteArrayTag) level.get("Biomes");
        IntTag xPos = (IntTag) level.get("xPos");
        IntTag zPos = (IntTag) level.get("zPos");

        TrackedChunk chunk = new TrackedChunk(xPos.data, zPos.data, heightmap.data, biome.data, manager);
        chunk.loadSections(sections);
        chunk.tag = tag;

        return chunk;
    }
}