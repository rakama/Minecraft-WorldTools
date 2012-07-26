package com.github.rakama.minecraft.tools;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;

import com.github.rakama.minecraft.chunk.Block;
import com.github.rakama.minecraft.chunk.Chunk;
import com.github.rakama.minecraft.tools.cache.ChunkAccess;
import com.github.rakama.minecraft.tools.cache.RegionInfo;
import com.github.rakama.minecraft.tools.light.ChunkRelighter;
import com.github.rakama.minecraft.tools.loc.Coordinate2D;
import com.github.rakama.util.EnumProfiler;

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

public class WorldTools
{
    /** skip relighting boundary chunks to avoid creating lighting artifacts **/
    protected static boolean relight_skip_boundaries = true;
    
    /** range [0, 4] - higher is faster, but consumes more memory **/
    protected static int relight_batch_scale = 2;
    
    protected EnumProfiler<Mode> profiler;
    protected ChunkAccess access;

    enum Mode {DEFAULT, READ, WRITE, RELIGHT};

    public static void main(String[] args)
    {
        try
        {
            Block.initializeMaterials();
        }
        catch(Exception e)
        {
            System.err.println("WorldTools encountered an error while initializing!");
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println("WorldTools v0.1 <ramsesakama@gmail.com>");

        if(args.length < 1)
        {
            System.err.println("Couldn't load map directory: (none specified)");
            System.exit(0);
        }
        
        WorldTools tools = new WorldTools();

        try
        {
            tools.setMapDirectory(args[0]);
        }
        catch(Exception e)
        {
            System.err.println("Couldn't load map directory: " + args[0]);
            e.printStackTrace();
            System.exit(0);
        }
        
        tools.relight();        
        tools.printRunningTime();

        System.exit(0);
    }

    public WorldTools()
    {
        profiler = new EnumProfiler<Mode>(Mode.DEFAULT);
    }
    
    public void setMapDirectory(String map_dir) throws IOException
    {
        setMapDirectory(new File(map_dir));
    }

    public void setMapDirectory(File map_dir) throws IOException
    {        
        access = ChunkAccess.createInstance(map_dir);
    }

    public void relight()
    {
        if(access == null)
            throw new IllegalStateException("Map directory has not been set.");
        
        int step = 1 << Math.min(4, relight_batch_scale);
        ChunkRelighter relighter = new ChunkRelighter(step + 2);

        Iterator<RegionInfo> regionIterator = access.getRegions().iterator();

        // relight each region
        while(regionIterator.hasNext())
        {
            RegionInfo current = regionIterator.next();
            Coordinate2D coord = current.getRegionCoordinate();
            
            System.out.println("Re-Lighting " + current.getFile().getAbsolutePath());

            int x0 = coord.x << 5;
            int z0 = coord.z << 5;

            // relight each chunk in batches
            for(int z = z0; z < z0 + 32; z += step)
                for(int x = x0; x < x0 + 32; x += step)
                    relight(relighter, x - 1, z - 1);
        }

        access.closeAll();
        
        System.out.println("Finished!");
    }

    protected void relight(ChunkRelighter relighter, int x0, int z0)
    {
        int span = relighter.span;

        profiler.setMode(Mode.READ);
        Chunk[] localChunks = relight_readChunks(x0, z0, span);

        profiler.setMode(Mode.RELIGHT);
        relight_preprocessChunks(localChunks);
        relighter.lightChunks(localChunks);

        profiler.setMode(Mode.WRITE);
        relight_writeChunks(x0, z0, span, localChunks);

        profiler.setMode(Mode.DEFAULT);
    }
    
    protected void relight_preprocessChunks(Chunk[] localChunks)
    {
        for(Chunk chunk : localChunks)
        {
            if(chunk == null)
                continue;
    
            chunk.trimSections();
            chunk.recomputeHeightmap();
        }
    }

    protected Chunk[] relight_readChunks(int x0, int z0, int span)
    {
        Chunk[] localChunks = new Chunk[span * span];

        for(int x = 0; x < span; x++)
            for(int z = 0; z < span; z++)
                localChunks[x + z * span] = readChunk(x + x0, z + z0);

        return localChunks;
    }

    protected void relight_writeChunks(int x0, int z0, int span, Chunk[] localChunks)
    {
        for(int z = 1; z < span - 1; z++)
        {
            for(int x = 1; x < span - 1; x++)
            {
                Chunk chunk = localChunks[x + z * span];

                if(chunk == null)
                    continue;
                
                if(relight_skip_boundaries && relight_isBoundary(x, z, span, localChunks))
                    continue;

                writeChunk(chunk, x + x0, z + z0);
            }
        }
    }

    protected boolean relight_isBoundary(int x, int z, int span, Chunk[] chunks)
    {
        return chunks[x-1 + (z-1) * span] == null || chunks[x + (z-1) * span] == null
            || chunks[x+1 + (z-1) * span] == null || chunks[x-1 + z * span] == null
            || chunks[x+1 + z * span] == null || chunks[x-1 + (z+1) * span] == null
            || chunks[x + (z+1) * span] == null || chunks[x+1 + (z+1) * span] == null;
    }
    
    public Chunk readChunk(int x, int z)
    {
        if(access == null)
            throw new IllegalStateException("Map directory has not been set.");
        
        try
        {
            return access.readChunk(x, z);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public boolean writeChunk(Chunk chunk, int x, int z)
    {
        if(access == null)
            throw new IllegalStateException("Map directory has not been set.");
        
        try
        {
            access.writeChunk(x, z, chunk);
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void printRunningTime()
    {
        System.out.print("Time elapsed: " + profiler.getMilliseconds() + "ms ");
        System.out.print("(read " + profiler.getMilliseconds(Mode.READ) + "ms, ");
        System.out.print("write " + profiler.getMilliseconds(Mode.WRITE) + "ms, ");
        System.out.print("relight " + profiler.getMilliseconds(Mode.RELIGHT) + "ms, ");
        System.out.print("other " + profiler.getMilliseconds(Mode.DEFAULT) + "ms)");
        System.out.println();
    }
}