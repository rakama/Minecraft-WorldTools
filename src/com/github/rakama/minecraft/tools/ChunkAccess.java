package com.github.rakama.minecraft.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.world.level.chunk.storage.RegionFile;

import com.github.rakama.minecraft.chunk.Chunk;
import com.github.rakama.util.Coordinate2D;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;

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

public class ChunkAccess
{
    protected int max_region_cache_size = 6;
    protected int max_chunk_cache_size = 34 * 34;

    protected Map<Coordinate2D, File> regions;
    protected LRUCache<Coordinate2D, RegionFile> regionCache;
    protected Map<Coordinate2D, Chunk> chunkCache;

    public ChunkAccess(File ... files)
    {
        init(files);
    }

    protected void init(File ... files)
    {
        if(regionCache != null)
            closeAll();
        
        regions = new TreeMap<Coordinate2D, File>();
        regionCache = new LRUCache<Coordinate2D, RegionFile>(max_region_cache_size);
        chunkCache = new LRUCache<Coordinate2D, Chunk>(max_chunk_cache_size);

        for(File file : files)
            addFile(file);
    }

    protected boolean addFile(File file)
    {
        String index = "(-??[0123456789]*)";
        Pattern pattern = Pattern.compile("r\\." + index + "\\." + index + "\\.mca");

        String name = file.getName();
        Matcher matcher = pattern.matcher(name);

        try
        {
            if(matcher.matches())
            {
                int x = Integer.parseInt(matcher.group(1));
                int z = Integer.parseInt(matcher.group(2));
                Coordinate2D regionCoordinate = new Coordinate2D(x, z);
                regions.put(regionCoordinate, file);
            }
        }
        catch(NumberFormatException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    protected File getFile(Coordinate2D loc)
    {
        return regions.get(loc);
    }

    protected RegionFile fetchRegion(Coordinate2D regionCoordinate)
    {
        if(!regions.containsKey(regionCoordinate))
            return null;

        // check for cached region
        RegionFile cache = regionCache.remove(regionCoordinate);

        // load new region
        if(cache == null)
            cache = new RegionFile(regions.get(regionCoordinate));
        
        regionCache.put(regionCoordinate, cache);
        
        return cache;
    }

    public Chunk readChunk(int x, int z) throws IOException
    {
        // TODO: change chunk cache to not clear chunks that
        // are still referenced from somewhere in process
        
        // check for cached chunk
        Coordinate2D chunkCoordinate = new Coordinate2D(x, z);
        Chunk chunk = chunkCache.remove(chunkCoordinate);

        if(chunk != null)
        {
            chunkCache.put(chunkCoordinate, chunk);
            return chunk;
        }

        // decompress new chunk
        DataInputStream dis = getDataInputStream(x, z);

        if(dis == null)
            return null;

        CompoundTag tag = NbtIo.read(dis);
        chunk = Chunk.loadChunk(tag);
        dis.close();

        // cache this copy
        chunkCache.put(chunkCoordinate, chunk);
        return chunk;
    }

    protected DataInputStream getDataInputStream(int x, int z) throws IOException
    {
        Coordinate2D regionCoordinate = new Coordinate2D(x >> 5, z >> 5);
        RegionFile cache = fetchRegion(regionCoordinate);

        if(cache == null)
            return null;

        return cache.getChunkDataInputStream(x & 0x1F, z & 0x1F);
    }

    public void writeChunk(int x, int z, Chunk chunk) throws IOException
    {
        DataOutputStream dos = getDataOutputStream(x, z);

        if(dos == null)
            throw new IOException();

        NbtIo.write(chunk.getTag(), dos);
        dos.close();
    }

    protected DataOutputStream getDataOutputStream(int x, int z) throws IOException
    {
        Coordinate2D regionCoordinate = new Coordinate2D(x >> 5, z >> 5);
        RegionFile cache = fetchRegion(regionCoordinate);

        if(cache == null)
            throw new IOException();

        return cache.getChunkDataOutputStream(x & 0x1F, z & 0x1F);
    }

    public void closeAll()
    {        
        Iterator<RegionFile> iter = regionCache.values().iterator();

        while(iter.hasNext())
        {
            RegionFile removed = iter.next();

            try
            {
                removed.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            iter.remove();
        }
    }

    public Iterator<Coordinate2D> regionIterator()
    {
        return regions.keySet().iterator();
    }

    public void finalize()
    {
        closeAll();
    }
}

@SuppressWarnings("serial")
class LRUCache<E, K> extends LinkedHashMap<E, K>
{
    int capacity;

    public LRUCache(int capacity)
    {
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Entry<E, K> eldest)
    {
        return size() > capacity;
    }
}