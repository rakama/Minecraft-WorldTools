package com.github.rakama.worldtools.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
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

public final class Block implements Comparable<Block>
{
    final static String materials_file = "materials.csv";
    
    final static Block[] list = initializeMaterials();

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
    public final static Block WOOL = getBlock(35);
    public final static Block BRICKS = getBlock(45);
    public final static Block OBSIDIAN = getBlock(49);
    public final static Block ICE = getBlock(79);
    public final static Block SNOW = getBlock(80);
    public final static Block CLAY = getBlock(82);
    public final static Block MYCELIUM = getBlock(110);
    
    protected final int id, data;
    protected final boolean transparent, shady;
    protected final int diffusion, luminance;
    
    protected Block(int id, int data, boolean trs, boolean shd, int dif, int lum)
    {
        this.id = id;
        this.data = data;
        this.transparent = trs;
        this.shady = shd;
        this.diffusion = dif;
        this.luminance = lum;
    }
    
    protected static Block[] initializeMaterials()
    {
        Block[] list = new Block[4096];  
        
        try
        {      
            loadMaterials(list, new File(materials_file));
        }
        catch(Exception e)
        {
            System.err.println("WorldTools encountered an error while initializing!");
            e.printStackTrace();
            System.exit(0);
        }

        return list;
    }

    protected static Block[] loadMaterials(Block[] list, File file) throws IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        reader.readLine(); // labels
        String line = reader.readLine();
        int count = 2;
        while(line != null)
        {
            loadEntry(count, list, line.split(","));
            line = reader.readLine();
            count++;
        }
        
        return list;
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

            if(list[id] != null)
                System.err.println("Found duplicate material! (id " + id + ")");                
            
            for(int dat=0; dat<16; dat++)
                list[id + (dat << 8)] = new Block(id, dat, trs, shd, dif, lum);
        }
        catch(Exception e)
        {
            System.err.println("Found improperly formatted material! (line " + count + ")");            
            System.err.println(e);
        }
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
    
    public int getID()
    {
        return id;
    }

    public int getMetadata()
    {
        return data;
    }
    
    public boolean isTransparent()
    {
        return transparent;
    }

    public boolean isOpaque()
    {
        return !transparent;
    }

    public boolean isShady()
    {
        return shady;
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

    public static boolean isShady(int id)
    {
        if(id != (id & 0xFF))
            return true;

        return list[id].isShady();
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