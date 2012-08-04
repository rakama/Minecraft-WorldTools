package com.github.rakama.worldtools.coord;

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

public class Coordinate2D implements Comparable<Coordinate2D>
{
    public final int x, z;

    public Coordinate2D(int x, int z)
    {
        this.x = x;
        this.z = z;
    }

    public double getDistance(Coordinate2D c)
    {
        return Math.sqrt(getSquaredDistance(c));
    }

    public double getSquaredDistance(Coordinate2D c)
    {
        double dx = x - c.x;
        double dz = z - c.z;
        return dx * dx + dz * dz;
    }

    public double getManhattanDistance(Coordinate2D c)
    {
        return Math.abs(x - c.x) + Math.abs(z - c.z);
    }

    public String toString()
    {
        return "(" + x + ", " + z + ")";
    }

    public int compareTo(Coordinate2D l)
    {
        if(z != l.z)
            return z - l.z;
        else if(x != l.x)
            return x - l.x;
        else
            return 0;
    }

    public boolean equals(Object o)
    {
        if(o == null)
            return false;
        else
            return equals((Coordinate2D) o);
    }

    public boolean equals(Coordinate2D c)
    {
        return c.x == x && c.z == z;
    }

    public int hashCode()
    {
        return x ^ z;
    }
}