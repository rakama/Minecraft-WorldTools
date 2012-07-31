package com.github.rakama.worldtools.light;

import java.util.Arrays;

import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.data.Section;
import com.github.rakama.worldtools.util.NibbleArray;

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

final class LightWrapper extends Chunk
{    
    NibbleArray[] light;
    
    public LightWrapper()
    {
        super(-1, -1, null, null);
    
        light = new NibbleArray[num_sections];
        
        for(int i=0; i<num_sections; i++)
            light[i] = new NibbleArray(Section.volume);
    }
    
    public void assign(Chunk chunk)
    {
        if(chunk == null)
            throw new NullPointerException();
        
        x = chunk.getX();
        z = chunk.getX();
        heightmap = chunk.getHeightmap();
        biomes = chunk.getBiomes();
        
        for(int i=0; i<num_sections; i++)
        {
            Section sec = chunk.getSection(i);
            
            if(sec == null)
            {
                sections[i] = null;
                continue;
            }
            
            sections[i] = new Section(i, sec.getBlockIDs(), 
                    sec.getMetaData(), light[i], light[i]);
        }
    }
    
    @Override
    protected Section getContainingSection(int y, boolean create)
    {
        int index = y >> 4;

        if(index < 0 || index >= num_sections)
            return null;

        synchronized (sections)
        {        
            Section sec = sections[index];
            
            if(create && sec == null)
            {
                sec = new Section(y >> 4);
                sections[index] = sec;
            }
            
            return sec;
        }
    }
    
    public void clear()
    {
        x = -1;
        z = -1;
        heightmap = null;
        biomes = null;
        Arrays.fill(sections, null);
    }
}