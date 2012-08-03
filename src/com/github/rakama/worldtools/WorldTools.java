package com.github.rakama.worldtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.github.rakama.worldtools.canvas.BlockCanvas;
import com.github.rakama.worldtools.io.ChunkAccess;
import com.github.rakama.worldtools.io.ChunkManager;
import com.github.rakama.worldtools.light.WorldRelighter;

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
    protected File regionDirectory, rootDirectory;
    protected ChunkAccess access;
    protected ChunkManager manager;

    protected WorldTools()
    {
    }
    
    /**
     * Creates a WorldTools instance, where rootDirectory points to the 
     * location of a Minecraft world's "level.dat" file.
     * 
     * @param rootDirectory directory location for a "level.dat" file
     * @return a WorldTools instance for the specified world
     * @throws IOException
     */
    public static WorldTools getInstance(File rootDirectory) throws IOException
    {
        WorldTools tools = new WorldTools();
        tools.setDirectory(rootDirectory);
        return tools;
    }
    
    protected void setDirectory(File directory) throws IOException
    {        
        rootDirectory = findRootDirectory(directory);
        regionDirectory = findRegionDirectory(directory);
        access = ChunkAccess.createInstance(regionDirectory);
        manager = new ChunkManager(access);
    }
    
    protected File findRootDirectory(File directory) throws IOException
    {
        if(!directory.exists())
            throw new FileNotFoundException(directory.getCanonicalPath());
        
        // get parent directory if argument is a file
        if(directory.isFile())
           directory = directory.getParentFile();

        // check for level.dat, to see if we're in the map directory
        File level = new File(directory.getCanonicalPath() + "/level.dat");
        if(!level.exists())
            throw new FileNotFoundException(directory.getCanonicalPath());

        return directory;
    }
    
    protected File findRegionDirectory(File directory) throws IOException
    {
        if(!directory.exists())
            throw new FileNotFoundException(directory.getCanonicalPath());

        // check for ..\region sub-directory
        directory = new File(directory.getCanonicalPath() + "/region");
        if(!directory.exists())
            throw new FileNotFoundException(directory.getCanonicalPath());   

        return directory;
    }
    
    public File getRootDirectory()
    {
        return rootDirectory;
    }

    public File getRegionDirectory()
    {
        return regionDirectory;
    }

    public ChunkManager getChunkManager()
    {
        return manager;
    } 
    
    public BlockCanvas createCanvas()
    {
        return BlockCanvas.createCanvas(manager);
    }
    
    public void relightWorld()
    {
        manager.closeAll();
        WorldRelighter.relightWorld(access, true);
    }
    
    public void closeAll()
    {
        manager.closeAll();
    }
    
    public static void main(String[] args)
    {
        System.out.println("WorldTools v0.1");

        if(args.length < 1)
        {
            System.err.println("Couldn't load map directory: (none specified)");
            System.exit(0);
        }
        
        WorldTools tools = null;

        try
        {
            tools = WorldTools.getInstance(new File(args[0]));
        }
        catch(Exception e)
        {
            System.err.println("Couldn't load map directory: " + args[0]);
            e.printStackTrace();
            System.exit(0);
        }
        
        tools.relightWorld();
        System.exit(0);
    }
}