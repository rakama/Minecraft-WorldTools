package com.github.rakama.minecraft.tools.cache;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.world.level.chunk.storage.RegionFile;

import com.github.rakama.minecraft.tools.loc.Coordinate2D;

class RegionManager 
{
    private Map<Coordinate2D, RegionInfo> regions;
    private RegionCache cache;
    private int cacheSize;

    protected RegionManager(int cacheSize)
    {
        this.cacheSize = cacheSize;        
        regions = new TreeMap<Coordinate2D, RegionInfo>();
        cache = new RegionCache(this.cacheSize);
    }

    protected void addFile(File file, int x, int z)
    {
        Coordinate2D regionCoordinate = new Coordinate2D(x, z);
        regions.put(regionCoordinate, new RegionInfo(file, x, z));
    }
    
    public RegionInfo getRegionInfo(Coordinate2D regionCoordinate)
    {
        return regions.get(regionCoordinate);
    }
    
    public RegionFile getRegionFile(Coordinate2D regionCoordinate)
    {
        RegionInfo region = regions.get(regionCoordinate);

        if(region == null)
            return null;
        
        // check for cached region
        if(!region.isCached())
            forceLoad(region);
        else
            forceRefresh(region);        
        
        return region.getCached();
    }
    
    public Collection<RegionInfo> getRegions()
    {
        return regions.values();
    }

    protected Collection<RegionInfo> getCachedRegions()
    {
        return cache.values();
    }
    
    protected void forceLoad(RegionInfo info)
    {
        if(!info.isCached())
            info.loadCached();
            
        cache.put(info.getRegionCoordinate(), info);
    }

    protected void forceUnload(RegionInfo info)
    {         
        if(info != null)
            deleteCached(info);   
        
        cache.remove(info.getRegionCoordinate());  
    }
    
    protected void forceRefresh(RegionInfo info)
    {
        cache.remove(info.getRegionCoordinate());         
        cache.put(info.getRegionCoordinate(), info);
    }

    protected void deleteCache()
    {
        for(RegionInfo info : cache.values())
            deleteCached(info);
        
        cache.clear();
    }
        
    private void deleteCached(RegionInfo info)
    {
        if(!info.isCached())
            return;
        
        try
        {
            info.deleteCached();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }        
    }

    public void closeAll()
    {
        deleteCache();
    }
    
    public void finalize()
    {
        deleteCache();
    }
    
    @SuppressWarnings("serial")
    class RegionCache extends LinkedHashMap<Coordinate2D, RegionInfo>
    {
        int capacity;

        public RegionCache(int capacity)
        {
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Entry<Coordinate2D, RegionInfo> eldest)
        {
            if(size() > capacity)
                deleteCached(eldest.getValue());
            
            return false;
        }
    }
}