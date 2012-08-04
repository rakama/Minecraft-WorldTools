package com.github.rakama.worldtools.io;

import java.io.File;

import net.minecraft.world.level.chunk.storage.RegionFile;

import com.github.rakama.worldtools.coord.BoundingBox;
import com.github.rakama.worldtools.coord.Coordinate2D;

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

public class RegionInfo
{
    private final File file;
    private final RegionID id;
    private final BoundingBox box;
    private RegionFile cached;
    
    protected RegionInfo(File file, int x, int z)
    {
        this.file = file;
        this.id = new RegionID(x, z);

        int minx = x << 9;
        int minz = z << 9;
        int maxx = minx + 511;
        int maxz = minz + 511;
        int miny = 0;
        int maxy = 255;
        
        this.box = new BoundingBox(minx, miny, minz, maxx, maxy, maxz);
    }
    
    public File getFile()
    {
        return file;
    }

    protected RegionID getID()
    {
        return id;
    }
    
    public Coordinate2D getRegionCoordinate()
    {
        return (Coordinate2D)id;
    }
    
    public BoundingBox getBoundingBox()
    {
        return box;
    }

    protected void setCached(RegionFile cached)
    {
        this.cached = cached;
    }
    
    protected RegionFile getCached()
    {
        return cached;
    }
    
    public boolean isCached()
    {
        return cached != null;
    }
}