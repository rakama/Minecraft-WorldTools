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

package rakama.worldtools.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.world.level.chunk.storage.RegionFile;
import rakama.worldtools.data.Chunk;
import rakama.worldtools.data.entity.EntityFactory;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.NbtIo;

public class ChunkAccess
{
    protected final boolean debug = false;
    protected final boolean write_empty_chunks = false;
    
    private File regionDirectory;
    private RegionManager regionManager;
    private EntityFactory entityFactory;

    protected ChunkAccess()
    {
        regionManager = new RegionManager();
        entityFactory = EntityFactory.getDefaultFactory();
    }

    public static ChunkAccess createInstance(File directory) throws IOException
    {    
        ChunkAccess access = new ChunkAccess();
        access.init(directory);
        return access;
    }
    
    protected void init(File directory) throws IOException
    {
        if(regionDirectory != null)
            throw new IllegalStateException("Region directory already initialized!");

        if(!directory.exists())
            throw new FileNotFoundException(directory.getCanonicalPath());
        
        // get parent directory if argument is a file
        if(directory.isFile())
           directory = directory.getParentFile();

        // check for level.dat, to see if we're in the map directory
        File level = new File(directory.getCanonicalPath() + "/level.dat");
        if(level.exists())
        {
            // try switching to the /region directory
            File newDirectory = new File(directory.getCanonicalPath() + "/region");
            if(newDirectory.exists())
                directory = newDirectory;
        }

        this.regionDirectory = directory;
        
        // check for any .mca files in the current directory
        File[] files = getRegionFiles(directory);

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
                regionManager.addFile(file, x, z);
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
        if(debug)
            log("READ_CHUNK " + x + " " + z);
        
        DataInputStream dis = getDataInputStream(x, z);

        if(dis == null)
            return null;

        // decompress chunk
        CompoundTag tag = NbtIo.read(dis);
        Chunk chunk = Chunk.loadChunk(tag);
        dis.close();
        
        return chunk;
    }
    
    protected ManagedChunk readChunk(int x, int z, ChunkManager manager) throws IOException
    {
        if(debug)
            log("READ_CHUNK " + x + " " + z);
        
        DataInputStream dis = getDataInputStream(x, z);

        if(dis == null)
            return null;

        // decompress chunk
        CompoundTag tag = NbtIo.read(dis);
        ManagedChunk chunk = ManagedChunk.loadChunk(tag, manager);
        dis.close();
        
        if(x != chunk.getX() || z != chunk.getZ())
            chunk.setChunkCoordinate(x, z);
        
        return chunk;
    }

    public void writeChunk(Chunk chunk) throws IOException
    {
        if(debug)
            log("WRITE_CHUNK " + chunk.getX() + " " + chunk.getZ());
        
        if(!write_empty_chunks && chunk.isEmpty() 
        && getDataInputStream(chunk.getX(), chunk.getZ()) == null)
            return;
        
        DataOutputStream dos = getDataOutputStream(chunk.getX(), chunk.getZ());

        if(dos == null)
            throw new IOException();

        NbtIo.write(chunk.getTag(), dos);
        dos.close();
    }

    protected DataInputStream getDataInputStream(int x, int z) throws IOException
    {
        RegionFile region = regionManager.getRegionFile(x >> 5, z >> 5);

        if(region == null)
            return null;

        return region.getChunkDataInputStream(x & 0x1F, z & 0x1F);
    }

    protected DataOutputStream getDataOutputStream(int x, int z) throws IOException
    {
        RegionFile region = regionManager.getRegionFile(x >> 5, z >> 5);

        if(region == null)
        {
            region = createRegionFile(x >> 5, z >> 5);
            if(region == null)
                throw new IOException();
        }

        return region.getChunkDataOutputStream(x & 0x1F, z & 0x1F);
    }

    private RegionFile createRegionFile(int x, int z) throws IOException
    {
        String path = regionDirectory.getCanonicalPath() + "/r." + x + "." + z + ".mca";        
        regionManager.addFile(new File(path), x, z);
        return regionManager.getRegionFile(x, z);
    }
    
    public EntityFactory getEntityFactory()
    {
        return entityFactory;
    }
    
    public Collection<RegionInfo> getRegions()
    {
        return regionManager.getRegions();
    }

    public File getRegionDirectory()
    {
        return regionDirectory;
    }
    
    protected RegionManager getRegionManager()
    {
        return regionManager;
    }
    
    public void closeAll()
    {
        regionManager.closeAll();
    }

    protected final void log(String str)
    {
        System.out.println(str);
    }

    @Override
    protected void finalize()
    {
        closeAll();
    }
}