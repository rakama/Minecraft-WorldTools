package com.github.rakama.minecraft.tools.cache;

import java.io.File;
import java.io.IOException;

import net.minecraft.world.level.chunk.storage.RegionFile;

import com.github.rakama.minecraft.tools.loc.BoundingBox;
import com.github.rakama.minecraft.tools.loc.Coordinate2D;

public class RegionInfo
{
    private final File file;
    private final Coordinate2D coordinate;
    private final BoundingBox box;
    private RegionFile cached;
    
    protected RegionInfo(File file, int x, int z)
    {
        this.file = file;
        this.coordinate = new Coordinate2D(x, z);

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

    public Coordinate2D getRegionCoordinate()
    {
        return coordinate;
    }
    
    public BoundingBox getBoundingBox()
    {
        return box;
    }

    protected void loadCached()
    {
        cached = new RegionFile(file);
    }

    protected void deleteCached() throws IOException
    {
        cached.close();
        cached = null;
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