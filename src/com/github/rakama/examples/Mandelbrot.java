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

package com.github.rakama.examples;

import java.io.File;
import java.io.IOException;

import com.github.rakama.worldtools.WorldTools;
import com.github.rakama.worldtools.canvas.BlockCanvas;
import com.github.rakama.worldtools.data.Block;

public class Mandelbrot
{
    static String directory = "C:/Users/My Computer/AppData/Roaming/.minecraft/saves/mandelbrot";
    
    public static void main(String[] args) throws IOException
    {
        WorldTools tools = WorldTools.getInstance(new File(directory));
        BlockCanvas canvas = tools.createCanvas();
        renderMandelbrot(canvas, 512, 128, 4);
        tools.closeAll();
    }
    
    public static void renderMandelbrot(BlockCanvas canvas, int size, int maxY, int samples)
    {
        int xStart = -size / 2;
        int zStart = -size / 2;
        int xEnd = xStart + size;
        int zEnd = zStart + size;

        double xScale = 4.0 / size;
        double zScale = 4.0 / size;
        double xCenter = 0.5 + xScale * (xStart + xEnd) / 2.0;
        double zCenter = zScale * (zStart + zEnd) / 2.0;
        
        for(int x=xStart; x<xEnd; x++)
        {
            for(int z=zStart; z<zEnd; z++)
            {
                double x0 = x*xScale - xCenter;
                double z0 = z*zScale - zCenter;
                
                double value = getMultisample(x0, z0, xScale, zScale, samples);
                int height = (int)(value * maxY);    
                
                renderColumn(canvas, x, z, height, maxY);
            }
        }
    }
    
    public static void renderColumn(BlockCanvas canvas, int x, int z, int height, int maxY)
    {
        Block block = getBlock(height, maxY);        
        canvas.setBlock(x, height, z, block);

        Block fill = getFill(block);  
        for(int y=0; y<height; y++)
            canvas.setBlock(x, y, z, fill);
        
        double waterLevel = getWaterLevel(maxY);        
        for(int y=height; y<waterLevel; y++)
            canvas.setBlock(x, y, z, Block.WATER);
        
        canvas.setBlock(x, 0, z, Block.BEDROCK);
    }
    
    public static Block getBlock(int y, int maxY)
    {
        if(y >= maxY)
            return Block.OBSIDIAN;
        else if(y > 0.9 * maxY)
            return Block.SNOW;
        else if(y > 0.65 * maxY)
            return Block.STONE;
        else if(y > 0.45 * maxY)
            return Block.GRASS;
        else
            return Block.SAND;
    }
    
    public static Block getFill(Block block)
    {
        if(block.equals(Block.GRASS))
            return Block.DIRT;
        else
            return block;
    }
    
    public static double getWaterLevel(int maxY)
    {
        return maxY * 0.42;
    }
    
    public static double getMultisample(double x0, double z0, double xScale, double zScale, int n)
    {
        double val = 0;
        
        for(int x=0; x<n; x++)
            for(int z=0; z<n; z++)
                val += getValue(x0 + x * xScale / n, z0 + z * zScale / n);
        
        return val / (n * n);
    }
    
    public static double getValue(double x0, double z0)
    {
        int max = 1000;
        int radius = 10;
        int iteration = 0;
        double r = 0;
        double i = 0;
        double rr = 0;
        double ii = 0;
        
        while(rr + ii < radius * radius && iteration < max)
        {
            double temp = rr - ii + x0;
            i = 2*r*i + z0;
            r = temp;
            rr = r*r;
            ii = i*i;
            iteration++;
        }
        
        double value = iteration;
        double magnitude = Math.sqrt(rr + ii);
        
        if(value < max)
            value = smooth(iteration, magnitude, radius);
        
        return flatten(flatten(flatten(normalize(value, max))));
    }
    
    private static double smooth(int iteration, double magnitude, double radius)
    {
        return iteration + 1 - Math.log(Math.log(magnitude) / Math.log(radius)) / Math.log(2);
    }
    
    private static double normalize(double value, double max)
    {
        return Math.log(value) / Math.log(max);
    }

    private static double flatten(double value)
    {
        return Math.log(value + 1) / Math.log(2);
    }
}