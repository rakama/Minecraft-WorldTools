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

package rakama.worldtools.examples.skintool;

import rakama.worldtools.canvas.BlockCanvas;
import rakama.worldtools.data.Block;
import rakama.worldtools.tex.BlockTexture;

public class TexturedBox
{
    // right, front, left, back, top, bottom
    BlockTexture[] faces;
    int width, length, height;
    boolean extrude;

    public TexturedBox(BlockTexture[] faces, int w, int h, int l)
    {
        this(faces, w, h, l, false);
    }
    
    public TexturedBox(BlockTexture[] faces, int w, int h, int l, boolean extrude)
    {
        this.faces = faces;
        this.width = w;
        this.height = h;
        this.length = l;
        this.extrude = extrude;
    }
    
    public void draw(BlockCanvas canvas, int x, int y, int z)
    {
        int pad;
        
        if(extrude)
            pad = 1;
        else
            pad = 0;
        
        //bottom
        drawFace(canvas, faces[5], x, y - pad, z, width, 0, length);
        
        //back
        drawFace(canvas, faces[3], x, y, z - pad, width, height, 0);
        
        //right
        drawFace(canvas, faces[0], x - pad, y, z, 0, height, length);
        
        //left
        drawFace(canvas, faces[2], x + width - (1 - pad), y, z, 0, height, length);
        
        //top
        drawFace(canvas, faces[4], x, y + height - (1 - pad), z, width, 0, length);

        //front 
        drawFace(canvas, faces[1], x, y, z + length - (1 - pad), width, height, 0);        
    }
    
    protected void drawFace(BlockCanvas canvas, BlockTexture face, int x, int y, int z, int w, int h, int l)
    {        
        boolean x_minor = w < h && w < l;
        boolean y_minor = h < w && h < l;

        w = Math.max(w, 1);
        h = Math.max(h, 1);
        l = Math.max(l, 1);
        
        for(int i=0; i<w; i++)
        {
            for(int j=0; j<h; j++)
            {
                for(int k=0; k<l; k++)
                {
                    double u, v;
                    
                    if(x_minor)
                    {
                        u = k / (double)length;
                        v = j / (double)height;
                    }
                    else if(y_minor)
                    {
                        u = i / (double)width;
                        v = k / (double)length;
                    }
                    else
                    {
                        u = i / (double)width;
                        v = j / (double)height;
                    }
                    
                    Block block = face.getBlockUV(u, v);
    
                    if(block == null || block.id == Block.AIR.id)
                        continue;

                    canvas.setBlock(x + i, y + j, z + k, block);
                }
            }
        }
    }
}
