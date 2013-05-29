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

package rakama.worldtools.canvas;

import java.util.List;

import rakama.worldtools.data.Block;
import rakama.worldtools.data.Entity;
import rakama.worldtools.data.Schematic;
import rakama.worldtools.data.TileEntity;

public interface BlockCanvas
{    
    public void setBlock(int x, int y, int z, Block block);
    public void setBlock(int x, int y, int z, int id, int data);    
    public void setBlockID(int x, int y, int z, int id);
    public void setMetaData(int x, int y, int z, int data);    
    public Block getBlock(int x, int y, int z);    
    public int getBlockID(int x, int y, int z);
    public int getMetaData(int x, int y, int z);

    public List<Entity> getEntities(int x0, int y0, int z0, int x1, int y1, int z1);
    public List<TileEntity> getTileEntities(int x0, int y0, int z0, int x1, int y1, int z1);
    public void addEntity(Entity e);
    public void addTileEntity(TileEntity e);
    public boolean removeEntity(Entity e);
    public boolean removeTileEntity(TileEntity e);
    
    public void importSchematic(int x0, int y0, int z0, Schematic schematic);
    public Schematic exportSchematic(int x0, int y0, int z0, int x1, int y1, int z1);
}