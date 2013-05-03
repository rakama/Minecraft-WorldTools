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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import rakama.worldtools.data.Block;

public class BlockTexture
{
    protected final Block[] data;
    protected final int width, height;
    
    public BlockTexture(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.data = new Block[width*height];
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }
    
    public boolean setBlock(int x, int y, Block block)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return false;

        data[x + y*width] = block;
        
        return true;
    }
    
    public BlockTexture flipHorizontally()
    {
        BlockTexture tex = new BlockTexture(width, height);
        
        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
                tex.setBlock(x, y, getBlockXY(width - x - 1, y));
        
        return tex;
    }

    public BlockTexture flipVertically()
    {
        BlockTexture tex = new BlockTexture(width, height);

        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
                tex.setBlock(x, y, getBlockXY(x, height - y - 1));
        
        return tex;
    }

    public BlockTexture rotate90()
    {
        BlockTexture tex = new BlockTexture(height, width);

        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
                tex.setBlock(x, y, getBlockXY(width - x - 1, y));
        
        return tex;
    }

    public BlockTexture rotate180()
    {
        BlockTexture tex = new BlockTexture(width, height);

        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
                tex.setBlock(x, y, getBlockXY(width - x - 1, height - y - 1));
        
        return tex;
    }

    public BlockTexture rotate270()
    {
        BlockTexture tex = new BlockTexture(height, width);

        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
                tex.setBlock(x, y, getBlockXY(x, height - y - 1));
        
        return tex;
    }
    
    public Block getBlockUV(double u, double v)
    {
        int x = (int)(u * width);
        int y = (int)(v * height);
        
        return getBlockXY(x, y);
    }

    public Block getBlockXY(int x, int y)
    {
        if(x < 0 || x >= width || y < 0 || y >= height)
            return null;        
        
        return data[x + y*width];
    }
    
    public static BlockTexture fromImage(BufferedImage img)
    {
        return fromImage(img, ColorProfile.getDefaultProfile());
    }

    public static BlockTexture fromImage(BufferedImage img, ColorProfile cpf)
    {
        int width = img.getWidth();
        int height = img.getHeight();

        BufferedImage transfer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        transfer.getGraphics().drawImage(img, 0, 0, null);
        
        BlockTexture tex = new BlockTexture(width, height);        
        WritableRaster alpha = transfer.getAlphaRaster();
        
        for(int x=0; x<width; x++)
            for(int y=0; y<height; y++)
                if(alpha == null || alpha.getSample(x, y, 0) > 32)
                    tex.setBlock(x, y, cpf.getBlock(transfer.getRGB(x, y)));
            
        return tex;
    }
}
