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

import java.awt.image.BufferedImage;

import rakama.worldtools.canvas.BlockCanvas;
import rakama.worldtools.tex.BlockTexture;


public class PlayerModel
{
    TexturedBox hat, head, torso, arm_left, arm_right, leg_left, leg_right;
    
    public PlayerModel(BufferedImage skin)
    {   
        hat = new TexturedBox(getHatTextures(skin), 8, 8, 8, true);
        head = new TexturedBox(getHeadTextures(skin), 8, 8, 8);
        torso = new TexturedBox(getTorsoTextures(skin), 8, 12, 4);
        arm_left = new TexturedBox(getArmTextures(skin, true), 4, 12, 4);
        arm_right = new TexturedBox(getArmTextures(skin, false), 4, 12, 4);
        leg_left = new TexturedBox(getLegTextures(skin, true), 4, 12, 4);
        leg_right = new TexturedBox(getLegTextures(skin, false), 4, 12, 4);
    }

    protected BlockTexture[] getHatTextures(BufferedImage skin)
    {
        BlockTexture[] faces = new BlockTexture[6];

        faces[0] = BlockTexture.fromImage(skin.getSubimage(32, 8, 8, 8)).flipVertically();
        faces[1] = BlockTexture.fromImage(skin.getSubimage(40, 8, 8, 8)).flipVertically();
        faces[2] = BlockTexture.fromImage(skin.getSubimage(48, 8, 8, 8)).rotate180();
        faces[3] = BlockTexture.fromImage(skin.getSubimage(56, 8, 8, 8)).rotate180();
        faces[4] = BlockTexture.fromImage(skin.getSubimage(40, 0, 8, 8));
        faces[5] = BlockTexture.fromImage(skin.getSubimage(48, 0, 8, 8)).flipVertically();
        
        return faces;
    }
    
    protected BlockTexture[] getHeadTextures(BufferedImage skin)
    {
        BlockTexture[] faces = new BlockTexture[6];

        faces[0] = BlockTexture.fromImage(skin.getSubimage(0, 8, 8, 8)).flipVertically();
        faces[1] = BlockTexture.fromImage(skin.getSubimage(8, 8, 8, 8)).flipVertically();
        faces[2] = BlockTexture.fromImage(skin.getSubimage(16, 8, 8, 8)).rotate180();
        faces[3] = BlockTexture.fromImage(skin.getSubimage(24, 8, 8, 8)).rotate180();
        faces[4] = BlockTexture.fromImage(skin.getSubimage(8, 0, 8, 8));
        faces[5] = BlockTexture.fromImage(skin.getSubimage(16, 0, 8, 8)).flipVertically();
        
        return faces;
    }

    protected BlockTexture[] getTorsoTextures(BufferedImage skin)
    {
        BlockTexture[] faces = new BlockTexture[6];

        faces[0] = BlockTexture.fromImage(skin.getSubimage(16, 20, 4, 12)).flipVertically();
        faces[1] = BlockTexture.fromImage(skin.getSubimage(20, 20, 8, 12)).flipVertically();
        faces[2] = BlockTexture.fromImage(skin.getSubimage(28, 20, 4, 12)).rotate180();
        faces[3] = BlockTexture.fromImage(skin.getSubimage(32, 20, 8, 12)).rotate180();
        faces[4] = BlockTexture.fromImage(skin.getSubimage(20, 16, 8, 4));
        faces[5] = BlockTexture.fromImage(skin.getSubimage(28, 16, 8, 4)).flipVertically();
        
        return faces;
    }

    protected BlockTexture[] getLegTextures(BufferedImage skin, boolean mirror)
    {
        BlockTexture[] faces = new BlockTexture[6];

        faces[0] = BlockTexture.fromImage(skin.getSubimage(0, 20, 4, 12)).flipVertically();
        faces[1] = BlockTexture.fromImage(skin.getSubimage(4, 20, 4, 12)).flipVertically();
        faces[2] = BlockTexture.fromImage(skin.getSubimage(8, 20, 4, 12)).rotate180();
        faces[3] = BlockTexture.fromImage(skin.getSubimage(12, 20, 4, 12)).rotate180();
        faces[4] = BlockTexture.fromImage(skin.getSubimage(4, 16, 4, 4));
        faces[5] = BlockTexture.fromImage(skin.getSubimage(8, 16, 4, 4)).flipVertically();
        
        if(mirror)
        {   
            BlockTexture temp = faces[0];
            faces[0] = faces[2];
            faces[2] = temp;
            
            faces[1] = faces[1].flipHorizontally();
            faces[3] = faces[3].flipHorizontally();
            
            faces[4] = faces[4].flipHorizontally();
            faces[5] = faces[5].flipHorizontally();
        }
        
        return faces;
    }

    protected BlockTexture[] getArmTextures(BufferedImage skin, boolean mirror)
    {
        BlockTexture[] faces = new BlockTexture[6];

        faces[0] = BlockTexture.fromImage(skin.getSubimage(40, 20, 4, 12)).flipVertically();
        faces[1] = BlockTexture.fromImage(skin.getSubimage(44, 20, 4, 12)).flipVertically();
        faces[2] = BlockTexture.fromImage(skin.getSubimage(48, 20, 4, 12)).rotate180();
        faces[3] = BlockTexture.fromImage(skin.getSubimage(52, 20, 4, 12)).rotate180();
        faces[4] = BlockTexture.fromImage(skin.getSubimage(44, 16, 4, 4));
        faces[5] = BlockTexture.fromImage(skin.getSubimage(48, 16, 4, 4)).flipVertically();

        if(mirror)
        {   
            BlockTexture temp = faces[0];
            faces[0] = faces[2];
            faces[2] = temp;
            
            faces[1] = faces[1].flipHorizontally();
            faces[3] = faces[3].flipHorizontally();
            
            faces[4] = faces[4].flipHorizontally();
            faces[5] = faces[5].flipHorizontally();
        }
        
        return faces;
    }
    
    public void draw(BlockCanvas canvas, int x, int y, int z)
    {
        head.draw(canvas, x + 4, y + 24, z + 1);
        hat.draw(canvas, x + 4, y + 24, z + 1);
        torso.draw(canvas, x + 4, y + 12, z + 3);
        arm_left.draw(canvas, x + 12, y + 12, z + 3);
        arm_right.draw(canvas, x, y + 12, z + 3);
        leg_left.draw(canvas, x + 8, y, z + 3);
        leg_right.draw(canvas, x + 4, y, z + 3);
    }
}
