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

package com.github.rakama.worldtools.data.entity;

import com.github.rakama.worldtools.data.Entity;
import com.github.rakama.worldtools.data.TileEntity;
import com.mojang.nbt.CompoundTag;
import com.mojang.nbt.IntTag;
import com.mojang.nbt.StringTag;
import com.mojang.nbt.Tag;

public class EntityFactory
{	
	public static Entity getEntity(CompoundTag tag)
	{
		Tag temp = tag.get("id");
		
		if(temp == null || !(temp instanceof StringTag))
			throw new IllegalArgumentException("Unrecognized tag format");
		
		StringTag id = (StringTag)temp;
		
		return new EntityImpl(tag);
	}

	public static TileEntity getTileEntity(CompoundTag tag)
	{
		Tag temp = tag.get("id");
		
		if(temp == null || !(temp instanceof StringTag))
			throw new IllegalArgumentException("Unrecognized tag format");
		
		StringTag id = (StringTag)temp;
		
		if(id.data.equals("Control"))
			return new CommandBlock(tag);
		else
			return new TileEntityImpl(tag);
	}
	
	public static CommandBlock createCommandBlock(int x, int y, int z, String command)
	{
		CompoundTag root = createRoot(x, y, z, "Control");
		root.put("Command", new StringTag("Command", command));
		return new CommandBlock(root);
	}
	
	protected static CompoundTag createRoot(int x, int y, int z, String id)
	{
		CompoundTag root = new CompoundTag();
		root.put("x", new IntTag("x", x));
		root.put("y", new IntTag("y", y));
		root.put("z", new IntTag("z", z));
		root.put("id", new StringTag("id", id));
		return root;
	}
}