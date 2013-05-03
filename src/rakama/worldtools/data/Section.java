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

import rakama.worldtools.util.NibbleArray;

import com.mojang.nbt.ByteArrayTag;
import com.mojang.nbt.ByteTag;
import com.mojang.nbt.CompoundTag;

public class Section
{
    public final static int width = 16;
    public final static int length = 16;
    public final static int height = 16;
    public final static int area = width * length;
    public final static int volume = width * length * height;

    protected final static int default_blockid = 0;
    protected final static int default_metadata = 0;
    protected final static int default_skylight = 15;
    protected final static int default_blocklight = 15;

    protected final int y;
    protected final byte[] blockid;
    protected final NibbleArray metadata;
    protected final NibbleArray blocklight;
    protected final NibbleArray skylight;

    public Section(int y)
    {
        this.y = y;

        blockid = new byte[volume];
        metadata = new NibbleArray(volume);
        blocklight = new NibbleArray(volume);
        skylight = new NibbleArray(volume);
    }

    public Section(int y, byte[] blockid, NibbleArray metadata, NibbleArray blocklight, NibbleArray skylight)
    {
        if(blockid.length != volume || metadata.size() != volume || blocklight.size() != volume || skylight.size() != volume)
            throw new IllegalArgumentException("Expected array of size " + volume);

        this.y = y;
        this.blockid = blockid;
        this.metadata = metadata;
        this.blocklight = blocklight;
        this.skylight = skylight;
    }

    public int getY()
    {
        return y;
    }

    public void setBlock(int index, Block block)
    {
        blockid[index] = (byte)block.id;
        metadata.set(index, block.data);
    }
    
    public void setBlock(int x, int y, int z, Block block)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        blockid[index] = (byte)block.id;
        metadata.set(index, block.data);
    }

    public void setBlockID(int index, int val)
    {
        blockid[index] = (byte)val;
    }
    
    public void setBlockID(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        blockid[toIndex(x, y, z)] = (byte)val;
    }

    public void setMetaData(int index, int val)
    {
        metadata.set(index, val);
    }
    
    public void setMetaData(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        metadata.set(toIndex(x, y, z), val);
    }

    public void setBlockLight(int index, int val)
    {
        blocklight.set(index, val);
    }
    
    public void setBlockLight(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        blocklight.set(toIndex(x, y, z), val);
    }

    public void setSkyLight(int index, int val)
    {
        skylight.set(index, val);
    }
    
    public void setSkyLight(int x, int y, int z, int val)
    {
        checkBounds(x, y, z);
        skylight.set(toIndex(x, y, z), val);
    }

    public Block getBlock(int index)
    {
        return Block.getBlock(0xFF & blockid[index], metadata.get(index));
    }

    public Block getBlock(int x, int y, int z)
    {
        checkBounds(x, y, z);
        int index = toIndex(x, y, z);
        return Block.getBlock(0xFF & blockid[index], metadata.get(index));
    }

    public int getBlockID(int index)
    {
        return 0xFF & blockid[index];
    }
    
    public int getBlockID(int x, int y, int z)
    {
        checkBounds(x, y, z);
        return 0xFF & blockid[toIndex(x, y, z)];
    }

    public int getMetaData(int index)
    {
        return metadata.get(index);
    }
    
    public int getMetaData(int x, int y, int z)
    {
        checkBounds(x, y, z);
        return metadata.get(toIndex(x, y, z));
    }

    public int getBlockLight(int index)
    {
        return blocklight.get(index);
    }
    
    public int getBlockLight(int x, int y, int z)
    {
        checkBounds(x, y, z);
        return blocklight.get(toIndex(x, y, z));
    }

    public int getSkyLight(int index)
    {
        return skylight.get(index);
    }
    
    public int getSkyLight(int x, int y, int z)
    {
        checkBounds(x, y, z);
        return skylight.get(toIndex(x, y, z));
    }

    public byte[] getBlockIDs()
    {
        return blockid;
    }

    public NibbleArray getMetaData()
    {
        return metadata;
    }

    public NibbleArray getBlockLights()
    {
        return skylight;
    }

    public NibbleArray getSkyLights()
    {
        return blocklight;
    }

    protected static void checkBounds(int x, int y, int z)
    {
        if(!inBounds(x, y, z))
            throw new IndexOutOfBoundsException("(" + x + ", " + y + ", " + z + ")");
    }

    protected static boolean inBounds(int x, int y, int z)
    {
        return x == (x & 0xF) && y == (y & 0xF) && z == (z & 0xF);
    }

    protected static int toIndex(int x, int y, int z)
    {
        return x + (z << 4) + (y << 8);
    }

    public boolean isEmptyAir()
    {
        for(int i = 0; i < volume; i++)
            if(blockid[i] != 0)
                return false;

        return true;
    }

    public CompoundTag createTag()
    {
        CompoundTag tag = new CompoundTag();

        ByteTag tagY = new ByteTag("Y", (byte) y);
        ByteArrayTag tagBlockid = new ByteArrayTag("Blocks", blockid);
        ByteArrayTag tagMetadata = new ByteArrayTag("Data", metadata.array);
        ByteArrayTag tagSkylight = new ByteArrayTag("SkyLight", skylight.array);
        ByteArrayTag tagBlocklight = new ByteArrayTag("BlockLight", blocklight.array);

        tag.put("Y", tagY);
        tag.put("Blocks", tagBlockid);
        tag.put("Data", tagMetadata);
        tag.put("SkyLight", tagSkylight);
        tag.put("BlockLight", tagBlocklight);

        return tag;
    }

    public static Section loadSection(CompoundTag tag)
    {
        ByteTag tagY = (ByteTag) tag.get("Y");
        ByteArrayTag tagBlockid = (ByteArrayTag) tag.get("Blocks");
        ByteArrayTag tagMetadata = (ByteArrayTag) tag.get("Data");
        ByteArrayTag tagSkylight = (ByteArrayTag) tag.get("SkyLight");
        ByteArrayTag tagBlocklight = (ByteArrayTag) tag.get("BlockLight");

        int y = tagY.data;

        NibbleArray metadata = new NibbleArray(tagMetadata.data);
        NibbleArray skylight = new NibbleArray(tagSkylight.data);
        NibbleArray blocklight = new NibbleArray(tagBlocklight.data);

        return new Section(y, tagBlockid.data, metadata, blocklight, skylight);
    }
}