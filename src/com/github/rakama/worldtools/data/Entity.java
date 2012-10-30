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

package com.github.rakama.worldtools.data;

import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.DoubleTag;
import com.mojang.nbt.IntTag;
import com.mojang.nbt.ListTag;
import com.mojang.nbt.StringTag;
import com.mojang.nbt.Tag;

public abstract class Entity
{
    protected CompoundTag tag;
    
    protected Entity(CompoundTag tag)
    {
        this.tag = tag;
    }
    
    public String getID()
    {
        return ((StringTag)tag.get("id")).data;
    }
    
    @SuppressWarnings("unchecked")
    protected void translate(double x, double y, double z)
    {
        ListTag<DoubleTag> pos = ((ListTag<DoubleTag>)tag.get("Pos"));
        pos.get(0).data += x;
        pos.get(1).data += y;
        pos.get(2).data += z;

        Tag xTag = tag.get("TileX");
        Tag yTag = tag.get("TileY");
        Tag zTag = tag.get("TileZ");

        if(xTag != null)
            ((IntTag)xTag).data += x;
        if(yTag != null)
            ((IntTag)yTag).data += y;
        if(zTag != null)
            ((IntTag)zTag).data += z;
    }
    
    @SuppressWarnings("unchecked")
    public double getX()
    {
        return ((ListTag<DoubleTag>)tag.get("Pos")).get(0).data;
    }

    @SuppressWarnings("unchecked")
    public double getY()
    {
        return ((ListTag<DoubleTag>)tag.get("Pos")).get(1).data;
    }

    @SuppressWarnings("unchecked")
    public double getZ()
    {
        return ((ListTag<DoubleTag>)tag.get("Pos")).get(2).data;
    }
    
    protected CompoundTag getTag()
    {
        return tag;
    }

    public Entity clone(double x, double y, double z)
    {
        Entity clone = clone();
        clone.translate(x, y, z);
        return clone;
    }
    
    public abstract Entity clone();
}