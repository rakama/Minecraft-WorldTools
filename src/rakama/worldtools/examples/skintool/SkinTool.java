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
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import rakama.worldtools.data.Schematic;


public class SkinTool
{
    public static void main(String[] args) throws IOException
    {
        if(args.length < 1)
        {
            System.err.println("Error: No skin file specified.");
            System.exit(0);
        }
        else if(args.length < 2)
        {
            System.err.println("Error: No output file specified.");
            System.exit(0);
        }
        
        String in = args[0];
        String out = args[1];
        
        BufferedImage skin = null;
        
        try
        {
            URL url;
            
            try
            {
                url = new URL(in);
            }
            catch(MalformedURLException e)
            {
                File file = new File(in);
                url = file.toURI().toURL();
            }
                
            skin = ImageIO.read(url);
        }
        catch(IOException e)
        {
            System.err.println("Error: Skin file could not be loaded.");
            System.err.println(e.getMessage() + " (" + in + ")");
            System.exit(0);
        }

        Schematic schema = null;
        
        try
        {
            schema = generateSkin(skin);
        }
        catch(IOException e)
        {
            System.err.println("Error: Failed to convert skin file.");
            System.err.println(e.getMessage());
            System.exit(0);
        }
        
        try
        {
            System.out.print("Writing \"" + out + "\" ... ");
            Schematic.saveSchematic(schema, new File(out));
        }
        catch(IOException e)
        {
            System.out.println();
            System.err.println("Error: Failed to save schematic.");
            System.err.println(e.getMessage());
            System.exit(0);
        }

        System.out.println("Success!");
    }
    
    public static Schematic generateSkin(BufferedImage skin) throws IOException
    {
        Schematic sch = new Schematic(16, 33, 10);        
        PlayerModel model = new PlayerModel(skin);
        model.draw(sch, 0, 0, 0);        
        return sch;
    }
}
