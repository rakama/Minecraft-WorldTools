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

package rakama.worldtools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import rakama.worldtools.canvas.WorldCanvas;
import rakama.worldtools.io.ChunkAccess;
import rakama.worldtools.io.ChunkManager;
import rakama.worldtools.light.WorldRelighter;


public class WorldManager
{    
    protected File regionDirectory, rootDirectory;
    protected ChunkAccess access;
    protected ChunkManager manager;
    protected WorldCanvas canvas;

    protected WorldManager()
    {
    }
    
    protected void setDirectory(File directory) throws IOException
    {
        rootDirectory = findRootDirectory(directory);
        regionDirectory = findRegionDirectory(directory);
        access = ChunkAccess.createInstance(regionDirectory);
        manager = new ChunkManager(access);
        canvas = new WorldCanvas(manager);
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
    
    public WorldCanvas getCanvas()
    {
        return canvas;
    }
    
    public void relightAll()
    {
        manager.closeAll();
        WorldRelighter.relightWorld(access, true);
    }
    
    public void closeAll()
    {
        manager.closeAll();
    }
}