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

import com.github.rakama.worldtools.data.Biome;
import com.github.rakama.worldtools.data.Block;
import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.io.ChunkManager;

public class BlockCanvas
{    
    protected final ChunkManager manager;
    
    protected BlockCanvas(ChunkManager manager)
    {
        this.manager = manager;
    }
    
    public static BlockCanvas createCanvas(ChunkManager manager)
    {
        return new BlockCanvas(manager);
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

    public void closeAll()
    {
        manager.closeAll();
    }
}