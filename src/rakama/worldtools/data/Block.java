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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public final class Block implements Comparable<Block>
{
    protected final static String default_materials_file = "csv/materials.csv";    
    protected final static Block[] list = new Block[4096];

    static
    {
        initializeMaterials(Block.class.getResource(default_materials_file));
    }
    
    public final static Block AIR = getBlock(0);
    public final static Block STONE = getBlock(1);
    public final static Block GRASS = getBlock(2);
    public final static Block DIRT = getBlock(3);
    public final static Block COBBLESTONE = getBlock(4);
    public final static Block PLANKS = getBlock(5);
    public final static Block BEDROCK = getBlock(7);
    public final static Block WATER = getBlock(9);
    public final static Block LAVA = getBlock(11);
    public final static Block SAND = getBlock(12);
    public final static Block GRAVEL = getBlock(13);
    public final static Block WOOD = getBlock(17);
    public final static Block LEAVES = getBlock(18, 4);
    public final static Block GLASS = getBlock(20);
    public final static Block SANDSTONE = getBlock(24);
    public final static Block SHRUB = getBlock(32);
    public final static Block WOOL = getBlock(35);
    public final static Block BRICKS = getBlock(45);
    public final static Block OBSIDIAN = getBlock(49);
    public final static Block ICE = getBlock(79);
    public final static Block SNOW = getBlock(80);
    public final static Block CLAY = getBlock(82);
    public final static Block MYCELIUM = getBlock(110);
    
    public final int id, data;
    
    protected boolean transparent, shade;
    protected int diffusion, luminance;
    
    protected Block(int id, int data, boolean trs, boolean shd, int dif, int lum)
    {
        this.id = id;
        this.data = data;
        this.transparent = trs;
        this.shade = shd;
        this.diffusion = dif;
        this.luminance = lum;
    }
    
    protected static void initializeMaterials(URL url)
    {
        try
        {      
            loadMaterials(list, url);
        }
        catch(Exception e)
        {
            throw new RuntimeException("WorldTools encountered an error while initializing!", e);
        }
    }

    protected static void loadMaterials(Block[] list, URL url) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        
        reader.readLine();
        String line = reader.readLine();
        int count = 2;
        
        while(line != null)
        {
            loadEntry(count, list, line.split(","));
            line = reader.readLine();
            count++;
        }
    }

    protected static void loadEntry(int count, Block[] list, String... vals)
    {
        try
        {
            int id = Integer.parseInt(vals[0].trim()) & 0xFF;
            boolean trs = Integer.parseInt(vals[1].trim()) > 0;
            boolean shd = Integer.parseInt(vals[2].trim()) > 0 || !trs;
            int dif = Integer.parseInt(vals[3].trim()) & 0xF;
            int lum = Integer.parseInt(vals[4].trim()) & 0xF;

            for(int dat=0; dat<16; dat++)
            {
                int index = id + (dat << 8);
                Block block = list[index];
                
                if(block == null)
                {
                    list[index] = new Block(id, dat, trs, shd, dif, lum);
                }
                else
                {
                    block.transparent = trs;
                    block.shade = shd;
                    block.diffusion = dif;
                    block.luminance = lum;
                }
            }
        }
        catch(Exception e)
        {
            System.err.println("Found improperly formatted material! (line " + count + ")");            
            System.err.println(e);
        }
    }

    public static Block getBlock(String str)
    {
        if(str.isEmpty())
            return null;
        
        String[] split = str.trim().split(":");
        
        int id = Integer.parseInt(split[0]);
        
        if(split.length > 1)
        {
            int data = Integer.parseInt(split[1]);
            return getBlock(id, data);
        }
        else
            return getBlock(id);
    }
    
    public static Block getBlock(int id)
    {
        if(id != (id & 0xFF))
            return null;

        return list[id];
    }

    public static Block getBlock(int id, int data)
    {
        if(id != (id & 0xFF) || data != (data & 0xF))
            return null;

        return list[id + (data << 8)];
    }
    
    public boolean isTransparent()
    {
        return transparent;
    }

    public boolean isOpaque()
    {
        return !transparent;
    }

    public boolean providesShade()
    {
        return shade;
    }
    
    public int getLightDiffusion()
    {
        return diffusion;
    }
    
    public int getLuminance()
    {
        return luminance;
    }
    
    public static boolean isTransparent(int id)
    {
        if(id != (id & 0xFF))
            return true;

        return list[id].isTransparent();
    }

    public static boolean isOpaque(int id)
    {
        if(id != (id & 0xFF))
            return true;

        return list[id].isOpaque();
    }

    public static boolean providesShade(int id)
    {
        if(id != (id & 0xFF))
            return true;

        return list[id].providesShade();
    }
    
    public static int getLightDiffusion(int id)
    {
        if(id != (id & 0xFF))
            return 0;

        return list[id].getLightDiffusion();
    }

    public static int getLuminance(int id)
    {
        if(id != (id & 0xFF))
            return 0;

        return list[id].getLuminance();
    }
    
    public String toString()
    {
        if(data > 0)
            return id + ":" + data;
        else
            return String.valueOf(id);
    }
    
    public int hashCode()
    {
        return id + (data << 8);
    }
    
    public boolean equals(Object o)
    {
        return equals((Block)o);
    }
    
    public boolean equals(Block b)
    {
        return b.id == id && b.data == data;
    }

    public int compareTo(Block b)
    {
        return ((id << 4) + data) - ((b.id << 4) + b.data);
    }
}