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

package rakama.worldtools.examples.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rakama.worldtools.WorldManager;
import rakama.worldtools.canvas.WorldCanvas;
import rakama.worldtools.data.Entity;
import rakama.worldtools.data.Schematic;
import rakama.worldtools.data.TileEntity;
import rakama.worldtools.data.entity.CommandBlock;
import rakama.worldtools.data.entity.EntityFactory;

public class Exporter
{      
    static boolean copy_biomes = true;

    public static void main(String[] args) throws IOException
    {
        List<String> list = new ArrayList<String>(Arrays.asList(args));  
        if(list.size() < 11)
        {
            System.err.println("Error: Incorrect number of arguments.");
            System.exit(0);
        }
        
        String src, dest;
        src = dest = null;
        int xSrc, ySrc, zSrc; 
        int width, height, length;
        int xDest, yDest, zDest;
        xSrc = ySrc = zSrc = 0;
        width = height = length = 0;
        xDest = yDest = zDest = 0;
        
        try
        {
            src = list.get(0);
            xSrc = Integer.parseInt(list.get(1));
            ySrc = Integer.parseInt(list.get(2));
            zSrc = Integer.parseInt(list.get(3));
            width = Integer.parseInt(list.get(4));
            height = Integer.parseInt(list.get(5));
            length = Integer.parseInt(list.get(6));
            dest = list.get(7);
            xDest = Integer.parseInt(list.get(8));
            yDest = Integer.parseInt(list.get(9));
            zDest = Integer.parseInt(list.get(10));
        }
        catch(Exception e)
        {
            System.err.println("Error: Unable to parse arguments.");
            System.err.println(e);
            System.exit(0);
        }

        if(width < 0){xSrc += width; width = -width;}
        if(height < 0){ySrc += height; height = -height;}
        if(length < 0){zSrc += length; length = -length;}

        File srcFile = new File(src);
        File destFile = new File(dest);
                
        WorldManager destManager = WorldManager.getWorldManager(new File(dest));
        WorldManager srcManager = destManager;
        
        if(!srcFile.equals(destFile))
            srcManager = WorldManager.getWorldManager(new File(src), true);

        WorldCanvas srcCanvas = srcManager.getCanvas();
        WorldCanvas destCanvas = destManager.getCanvas();
        
        // export schematic from source map
        Schematic schema = srcCanvas.exportSchematic(xSrc, ySrc, zSrc, 
                            xSrc+width-1, ySrc+height-1, zSrc+length-1); 

        int dx = xSrc - xDest;
        int dy = ySrc - yDest;
        int dz = zSrc - zDest;

        // regular expression for tp commands
        String space = "\\s+?"; 
        String target = "(@[prafPRAF](?:\\[.*\\])*?)";
        String number = "(~?-?[\\p{Digit}]+?)";
        String tpstr = "/tp" + space + target + space + number 
                        + space + number + space + number + "\\s*?";

        // regular expression for general commands
        String argstr = "/([^\\s]*)" + space + "(.*)";
        String argsplit = "(@[prafPRAF])(\\[.*\\])*?)";
        
        Pattern apattern = Pattern.compile(argstr);
        Pattern tpattern = Pattern.compile(tpstr);

        // edit command block teleport coordinates    
        List<TileEntity> tileEntities = schema.getTileEntities();
        for(TileEntity e : new ArrayList<TileEntity>(tileEntities))
        {
            if(e instanceof CommandBlock)
            {
                CommandBlock cb = (CommandBlock)e;
                int xc = cb.getX();
                int yc = cb.getY();
                int zc = cb.getZ();
                String cmd = cb.getCommand();
                
                if(cmd == null)
                    continue;                

                Matcher tmatcher = tpattern.matcher(cmd);
                
                if(tmatcher.matches())
                {
                    String tar = tmatcher.group(1);
                    String xstr = tmatcher.group(2);
                    String ystr = tmatcher.group(3);
                    String zstr = tmatcher.group(4);
                                        
                    int xt = Integer.parseInt(xstr.replace("~", "")) - dx;
                    int yt = Integer.parseInt(ystr.replace("~", "")) - dy;
                    int zt = Integer.parseInt(zstr.replace("~", "")) - dz;
                    
                    xstr = xstr.startsWith("~") ? xstr : Integer.toString(xt);
                    ystr = ystr.startsWith("~") ? ystr : Integer.toString(yt);
                    zstr = zstr.startsWith("~") ? zstr : Integer.toString(zt);         
                        
                    cmd = "/tp " + tar + " " + xt + " " + yt + " " + zt;
                }
                
                Matcher amatcher = apattern.matcher(cmd);
                
                if(amatcher.matches())
                {
                    String arg = tmatcher.group(1);
                    String val = tmatcher.group(2);
                    
                    // TODO: split val into tokens, fix the xyz ones
                }
                else
                    continue;
                        
                cb = EntityFactory.getDefaultFactory().createCommandBlock(xc, yc, zc, cmd);
                schema.removeTileEntity(e);
                schema.addTileEntity(cb);
            }
        }
        
        // remove existing entities from target region
        List<Entity> removeEntities = destCanvas.getEntities(xDest, yDest, zDest,
                xDest+width-1, yDest+height-1, zDest+length-1);
        for(Entity e : new ArrayList<Entity>(removeEntities))
            destCanvas.removeEntity(e);

        // remove existing tile entities from target region
        List<TileEntity> removeTiles = destCanvas.getTileEntities(xDest, yDest, zDest,
                xDest+width-1, yDest+height-1, zDest+length-1);
        for(TileEntity e : new ArrayList<TileEntity>(removeTiles))
            destCanvas.removeTileEntity(e);
        
        // import schematic to destination map
        destCanvas.importSchematic(xDest, yDest, zDest, schema);
        
        // copy biomes from source to destination
        if(copy_biomes)
        {
            for(int z=zSrc; z<zSrc+length; z++)
            {
                for(int x=xSrc; x<xSrc+width; x++)
                {
                    int biome = srcCanvas.getBiome(x, z);
                    if(biome < 0)
                        biome = 0;
                    
                    destCanvas.setBiome(x - dx, z - dz, biome);
                }
            }
        }
        
        destManager.closeAll();
        srcManager.closeAll();
        
        System.out.println("Success...");
    }
}