package com.github.rakama.minecraft.tools.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.world.level.chunk.storage.RegionFile;

import com.github.rakama.minecraft.chunk.Chunk;
import com.github.rakama.minecraft.tools.loc.Coordinate2D;
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
    public static int max_region_cache_size = 6;
    public static int max_chunk_cache_size = 34 * 34;

    protected int regionCacheSize, chunkCacheSize;

    private File regionDirectory;
    private RegionManager regions;
    private ChunkCache cache;

    protected ChunkAccess(int regionCacheSize, int chunkCacheSize)
    {
        this.regionCacheSize = regionCacheSize;
        this.chunkCacheSize = chunkCacheSize;
        
        regions = new RegionManager(regionCacheSize);
        cache = new ChunkCache(chunkCacheSize);
    }

    public static ChunkAccess createInstance(File regionDirectory) throws IOException
    {        
        ChunkAccess access = new ChunkAccess(max_region_cache_size, max_chunk_cache_size);
        access.init(regionDirectory);
        return access;
    }
    
    protected void init(File regionDirectory) throws IOException
    {
        if(regions != null)
            closeAll();

        if(!regionDirectory.exists())
            throw new FileNotFoundException(regionDirectory.getCanonicalPath());
        
        // get parent directory if argument is a file
        if(regionDirectory.isFile())
           regionDirectory = regionDirectory.getParentFile();

        // check for level.dat, to see if we're in the map directory
        File level = new File(regionDirectory.getCanonicalPath() + "/level.dat");
        if(level.exists())
        {
            // try switching to the /region directory
            File newDirectory = new File(regionDirectory.getCanonicalPath() + "/region");
            if(newDirectory.exists())
                regionDirectory = newDirectory;
        }

        this.regionDirectory = regionDirectory;
        
        // check for any .mca files in the current directory
        File[] files = getRegionFiles(regionDirectory);

        for(File file : files)
            addFile(file);
    }

    private static File[] getRegionFiles(File regionDirectory)
    {
        return regionDirectory.listFiles(new FilenameFilter(){
            public boolean accept(File f, String s){
                return s.endsWith(".mca");}});
    }
    
    private boolean addFile(File file)
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
                regions.addFile(file, x, z);
                return true;
            }
        }
        catch(NumberFormatException e)
        {
            return false;
        }

        return false;
    }
    
    public Chunk readChunk(int x, int z) throws IOException
    {
        // check for cached chunk
        Coordinate2D chunkCoordinate = new Coordinate2D(x, z);
        Chunk chunk = cache.remove(chunkCoordinate);

        if(chunk == null)
        {
            // decompress new chunk
            DataInputStream dis = getDataInputStream(x, z);

            if(dis == null)
                return null;

            CompoundTag tag = NbtIo.read(dis);
            chunk = Chunk.loadChunk(tag);
            dis.close();
        }

        // cache this copy
        cache.put(chunkCoordinate, chunk);

        return chunk;
    }

    public void writeChunk(int x, int z, Chunk chunk) throws IOException
    {
        DataOutputStream dos = getDataOutputStream(x, z);

        if(dos == null)
            throw new IOException();

        NbtIo.write(chunk.getTag(), dos);
        dos.close();
    }

    protected DataInputStream getDataInputStream(int x, int z) throws IOException
    {
        Coordinate2D regionCoordinate = new Coordinate2D(x >> 5, z >> 5);
        RegionFile region = regions.getRegionFile(regionCoordinate);

        if(region == null)
            return null;

        return region.getChunkDataInputStream(x & 0x1F, z & 0x1F);
    }

    protected DataOutputStream getDataOutputStream(int x, int z) throws IOException
    {
        Coordinate2D regionCoordinate = new Coordinate2D(x >> 5, z >> 5);
        RegionFile region = regions.getRegionFile(regionCoordinate);

        if(region == null)
            throw new IOException();

        return region.getChunkDataOutputStream(x & 0x1F, z & 0x1F);
    }
    
    public File getRegionDirectory()
    {
        return regionDirectory;
    }        

    public Collection<RegionInfo> getRegions()
    {
        return regions.getRegions();
    }

    public void closeAll()
    {
        regions.closeAll();
    }

    public void finalize()
    {
        closeAll();
    }

    @SuppressWarnings("serial")
    class ChunkCache extends LinkedHashMap<Coordinate2D, Chunk>
    {
        int capacity;

        public ChunkCache(int capacity)
        {
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Entry<Coordinate2D, Chunk> eldest)
        {
            return size() > capacity;
        }
    }
}