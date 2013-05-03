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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rakama.worldtools.data.entity.EntityFactory;

import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.ByteTag;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.IntArrayTag;
import com.mojang.nbt.IntTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.LongTag;
import com.mojang.nbt.Tag;

public class Chunk
{
    public final static int num_sections = 16;
    public final static int width = Section.width;
    public final static int length = Section.length;
    public final static int height = num_sections * Section.height;
    public final static int area = width * length;
    public final static int volume = width * length * height;

    protected final static int default_blockid = Section.default_blockid;
    protected final static int default_metadata = Section.default_metadata;
    protected final static int default_skylight = Section.default_skylight;
    protected final static int default_blocklight = Section.default_blocklight;

    protected int x, z;
    protected Section[] sections;
    protected int[] heightmap;
    protected byte[] biomes;

    protected List<Entity> entities;
    protected List<TileEntity> tileEntities;
    
    protected CompoundTag tag;

    public Chunk(int x, int z)
    {
        this(x, z, new int[area], new byte[area]);
    }

    public Chunk(int x, int z, int[] heightmap, byte[] biomes)
    {
        if(heightmap.length != area || biomes.length != area)
            throw new IllegalArgumentException("Expected array of size " + area);
        
        this.sections = new Section[num_sections];
        this.heightmap = heightmap;
        this.biomes = biomes;
        this.x = x;
        this.z = z;
        this.entities = new ArrayList<Entity>();
        this.tileEntities = new ArrayList<TileEntity>();
    }
    
    public int getX()
    {
        return x;
    }

    public int getZ()
    {
        return z;
    }
    
    public void setHeight(int x, int z, int val)
    {
        checkBounds(x, z);
        heightmap[x + (z << 4)] = val;
    }

    public void setBiome(int x, int z, int val)
    {
        checkBounds(x, z);
        biomes[x + (z << 4)] = (byte)val;
    }

    public void setBiome(int x, int z, Biome biome)
    {
        checkBounds(x, z);
        biomes[x + (z << 4)] = (byte)biome.getID();
    }
    
    public int getHeight(int x, int z)
    {
        checkBounds(x, z);
        return heightmap[x + (z << 4)];
    }

    public int getBiome(int x, int z)
    {
        checkBounds(x, z);
        return biomes[x + (z << 4)];
    }

    public void setBlock(int x, int y, int z, Block block)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, true);
        sec.setBlock(x, y & 0xF, z, block);
    }
    
    public void setBlockID(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, true);
        sec.setBlockID(x, y & 0xF, z, val);
    }

    public void setMetaData(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, true);
        sec.setMetaData(x, y & 0xF, z, val);
    }

    public void setBlockLight(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, true);
        sec.setBlockLight(x, y & 0xF, z, val);
    }

    public void setSkyLight(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, true);
        sec.setSkyLight(x, y & 0xF, z, val);
    }

    public Block getBlock(int x, int y, int z)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, false);

        if(sec == null)
            return Block.getBlock(default_blockid);

        return sec.getBlock(x, y & 0xF, z);
    }
    
    public int getBlockID(int x, int y, int z)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, false);

        if(sec == null)
            return default_blockid;

        return sec.getBlockID(x, y & 0xF, z);
    }

    public int getMetaData(int x, int y, int z)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, false);

        if(sec == null)
            return default_metadata;

        return sec.getMetaData(x, y & 0xF, z);
    }

    public int getBlockLight(int x, int y, int z)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, false);

        if(sec == null)
            return default_blocklight;

        return sec.getBlockLight(x, y & 0xF, z);
    }

    public int getSkyLight(int x, int y, int z)
    {
        checkBounds(x, y, z);
        Section sec = getContainingSection(y, false);

        if(sec == null)
            return default_skylight;

        return sec.getSkyLight(x, y & 0xF, z);
    }
    
    protected int[] getHeightmap()
    {
        return heightmap;
    }

    protected byte[] getBiomes()
    {
        return biomes;
    }
    
    public Section getSection(int y)
    {
        return sections[y];
    }
    
    protected Section getContainingSection(int y, boolean create)
    {
        int index = y >> 4;

        if(index < 0 || index >= num_sections)
            return null;

        synchronized(this)
        {
            Section sec = sections[index];
            
            if(create && sec == null)
            {
                createSection(index);
                sec = sections[index];
            }
            
            return sec;
        }
    }
    
    public List<Entity> getEntities()
    {
        return Collections.unmodifiableList(entities);
    }

    public List<TileEntity> getTileEntities()
    {
        return Collections.unmodifiableList(tileEntities);
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
    
    private synchronized void createSection(int index)
    {
        if(sections[index] == null)        
            sections[index] = new Section(index);
    }

    public synchronized CompoundTag getTag()
    {
        if(tag == null)
            tag = createChunkTag();

        // recreate list to guarantee that new sections are included
        ListTag<CompoundTag> list = new ListTag<CompoundTag>();

        for(Section sec : sections)
            if(sec != null)
                list.add(sec.createTag());
        
        CompoundTag level = (CompoundTag)tag.get("Level");
        level.put("Sections", list);
        
        ListTag<CompoundTag> tagEntities = new ListTag<CompoundTag>("Entities");
        for(Entity e : entities)
            tagEntities.add(e.getTag());

        ListTag<CompoundTag> tagTileEntities = new ListTag<CompoundTag>("TileEntities");
        for(TileEntity e : tileEntities)
            tagTileEntities.add(e.getTag());
        
        level.put("Entities", tagEntities);
        level.put("TileEntities", tagTileEntities);

        return tag;
    }
    
    private CompoundTag createChunkTag()
    {
        CompoundTag level = new CompoundTag("Level");
        level.put("Biomes", new ByteArrayTag("Biomes", biomes));
        level.put("HeightMap", new IntArrayTag("HeightMap", heightmap));
        level.put("LastUpdate", new LongTag("LastUpdate", 0));
        level.put("xPos", new IntTag("xPos", x));
        level.put("zPos", new IntTag("zPos", z));
        level.put("TerrainPopulated", new ByteTag("TerrainPopulated", (byte)1));

        CompoundTag root = new CompoundTag("");
        root.put("Level", level);
        return root;
    }

    protected void checkBounds(int x, int z)
    {
        if(!inBounds(x, 0, z))
            throw new IndexOutOfBoundsException("(" + x + ", " + z + ")");
    }

    protected void checkBounds(int x, int y, int z)
    {
        if(!inBounds(x, y, z))
            throw new IndexOutOfBoundsException("(" + x + ", " + y + ", " + z + ")");
    }

    protected boolean inBounds(int x, int y, int z)
    {
        return x == (x & 0xF) && y == (y & 0xFF) && z == (z & 0xF);
    }

    public boolean isEmpty()
    {
        for(Section section : sections)
            if(section != null)
                return false;

        return true;
    }

    public synchronized void recomputeHeightmap()
    {
        for(int z = 0; z < length; z++)
            for(int x = 0; x < width; x++)
                recomputeHeight(x, z);
    }

    protected synchronized void recomputeHeight(int x, int z)
    {
        int hindex = x + (z << 4);
        heightmap[hindex] = 0;

        for(int sec = num_sections - 1; sec >= 0; sec--)
        {
            Section section = sections[sec];

            if(section == null)
                continue;

            Block block = Block.AIR;
            int y = Section.height;
            while(!block.providesShade() && --y >= 0)
                block = Block.getBlock(0xFF & section.blockid[hindex + (y << 8)]);

            if(block.providesShade())
            {
                heightmap[hindex] = y + (sec << 4) + 1;
                break;
            }
        }
    }
    
    public synchronized void trimSections()
    {
        boolean fill = false;

        for(int i = num_sections - 1; i >= 0; i--)
        {
            if(sections[i] == null)
            {
                if(fill)
                    sections[i] = new Section(i);
                else
                    continue;
            }
            else if(!fill && sections[i].isEmptyAir())
                sections[i] = null;
            else
                fill = true;
        }
    }

    public synchronized void clearBlockLights()
    {
        for(Section section : sections)
        {
            if(section == null)
                continue;

            section.blocklight.fill(0);
        }
    }

    public synchronized void clearSkyLights()
    {
        for(Section section : sections)
        {
            if(section == null)
                continue;

            section.skylight.fill(0);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static Chunk loadChunk(CompoundTag tag)
    {
        CompoundTag level = (CompoundTag) tag.get("Level");

        ListTag<CompoundTag> sections = (ListTag<CompoundTag>) level.get("Sections");
        IntArrayTag heightmap = (IntArrayTag) level.get("HeightMap");
        ByteArrayTag biome = (ByteArrayTag) level.get("Biomes");
        IntTag xPos = (IntTag) level.get("xPos");
        IntTag zPos = (IntTag) level.get("zPos");

        Chunk chunk = new Chunk(xPos.data, zPos.data, heightmap.data, biome.data);
        chunk.loadSections(sections);

        Tag tagEntities = level.get("Entities");   
        if(tagEntities != null)
        {
            ListTag<CompoundTag> list = (ListTag<CompoundTag>)tagEntities;            
            for(int i=0; i<list.size(); i++)
                chunk.entities.add(EntityFactory.getEntity(list.get(i)));
        }
        
        Tag tagTileEntities = level.get("TileEntities");
        if(tagEntities != null)
        {
            ListTag<CompoundTag> list = (ListTag<CompoundTag>)tagTileEntities;            
            for(int i=0; i<list.size(); i++)
                chunk.tileEntities.add(EntityFactory.getTileEntity(list.get(i)));
        }

        chunk.tag = tag;
        return chunk;
    }
    
    protected void loadSections(ListTag<CompoundTag> sections)
    {
        for(int i = 0; i < sections.size(); i++)
        {
            CompoundTag section = sections.get(i);
            Section sec = Section.loadSection(section);
            int y = sec.getY();
            this.sections[y] = sec;
        }
    }
}