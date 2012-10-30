/*
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

package com.github.rakama.worldtools.io;

import com.github.rakama.worldtools.coord.Coordinate2D;
import com.github.rakama.worldtools.data.Block;
import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.data.Entity;
import com.github.rakama.worldtools.data.TileEntity;
import com.github.rakama.worldtools.data.entity.EntityFactory;
import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.IntArrayTag;
import com.mojang.nbt.IntTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.Tag;

class ManagedChunk extends Chunk
{
    private ChunkManager manager;
    private ChunkID id;
    private boolean needsWrite, needsRelight, needsNeighborNotify;

    public ManagedChunk(int x, int z, ChunkManager manager)
    {
        super(x, z);
        this.manager = manager;
        this.needsWrite = false;
        this.needsRelight = false;
        this.needsNeighborNotify = false;
        this.id = new ChunkID(x, z);
    }
    
    public ManagedChunk(int x, int z, int[] heightmap, byte[] biomes, ChunkManager manager)
    {
        super(x, z, heightmap, biomes);
        this.manager = manager;
        this.needsWrite = false;
        this.needsRelight = false;
        this.needsNeighborNotify = false;
        this.id = new ChunkID(x, z);
    }

    public boolean isDirty()
    {
        return needsNeighborNotify || needsRelight || needsWrite;
    }
    
    public boolean needsNeighborNotify()
    {
        return needsNeighborNotify;
    }
    
    public boolean needsRelight()
    {
        return needsRelight;
    }

    public boolean needsWrite()
    {
        return needsWrite;
    }

    public void validateNeighborNotify()
    {
        this.needsNeighborNotify = false;
    }

    public void validateLights()
    {
        this.needsRelight = false;
    }
    
    public void validateFile()
    {
        this.needsWrite = false;
    }
    
    public void invalidateBlocks()
    {
        this.needsWrite = true;
        this.needsRelight = true;
        this.needsNeighborNotify = true;
    }

    public void invalidateLights()
    {
        this.needsWrite = true;
        this.needsRelight = true;
    }
    
    public void invalidateFile()
    {
        this.needsWrite = true;
    }

    @Override
    public void setHeight(int x, int z, int val)
    {
        invalidateFile();
        super.setHeight(x, z, val);
    }

    @Override
    public void setBiome(int x, int z, int val)
    {
        invalidateFile();
        super.setBiome(x, z, val);
    }

    @Override
    public void setBlock(int x, int y, int z, Block block)
    {
        invalidateBlocks();
        super.setBlock(x, y, z, block);
    }

    @Override
    public void setBlockID(int x, int y, int z, int val)
    {
        invalidateBlocks();
        super.setBlockID(x, y, z, val);
    }

    @Override
    public void setMetaData(int x, int y, int z, int val)
    {
        invalidateBlocks();
        super.setMetaData(x, y, z, val);
    }

    @Override
    public void setBlockLight(int x, int y, int z, int val)
    {
        invalidateLights();
        super.setBlockLight(x, y, z, val);
    }
    
    @Override
    public void setSkyLight(int x, int y, int z, int val)
    {
        invalidateLights();
        super.setSkyLight(x, y, z, val);
    }
    
    @Override
    public void clearBlockLights()
    {
        invalidateLights();
        super.clearBlockLights();
    }

    @Override
    public void clearSkyLights()
    {
        invalidateLights();
        super.clearSkyLights();
    }

    @Override
    public void addEntity(Entity e)
    {
        invalidateFile();
    	super.addEntity(e);
    }

    @Override
    public void addTileEntity(TileEntity e)
    {
        invalidateFile();
    	super.addTileEntity(e);
    }

    @Override
    public boolean removeEntity(Entity e)
    {
        invalidateFile();
    	return super.removeEntity(e);
    }

    @Override
    public boolean removeTileEntity(TileEntity e)
    {
        invalidateFile();
    	return super.removeTileEntity(e);
    }

    protected ChunkID getID()
    {
        return id;
    }
    
    @Override
    protected void finalize() throws Throwable
    {
        if(isDirty())
            manager.requestCleanup(this);
    }
    
    @SuppressWarnings("unchecked")
	public static ManagedChunk loadChunk(CompoundTag tag, ChunkManager manager)
    {
        CompoundTag level = (CompoundTag) tag.get("Level");

        ListTag<CompoundTag> sections = (ListTag<CompoundTag>) level.get("Sections");
        IntArrayTag heightmap = (IntArrayTag) level.get("HeightMap");
        ByteArrayTag biome = (ByteArrayTag) level.get("Biomes");
        IntTag xPos = (IntTag) level.get("xPos");
        IntTag zPos = (IntTag) level.get("zPos");

        ManagedChunk chunk;
        chunk = new ManagedChunk(xPos.data, zPos.data, heightmap.data, biome.data, manager);
        // TODO: create ManagedSection to catch Section changes
        chunk.loadSections(sections);

        Tag tagEntities = level.get("Entities");   
        if(tagEntities != null)
        {
        	ListTag<CompoundTag> list = (ListTag<CompoundTag>)tagEntities;        	
        	for(int i=0; i<list.size(); i++)
        		chunk.entities.add(EntityFactory.getEntity(list.get(i)));
        }
        
        Tag tagTileEntities = level.get("TileEntities");
        if(tagEntities != null)
        {
        	ListTag<CompoundTag> list = (ListTag<CompoundTag>)tagTileEntities;        	
        	for(int i=0; i<list.size(); i++)
        		chunk.tileEntities.add(EntityFactory.getTileEntity(list.get(i)));
        }
        
        chunk.tag = tag;
        return chunk;
    }
}

final class ChunkID extends Coordinate2D
{    
    public ChunkID(int x, int z)
    {
        super(x, z);
    }
}