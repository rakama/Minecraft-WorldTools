package com.github.rakama.minecraft.chunk;

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

public class Block
{
    static String materials_file = "materials.csv";

    static boolean[] transparency;
    static int[] diffusion;
    static int[] luminance;

    public static void initializeMaterials() throws IOException
    {
        loadMaterials(new File(materials_file));
    }

    public static void loadMaterials(File file) throws IOException
    {
        transparency = new boolean[256];
        diffusion = new int[256];
        luminance = new int[256];

        BufferedReader reader = new BufferedReader(new FileReader(file));
        reader.readLine(); // labels
        String line = reader.readLine();
        while(line != null)
        {
            loadEntry(line.split(","));
            line = reader.readLine();
        }
    }

    protected static void loadEntry(String... vals)
    {
        try
        {
            int id = Integer.parseInt(vals[0].trim());
            int trs = Integer.parseInt(vals[2].trim());
            int dif = Integer.parseInt(vals[3].trim());
            int lum = Integer.parseInt(vals[4].trim());

            if(trs > 0)
                setTransparent(id);

            setDiffusion(id, dif);
            setLuminance(id, lum);
        }
        catch(NumberFormatException e)
        {
            System.err.println(e);
        }
    }

    protected static void setDiffusion(int id, int dif)
    {
        diffusion[id] = dif;
    }

    protected static void setTransparent(int id)
    {
        transparency[id] = true;
    }

    protected static void setLuminance(int id, int lum)
    {
        luminance[id] = lum;
    }

    public static boolean isAir(int id)
    {
        return id == 0;
    }

    public static boolean isTransparent(int id)
    {
        if(id < 0 || id >= transparency.length)
            return true;

        return transparency[id];
    }

    public static boolean isOpaque(int id)
    {
        if(id < 0 || id >= transparency.length)
            return true;

        return !transparency[id];
    }

    public static int getDiffusion(int id)
    {
        if(id < 0 || id >= diffusion.length)
            return 0;

        return diffusion[id];
    }

    public static int getLuminance(int id)
    {
        if(id < 0 || id >= luminance.length)
            return 0;

        return luminance[id];
    }
}