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

package rakama.worldtools.tex;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import rakama.worldtools.data.Block;


public class ColorProfile
{
    protected static String default_file = "csv/colors.csv";   
    protected static ColorProfile default_profile;

    protected float hue_weight, sat_weight, val_weight;
    protected float red_weight, grn_weight, blu_weight;    
    protected List<Entry> blocks;

    protected ColorProfile()
    {
        blocks = new LinkedList<Entry>();
        
        hue_weight = sat_weight = val_weight = 0.1f;
        red_weight = grn_weight = blu_weight = 0.0f;
    }
    
    public static ColorProfile getDefaultProfile()
    {
        if(default_profile != null)
            return default_profile;
        
        try
        {
            default_profile = getColorProfile(ColorProfile.class.getResource(default_file));
        }
        catch(Exception e)
        {
            throw new RuntimeException("WorldTools encountered an error while initializing!", e);
        }
        
        return default_profile;
    }
    
    public static ColorProfile getColorProfile(URL url) throws IOException
    {
        ColorProfile profile = new ColorProfile();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line = reader.readLine();

        try
        {
            String[] split = line.split(",");
            
            profile.hue_weight = Float.parseFloat(split[0].trim());
            profile.sat_weight = Float.parseFloat(split[1].trim());
            profile.val_weight = Float.parseFloat(split[2].trim());            
            profile.red_weight = Float.parseFloat(split[3].trim());
            profile.grn_weight = Float.parseFloat(split[4].trim());
            profile.blu_weight = Float.parseFloat(split[5].trim());
        }
        catch(Exception e)
        {
            // do nothing
        }
        
        while(line != null)
        {
            Entry e = loadEntry(line.split(","));

            if(e != null)
                profile.blocks.add(e);
                      
            line = reader.readLine();
        }
        
        return profile;
    }
    
    protected static Entry loadEntry(String[] split)
    {
        if(split.length < 2)
            return null;
        
        try
        {
            Block block = Block.getBlock(split[0].trim());
            String hexstr = split[1].trim();
            return new Entry(block, hexstr);
        }
        catch(Exception e)
        {
            return null;
        }
    }
    
    public Block getBlock(int rgb)
    {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = (rgb) & 0xFF;
        
        return getBlock(r, g, b);
    }
    
    public Block getBlock(int r, int g, int b)
    {
        Block closestBlock = null;
        double closestMatch = Double.POSITIVE_INFINITY;

        float[] hsv = new float[3];
        Color.RGBtoHSB(r,g,b,hsv);

        float h = hsv[0];
        float s = hsv[1];
        float v = hsv[2];
        
        for(Entry e : blocks)
        {
            float dh = h - e.h;
            float ds = s - e.s;
            float dv = v - e.v;
            float dr = r - e.r;
            float dg = g - e.g;
            float db = b - e.b;
            
            double temp;
            temp = hue_weight*dh*dh + sat_weight*ds*ds + val_weight*dv*dv;
            temp += red_weight*dr*dr + grn_weight*dg*dg + blu_weight*db*db;
            
            if(temp < closestMatch)
            {
                closestMatch = temp;
                closestBlock = e.block;
            }
        }
        
        return closestBlock;
    }

    static class Entry
    {
        final Block block;
        final int r, g, b;
        float h, s, v;

        public Entry(Block block, String str)
        {
            this(block, Integer.decode("0x" + str.replace("0x", "").replace("#", "")));
        }
        
        public Entry(Block block, int rgb)
        {
            this(block, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        }
        
        public Entry(Block block, int r, int g, int b)
        {
            this.block = block;
            this.r = r;
            this.g = g;
            this.b = b;

            float[] hsv = new float[3];
            Color.RGBtoHSB(r,g,b,hsv);

            this.h = hsv[0];
            this.s = hsv[1];
            this.v = hsv[2];
        }

    }
}