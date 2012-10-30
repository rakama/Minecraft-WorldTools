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

package com.github.rakama.examples.export;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.rakama.worldtools.WorldManager;
import com.github.rakama.worldtools.WorldTools;
import com.github.rakama.worldtools.canvas.BlockCanvas;
import com.github.rakama.worldtools.data.Entity;
import com.github.rakama.worldtools.data.Schematic;
import com.github.rakama.worldtools.data.TileEntity;
import com.github.rakama.worldtools.data.entity.CommandBlock;
import com.github.rakama.worldtools.data.entity.EntityFactory;

public class Exporter
{
	public static void main(String[] args) throws IOException
	{
		if(args.length < 11)
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
			src = args[0];
			xSrc = Integer.parseInt(args[1]);
			ySrc = Integer.parseInt(args[2]);
			zSrc = Integer.parseInt(args[3]);
			width = Integer.parseInt(args[4]);
			height = Integer.parseInt(args[5]);
			length = Integer.parseInt(args[6]);
			dest = args[7];
			xDest = Integer.parseInt(args[8]);
			yDest = Integer.parseInt(args[9]);
			zDest = Integer.parseInt(args[10]);
		}
		catch(Exception e)
		{
            System.err.println("Error: Unable to parse arguments.");
            System.err.println(e);
            System.exit(0);
		}

        WorldManager srcManager = WorldTools.getWorldManager(new File(src));
        WorldManager destManager = WorldTools.getWorldManager(new File(dest));
        BlockCanvas srcCanvas = srcManager.getCanvas();
        BlockCanvas destCanvas = destManager.getCanvas();
        
        // export schematic from source map
        Schematic schema = srcCanvas.exportSchematic(xSrc, ySrc, zSrc, 
        					xSrc+width-1, ySrc+height-1, zSrc+length-1); 

		int dx = xSrc - xDest;
		int dy = ySrc - yDest;
		int dz = zSrc - zDest;
		
		// create regular expression for tp commands
		String space = "\\s+?"; 
        String target = "(@[prafPRAF](?:\\[.*\\])*?)";
        String number = "(-?[\\p{Digit}]+?)";
        String tpstr = "/tp" + space + target + space + number 
    					+ space + number + space + number + "\\s*?";        
		Pattern pattern = Pattern.compile(tpstr);

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
        		      
        		Matcher matcher = pattern.matcher(cmd);
        		
        		if(matcher.matches())
        		{
        			String tar = matcher.group(1);
        			int xt = Integer.parseInt(matcher.group(2)) - dx;
        			int yt = Integer.parseInt(matcher.group(3)) - dy;
        			int zt = Integer.parseInt(matcher.group(4)) - dz;
        			cmd = "/tp " + tar + " " + xt + " " + yt + " " + zt;
        		}
                		
        		cb = EntityFactory.createCommandBlock(xc, yc, zc, cmd);
        		schema.removeTileEntity(e);
        		schema.addTileEntity(cb);
        	}
        }
        
        // remove existing entities from target region
        List<Entity> removeEntities = destCanvas.getEntities(
        		xDest, yDest, zDest,
        		xDest+width-1, yDest+height-1, zDest+length-1);
        for(Entity e : new ArrayList<Entity>(removeEntities))
        	destCanvas.removeEntity(e);

        // remove existing tile entities from target region
        List<TileEntity> removeTiles = destCanvas.getTileEntities(
        		xDest, yDest, zDest,
        		xDest+width-1, yDest+height-1, zDest+length-1);
        for(TileEntity e : new ArrayList<TileEntity>(removeTiles))
        	destCanvas.removeTileEntity(e);
        
        // import schematic to destination map
        destCanvas.importSchematic(xDest, yDest, zDest, schema);
        
        destManager.closeAll();
        srcManager.closeAll();
        
        System.out.println("Success...");
	}
}