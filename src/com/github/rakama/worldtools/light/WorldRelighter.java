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

import java.io.IOException;
import java.util.Iterator;

import com.github.rakama.worldtools.coord.Coordinate2D;
import com.github.rakama.worldtools.data.Chunk;
import com.github.rakama.worldtools.io.ChunkAccess;
import com.github.rakama.worldtools.io.RegionInfo;
import com.github.rakama.worldtools.util.EnumProfiler;

public class WorldRelighter
{
    /** skip relighting boundary chunks to avoid creating lighting artifacts **/
    protected static boolean relight_skip_boundaries = true;
    
    /** range [0, 4] - higher is faster, but consumes more memory **/
    protected static int relight_batch_scale = 3;
    
    protected EnumProfiler<Mode> profiler;
    protected ChunkAccess access;
    protected boolean verbose;

    protected enum Mode {DEFAULT, READ, WRITE, RELIGHT};

    protected WorldRelighter(ChunkAccess access, boolean verbose)
    {
        this.profiler = new EnumProfiler<Mode>(Mode.DEFAULT);
        this.access = access;
        this.verbose = verbose;
    }

    public static void relightWorld(ChunkAccess access, boolean verbose)
    {
        WorldRelighter relighter = new WorldRelighter(access, verbose);
        relighter.relightWorld();
    }
    
    protected void relightWorld()
    {
        profiler.reset();
        
        int step = 1 << Math.min(4, relight_batch_scale);
        int span = step + 2;
        ChunkRelighter relighter = new ChunkRelighter(span);

        Iterator<RegionInfo> regionIterator = access.getRegions().iterator();

        // relight each region
        while(regionIterator.hasNext())
        {
            RegionInfo current = regionIterator.next();
            Coordinate2D coord = current.getRegionCoordinate();
            
            log("Re-Lighting " + current.getFile().getAbsolutePath());

            int x0 = coord.x << 5;
            int z0 = coord.z << 5;

            // relight each chunk in batches
            for(int z = z0; z < z0 + 32; z += step)
                for(int x = x0; x < x0 + 32; x += step)
                    relight_batch(relighter, x - 1, z - 1, span);
        }

        access.closeAll();
        
        log("Finished!");
        printRunningTime();
    }

    protected void relight_batch(ChunkRelighter relighter, int x0, int z0, int span)
    {
        profiler.setMode(Mode.READ);
        Chunk[] localChunks = relight_readChunks(x0, z0, span);

        profiler.setMode(Mode.RELIGHT);
        relighter.lightChunks(localChunks);

        profiler.setMode(Mode.WRITE);
        relight_writeChunks(x0, z0, span, localChunks);

        profiler.setMode(Mode.DEFAULT);
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
    
    protected Chunk readChunk(int x, int z)
    {
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

    protected boolean writeChunk(Chunk chunk, int x, int z)
    {
        try
        {
            access.writeChunk(chunk);
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    protected void printRunningTime()
    {
        StringBuilder str = new StringBuilder();
        
        str.append("Time elapsed: " + profiler.getMilliseconds() + "ms ");
        str.append("(read " + profiler.getMilliseconds(Mode.READ) + "ms, ");
        str.append("write " + profiler.getMilliseconds(Mode.WRITE) + "ms, ");
        str.append("relight " + profiler.getMilliseconds(Mode.RELIGHT) + "ms, ");
        str.append("other " + profiler.getMilliseconds(Mode.DEFAULT) + "ms)");
        
        log(str.toString());
    }
    
    protected void log(String str)
    {
        if(verbose)
            System.out.println(str);
    }
}