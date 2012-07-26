package com.github.rakama.minecraft.tools.loc;

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

public final class Coordinate3D implements Comparable<Coordinate3D>
{
    public final int x, y, z;

    public Coordinate3D(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getDistance(Coordinate3D c)
    {
        return Math.sqrt(getSquaredDistance(c));
    }

    public double getSquaredDistance(Coordinate3D c)
    {
        double dx = x - c.x;
        double dy = y - c.y;
        double dz = z - c.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double getManhattanDistance(Coordinate3D c)
    {
        return Math.abs(x - c.x) + Math.abs(y - c.y) + Math.abs(z - c.z);
    }

    public String toString()
    {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public int compareTo(Coordinate3D l)
    {
        if(y != l.y)
            return y - l.y;
        else if(z != l.z)
            return z - l.z;
        else if(x != l.x)
            return x - l.x;
        else
            return 0;
    }

    public boolean equals(Object o)
    {
        return equals((Coordinate3D) o);
    }

    public boolean equals(Coordinate3D c)
    {
        return c.x == x && c.y == y && c.z == z;
    }

    public int hashCode()
    {
        return x ^ y ^ z;
    }
}