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

package com.github.rakama.worldtools.canvas;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.github.rakama.worldtools.data.Biome;
import com.github.rakama.worldtools.data.Block;
import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.data.Entity;
import com.github.rakama.worldtools.data.Schematic;
import com.github.rakama.worldtools.data.TileEntity;
import com.github.rakama.worldtools.io.ChunkManager;

public class WorldCanvas implements BlockCanvas
{    
    protected final ChunkManager manager;
    
    public WorldCanvas(ChunkManager manager)
    {
        this.manager = manager;
    }

    public void setBlock(int x, int y, int z, Block block)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);        
        chunk.setBlock(x & 0xF, y, z & 0xF, block);
    }

    public void setBlock(int x, int y, int z, int id, int data)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);        
        chunk.setBlock(x & 0xF, y, z & 0xF, Block.getBlock(id, data));
    }
    
    public void setBlockID(int x, int y, int z, int id)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);        
        chunk.setBlockID(x & 0xF, y, z & 0xF, id);
    }

    public void setMetaData(int x, int y, int z, int data)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);        
        chunk.setMetaData(x & 0xF, y, z & 0xF, data);
    }
    
    public Block getBlock(int x, int y, int z)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4);

        if(chunk == null)
            return null;
        else
            return chunk.getBlock(x & 0xF, y, z & 0xF);
    }
    
    public int getBlockID(int x, int y, int z)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4);    

        if(chunk == null)
            return -1;
        else
            return chunk.getBlockID(x & 0xF, y, z & 0xF);
    }

    public int getMetaData(int x, int y, int z)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4);
        
        if(chunk == null)
            return -1;
        else
            return chunk.getMetaData(x & 0xF, y, z & 0xF);
    }

    public void setBiome(int x, int z, int biome)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);
        chunk.setBiome(x & 0xF, z & 0xF, biome);
    }

    public void setBiome(int x, int z, Biome biome)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);
        chunk.setBiome(x & 0xF, z & 0xF, biome);
    }
    
    public int getBiome(int x, int z)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4);

        if(chunk == null)
            return -1;
        else
            return chunk.getBiome(x & 0xF, z & 0xF);
    }

    public int getHeight(int x, int z)
    {
        Chunk chunk = manager.getChunk(x >> 4, z >> 4);

        if(chunk == null)
            return -1;
        else
            return chunk.getHeight(x & 0xF, z & 0xF);
    }

    public List<Entity> getEntities(int x0, int y0, int z0, int x1, int y1, int z1)
    {
        List<Entity> list = new LinkedList<Entity>();
        
        for(int x=x0; x<=x1+Chunk.width; x+=Chunk.width)
        {
            for(int z=z0; z<=z1+Chunk.length; z+=Chunk.length)
            {
                Chunk chunk = manager.getChunk(x >> 4, z >> 4);

                if(chunk == null)
                    continue;
                
                for(Entity e : chunk.getEntities())
                    if(e.getX() >= x0 && e.getX() <= x1
                    && e.getY() >= y0 && e.getY() <= y1
                    && e.getZ() >= z0 && e.getZ() <= z1)
                        list.add(e);
            }
        }
        
        return Collections.unmodifiableList(list);
    }

    public List<TileEntity> getTileEntities(int x0, int y0, int z0, int x1, int y1, int z1)
    {
        List<TileEntity> list = new LinkedList<TileEntity>();
        
        for(int x=x0; x<=x1+Chunk.width; x+=Chunk.width)
        {
            for(int z=z0; z<=z1+Chunk.length; z+=Chunk.length)
            {
                Chunk chunk = manager.getChunk(x >> 4, z >> 4);
                
                if(chunk == null)
                    continue;
                
                for(TileEntity e : chunk.getTileEntities())
                    if(e.getX() >= x0 && e.getX() <= x1
                    && e.getY() >= y0 && e.getY() <= y1
                    && e.getZ() >= z0 && e.getZ() <= z1)
                        list.add(e);
            }
        }
        
        return Collections.unmodifiableList(list);
    }
    
    public void addEntity(Entity e)
    {        
        int x = (int)e.getX();
        int z = (int)e.getZ();
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);
        chunk.addEntity(e);
    }
    
    public void addTileEntity(TileEntity e)
    {
        int x = e.getX();
        int z = e.getZ();
        Chunk chunk = manager.getChunk(x >> 4, z >> 4, true);
        chunk.addTileEntity(e);
    }
    
    public boolean removeEntity(Entity e)
    {
        int x = (int)e.getX();
        int z = (int)e.getZ();
        Chunk chunk = manager.getChunk(x >> 4, z >> 4);
        if(chunk == null)
            return false;
        return chunk.removeEntity(e);
    }
    
    public boolean removeTileEntity(TileEntity e)
    {
        int x = e.getX();
        int z = e.getZ();
        Chunk chunk = manager.getChunk(x >> 4, z >> 4);
        if(chunk == null)
            return false;
        return chunk.removeTileEntity(e);
    }
    
    public void importSchematic(int x0, int y0, int z0, Schematic schema)
    {
        int x1 = x0 + schema.getWidth() - 1;
        int y1 = y0 + schema.getHeight() - 1;
        int z1 = z0 + schema.getLength() - 1;
        
        int y0t = Math.max(0, y0);
        int y1t = Math.min(Chunk.height - 1, y1);

        for(int z=z0; z<=z1; z++)
        {
            for(int x=x0; x<=x1; x++)
            {
                for(int y=y0t; y<=y1t; y++)
                {
                    Block block = schema.getBlock(x-x0, y-y0, z-z0);
                    
                    if(block == null)
                        continue;

                    if(y-y0 >= 0 && y-y0 < Chunk.height)
                        setBlock(x, y, z, block);
                }
            }
        }

        for(Entity e : schema.getEntities())
            addEntity(e.clone(x0, y0, z0));

        for(TileEntity e : schema.getTileEntities())
            addTileEntity(e.clone(x0, y0, z0));
    }
    
    public Schematic exportSchematic(int x0, int y0, int z0, int x1, int y1, int z1)
    {
        if(x1 < x0 || y1 < y0 || z1 < z0)
            throw new IllegalArgumentException("Dimensions must be positive");

        int width = x1 - x0 + 1;
        int height = y1 - y0 + 1;
        int length = z1 - z0 + 1;

        Schematic schema = new Schematic(width, height, length);

        int y0t = Math.max(0, y0);
        int y1t = Math.min(Chunk.height - 1, y1);
        
        for(int z=z0; z<=z1; z++)
        {
            for(int x=x0; x<=x1; x++)
            {
                for(int y=y0t; y<=y1t; y++)
                {
                    Block block = getBlock(x, y, z);

                    if(block == null)
                        continue;
                    
                    if(y-y0 >= 0 && y-y0 < Chunk.height)
                        schema.setBlock(x-x0, y-y0, z-z0, block);
                }
            }
        }

        for(Entity e : getEntities(x0, y0, z0, x1, y1, z1))
            schema.addEntity(e.clone(-x0, -y0, -z0));
        
        for(TileEntity e : getTileEntities(x0, y0, z0, x1, y1, z1))
            schema.addTileEntity(e.clone(-x0, -y0, -z0));

        return schema;
    }
}