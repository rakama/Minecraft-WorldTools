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

package com.github.rakama.worldtools.light;

import java.util.Arrays;

import com.github.rakama.worldtools.data.Block;
import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.data.Section;
import com.github.rakama.worldtools.util.NibbleArray;

final class ChunkProtector extends Chunk
{    
    NibbleArray[] tempLights;
    
    public ChunkProtector()
    {
        super(-1, -1);
        
        tempLights = new NibbleArray[num_sections];
        for(int i=0; i<num_sections; i++)
            tempLights[i] = new NibbleArray(Section.volume);
    }
    
    public void assign(Chunk chunk)
    {
        if(chunk == null)
            throw new NullPointerException();
        
        x = chunk.getX();
        z = chunk.getZ();
        
        for(int i=0; i<num_sections; i++)
            sections[i] = cloneSection(i, chunk.getSection(i));
    }

    private Section cloneSection(int y, Section sec)
    {
        if(sec == null)
            return null;
        
        return new Section(y, sec.getBlockIDs(), sec.getMetaData(), 
                tempLights[y], tempLights[y]);
    }
    
    public void clear()
    {
        x = -1;
        z = -1;
        Arrays.fill(sections, null);
    }
    
    @Override
    public void setBiome(int x, int z, int val)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlock(int x, int y, int z, Block block)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlockID(int x, int y, int z, int val)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMetaData(int x, int y, int z, int val)
    {
        throw new UnsupportedOperationException();
    }
}