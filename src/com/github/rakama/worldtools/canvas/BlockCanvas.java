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

public interface BlockCanvas
{    
    public void setBlock(int x, int y, int z, Block block);
    public void setBlock(int x, int y, int z, int id, int data);    
    public void setBlockID(int x, int y, int z, int id);
    public void setMetaData(int x, int y, int z, int data);    
    public Block getBlock(int x, int y, int z);    
    public int getBlockID(int x, int y, int z);
    public int getMetaData(int x, int y, int z);
    public void setBiome(int x, int z, int biome);
    public void setBiome(int x, int z, Biome biome);    
    public int getBiome(int x, int z);
    public int getHeight(int x, int z);
}