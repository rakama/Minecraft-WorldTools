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

package rakama.worldtools.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import rakama.worldtools.canvas.BlockCanvas;
import rakama.worldtools.data.entity.EntityFactory;

import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.NbtIo;
import com.mojang.nbt.ShortTag;
import com.mojang.nbt.StringTag;
import com.mojang.nbt.Tag;

public class Schematic implements BlockCanvas
{
    protected final int width, height, length;
    protected final byte[] blockid;
    protected final byte[] metadata;

    protected List<Entity> entities;
    protected List<TileEntity> tileEntities;
    
    protected CompoundTag tag;
    
    public Schematic(int width, int height, int length)
    {
        this(width, height, length, new byte[width*height*length], 
                new byte[width*height*length]);
    }
    
    protected Schematic(int width, int height, int length, byte[] blockid, byte[] metadata)
    {
        this.width = width;
        this.height = height;
        this.length = length;
        this.blockid = blockid;
        this.metadata = metadata;
        this.entities = new ArrayList<Entity>();
        this.tileEntities = new ArrayList<TileEntity>();
    }

    @SuppressWarnings("unchecked")
    protected Schematic(CompoundTag tag)
    {
        this.tag = tag;
        this.width = ((ShortTag)tag.get("Width")).data;
        this.height = ((ShortTag)tag.get("Height")).data;
        this.length = ((ShortTag)tag.get("Length")).data;
        this.blockid = ((ByteArrayTag)tag.get("Blocks")).data;
        this.metadata = ((ByteArrayTag)tag.get("Data")).data;
        
        this.entities = new ArrayList<Entity>();
        this.tileEntities = new ArrayList<TileEntity>();
        
        Tag tagEntities = tag.get("Entities");        
        if(tagEntities != null)
        {
            ListTag<CompoundTag> list = (ListTag<CompoundTag>)tagEntities;            
            for(int i=0; i<list.size(); i++)
                entities.add(EntityFactory.getEntity(list.get(i)));
        }
        
        Tag tagTileEntities = tag.get("TileEntities");
        if(tagEntities != null)
        {
            ListTag<CompoundTag> list = (ListTag<CompoundTag>)tagTileEntities;            
            for(int i=0; i<list.size(); i++)
                tileEntities.add(EntityFactory.getTileEntity(list.get(i)));
        }
    }
    
    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public int getLength()
    {
        return length;
    }

    public void setBlock(int x, int y, int z, Block block)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        blockid[index] = (byte)block.id;
        metadata[index] = (byte)block.data;
    }
    
    public void setBlock(int x, int y, int z, int id, int data)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        blockid[index] = (byte)id;
        metadata[index] = (byte)data;
    }
    
    public void setBlockID(int x, int y, int z, int id)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        blockid[index] = (byte)id;
    }
    
    public void setMetaData(int x, int y, int z, int data)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        metadata[index] = (byte)data;
    }
    
    public Block getBlock(int x, int y, int z)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        int data = metadata[index] & 0xFF;
        int id = blockid[index] & 0xFF;
        return Block.getBlock(id, data);
    }
    
    public int getBlockID(int x, int y, int z)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        return blockid[index] & 0xFF;
    }
    
    public int getMetaData(int x, int y, int z)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        return metadata[index] & 0xFF;
    }

    public int getBlockLight(int x, int y, int z)
    {
        return -1;
    }

    public int getSkyLight(int x, int y, int z)
    {
        return -1;
    }
    
    public void setBiome(int x, int z, int biome)
    {
        throw new UnsupportedOperationException();
    }
    
    public void setBiome(int x, int z, Biome biome)
    {
        throw new UnsupportedOperationException();
    }
    
    public int getBiome(int x, int z)
    {
        throw new UnsupportedOperationException();
    }
    
    public int getHeight(int x, int z)
    {
        throw new UnsupportedOperationException();
    }    

    public List<Entity> getEntities()
    {
        return Collections.unmodifiableList(entities);
    }

    public List<TileEntity> getTileEntities()
    {
        return Collections.unmodifiableList(tileEntities);
    }

    public List<Entity> getEntities(int x0, int y0, int z0, int x1, int y1, int z1)
    {
        if(x1 < x0 || y1 < y0 || z1 < z0)
            throw new IllegalArgumentException("Dimensions must be positive");

        List<Entity> list = new LinkedList<Entity>();
        
        for(Entity e : entities)
            if(e.getX() >= x0 && e.getX() <= x1
            && e.getY() >= y0 && e.getY() <= y1
            && e.getZ() >= z0 && e.getZ() <= z1)
                list.add(e);

        return Collections.unmodifiableList(list);    
    }

    public List<TileEntity> getTileEntities(int x0, int y0, int z0, int x1, int y1, int z1)
    {
        if(x1 < x0 || y1 < y0 || z1 < z0)
            throw new IllegalArgumentException("Dimensions must be positive");

        List<TileEntity> list = new LinkedList<TileEntity>();
        
        for(TileEntity e : tileEntities)
            if(e.getX() >= x0 && e.getX() <= x1
            && e.getY() >= y0 && e.getY() <= y1
            && e.getZ() >= z0 && e.getZ() <= z1)
                list.add(e);

        return Collections.unmodifiableList(list);        
    }
    
    public void addEntity(Entity e)
    {
        entities.add(e);
    }
    
    public void addTileEntity(TileEntity e)
    {
        tileEntities.add(e);
    }
    
    public boolean removeEntity(Entity e)
    {
        return entities.remove(e);
    }
    
    public boolean removeTileEntity(TileEntity e)
    {
        return tileEntities.remove(e);
    }
    
    public void importSchematic(int x0, int y0, int z0, Schematic schematic)
    {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    public Schematic exportSchematic(int x0, int y0, int z0, int x1, int y1, int z1)
    {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
    
    public CompoundTag getTag()
    {
        if(tag == null)
            tag = new CompoundTag("Schematic");

        ShortTag tagW = new ShortTag("Width", (short)width);
        ShortTag tagH = new ShortTag("Height", (short)height);
        ShortTag tagL = new ShortTag("Length", (short)length);
        StringTag tagMat = new StringTag("Materials", "Alpha");
        ByteArrayTag tagBlockid = new ByteArrayTag("Blocks", blockid);
        ByteArrayTag tagMetadata = new ByteArrayTag("Data", metadata);

        tag.put("Width", tagW);
        tag.put("Height", tagH);
        tag.put("Length", tagL);
        tag.put("Materials", tagMat);
        tag.put("Blocks", tagBlockid);
        tag.put("Data", tagMetadata);

        ListTag<CompoundTag> tagEntities = new ListTag<CompoundTag>("Entities");
        for(Entity e : entities)
            tagEntities.add(e.getTag());

        ListTag<CompoundTag> tagTileEntities = new ListTag<CompoundTag>("TileEntities");
        for(TileEntity e : tileEntities)
            tagTileEntities.add(e.getTag());
                
        tag.put("Entities", tagEntities);
        tag.put("TileEntities", tagTileEntities);

        return tag;
    }

    protected void checkBounds(int x, int y, int z)
    {
        if(!inBounds(x, y, z))
            throw new IndexOutOfBoundsException("(" + x + ", " + y + ", " + z + ")");
    }

    protected boolean inBounds(int x, int y, int z)
    {
        return x >= 0 && x < width 
            && y >= 0 && y < height 
            && z >= 0 && z < length;
    }

    protected int toIndex(int x, int y, int z)
    {
        return x + (z * width) + (y * length * width);
    }

    public static Schematic loadSchematic(CompoundTag tag) throws IOException
    {        
        try
        {
            return new Schematic(tag);
        }
        catch(Exception e)
        {
            throw new IOException(e);
        }
    }
    
    public static Schematic loadSchematic(File file) throws IOException
    {
        return loadSchematic(NbtIo.read(file));
    }
    
    public static void saveSchematic(Schematic schema, File file) throws IOException
    {
        NbtIo.write(schema.getTag(), file);
    }
}