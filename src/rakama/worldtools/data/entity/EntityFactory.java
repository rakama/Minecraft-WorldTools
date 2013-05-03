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

package rakama.worldtools.data.entity;

import rakama.worldtools.data.Entity;
import rakama.worldtools.data.TileEntity;

import com.mojang.nbt.ByteTag;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.DoubleTag;
import com.mojang.nbt.FloatTag;
import com.mojang.nbt.IntTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.ShortTag;
import com.mojang.nbt.StringTag;
import com.mojang.nbt.Tag;

public class EntityFactory
{ 
    // TODO: manage entities so read/write is handled properly
    
    public static Entity getEntity(CompoundTag tag)
    {
        Tag temp = tag.get("id");
        
        if(temp == null || !(temp instanceof StringTag))
            throw new IllegalArgumentException("Unrecognized tag format");
        
        StringTag id = (StringTag)temp;
        
        // TODO: map into correct classes
        
        return new EntityImpl(tag);
    }

    public static TileEntity getTileEntity(CompoundTag tag)
    {
        Tag temp = tag.get("id");
        
        if(temp == null || !(temp instanceof StringTag))
            throw new IllegalArgumentException("Unrecognized tag format");
        
        StringTag id = (StringTag)temp;
        
        // TODO: allow user to register these
        if(id.data.equals("Control"))
            return new CommandBlock(tag);
        else if(id.data.equals("Sign"))
            return new Sign(tag);
        else
            return new TileEntityImpl(tag);
    }

    public static EntityImpl createXPOrb(int x, int y, int z, int value)
    {
        CompoundTag root = createEntityRoot(x, y, z, "XPOrb");
        root.put("Health", new ShortTag("Health", (short)5));
        root.put("Age", new ShortTag("Age", (short)0));
        root.put("Value", new ShortTag("Value", (short)value));
        return new EntityImpl(root);
    }
    
    public static Entity createFallingBlock(int x, int y, int z, int id, int data)
    {
        CompoundTag root = createEntityRoot(x, y, z, "FallingSand");
        root.put("Tile", new ByteTag("Tile", (byte)id));
        root.put("Data", new ByteTag("Data", (byte)data));
        root.put("Time", new ByteTag("Time", (byte)0));
        root.put("DropItem", new ByteTag("DropItem", (byte)0));
        root.put("HurtEntities", new ByteTag("HurtEntities", (byte)0));
        root.put("FallHurtMax", new IntTag("FallHurtMax", 0));
        root.put("FallHurtAmount", new FloatTag("FallHurtAmount", 0));
        return new EntityImpl(root);
    }

    public static TileEntity createHiddenBlock(int x, int y, int z, int id, int data, float delay)
    {
        CompoundTag root = createTileRoot(x, y, z, "Piston");
        root.put("blockId", new IntTag("blockId", id));
        root.put("blockData", new IntTag("blockData", data));
        root.put("facing", new IntTag("facing", 0));
        root.put("progress", new FloatTag("progress", -delay));
        root.put("extending", new ByteTag("extending", (byte)0));
        return new TileEntityImpl(root);
    }
    
    public static CommandBlock createCommandBlock(int x, int y, int z, String command)
    {
        CompoundTag root = createTileRoot(x, y, z, "Control");
        root.put("Command", new StringTag("Command", command));
        return new CommandBlock(root);
    }
    
    protected static CompoundTag createTileRoot(int x, int y, int z, String id)
    {
        CompoundTag root = new CompoundTag();
        root.put("x", new IntTag("x", x));
        root.put("y", new IntTag("y", y));
        root.put("z", new IntTag("z", z));
        root.put("id", new StringTag("id", id));
        return root;
    }

    protected static CompoundTag createEntityRoot(int x, int y, int z, String id)
    {
        CompoundTag root = new CompoundTag();
        
        root.put("id", new StringTag("id", id));
        
        ListTag<DoubleTag> pos = new ListTag<DoubleTag>("Pos");
        pos.add(new DoubleTag("x", x));
        pos.add(new DoubleTag("y", y));
        pos.add(new DoubleTag("z", z));
        root.put("Pos", pos);  

        ListTag<DoubleTag> motion = new ListTag<DoubleTag>("Motion");
        motion.add(new DoubleTag("dX", 0));
        motion.add(new DoubleTag("dY", -100));
        motion.add(new DoubleTag("dZ", 0));
        root.put("Motion", motion);

        ListTag<FloatTag> rotation = new ListTag<FloatTag>("Rotation");
        rotation.add(new FloatTag("yaw", 0));
        rotation.add(new FloatTag("pitch", 0));
        root.put("Rotation", rotation);

        root.put("FallDistance", new FloatTag("FallDistance", 0));
        root.put("Fire", new ShortTag("Fire", (short)0));
        root.put("Air", new ShortTag("Air", (short)0));
        root.put("OnGround", new ByteTag("OnGround", (byte)0));
        root.put("Dimension", new IntTag("Dimension", 0));
        root.put("Invulnerable", new ByteTag("Invulnerable", (byte)1));
        root.put("PortalCooldown", new IntTag("PortalCooldown", 0));
        
        return root;
    }
}