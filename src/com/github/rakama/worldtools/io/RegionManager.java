package com.github.rakama.worldtools.io;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import net.minecraft.world.level.chunk.storage.RegionFile;

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

class RegionManager 
{
    protected final static int region_cache_size = 12;
    protected final boolean debug = false;

    private Map<RegionID, RegionInfo> regions;
    private Map<RegionID, RegionInfo> cache;

    protected RegionManager()
    {
        this(region_cache_size);
    }

    protected RegionManager(int cacheSize)
    {
        regions = new TreeMap<RegionID, RegionInfo>();
        cache = new RegionCache(cacheSize);
    }

    protected void addFile(File file, int x, int z)
    {
        regions.put(new RegionID(x, z), new RegionInfo(file, x, z));
    }
    
    public RegionInfo getRegionInfo(int x, int z)
    {
        return regions.get(new RegionID(x, z));
    }
    
    public RegionFile getRegionFile(int x, int z)
    {
        RegionInfo region = regions.get(new RegionID(x, z));

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
            load(info);
            
        cache.put(info.getID(), info);
    }

    protected void forceUnload(RegionInfo info)
    {         
        if(info != null)
            unload(info);   
        
        cache.remove(info.getID());  
    }
    
    protected void forceRefresh(RegionInfo info)
    {
        cache.remove(info.getID());         
        cache.put(info.getID(), info);
    }
    
    @SuppressWarnings("unused")
    private void load(RegionInfo info)
    {
        if(debug && info.getFile().exists())
            log("LOAD_REGION " + info.getRegionCoordinate().x + " " + info.getRegionCoordinate().z);
        else if(debug && !info.getFile().exists())
            log("NEW_REGION " + info.getRegionCoordinate().x + " " + info.getRegionCoordinate().z);
        
        info.setCached(new RegionFile(info.getFile()));
    }
    
    private void unload(RegionInfo info)
    {
        if(!info.isCached())
            return;

        if(debug)
            log("UNLOAD_REGION " + info.getRegionCoordinate().x + " " + info.getRegionCoordinate().z);
        
        try
        {
            info.getCached().close();
            info.setCached(null);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }        
    }

    protected void unloadCache()
    {
        if(debug)
            log("UNLOAD_REGION *");
        
        synchronized(cache)
        {
            for(RegionInfo info : cache.values())
                unload(info);
        }
        
        cache.clear();
    }

    public void closeAll()
    {
        unloadCache();
    }

    @Override
    protected void finalize()
    {
        unloadCache();
    }
    
    @SuppressWarnings("serial")
    protected class RegionCache extends LinkedHashMap<RegionID, RegionInfo>
    {
        int capacity;

        public RegionCache(int capacity)
        {
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Entry<RegionID, RegionInfo> eldest)
        {
            if(size() > capacity)
            {
                unload(eldest.getValue());
                remove(eldest.getKey());
            }
            
            return false;
        }
    }

    protected final void log(String str)
    {
        System.out.println(str);
    }
}