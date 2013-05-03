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

package rakama.worldtools.util;

import java.util.Arrays;

public class NibbleArray
{
    public final byte[] array;
    private final int size;

    public NibbleArray(int size)
    {
        this.size = size;
        this.array = new byte[size / 2 + size % 2];
    }

    public NibbleArray(byte[] data)
    {
        this.size = data.length * 2;
        this.array = data;
    }

    public int size()
    {
        return size;
    }

    public int get(int index)
    {
        if(index < 0 || index > size)
            throw new IndexOutOfBoundsException("index out of bounds " + index);

        return getHalfByte(index, array);
    }

    public void set(int index, int halfbyte)
    {
        if(index < 0 || index > size)
            throw new IndexOutOfBoundsException("index out of bounds " + index);

        setHalfByte(index, halfbyte, array);
    }

    public void fill(int halfbyte)
    {
        halfbyte = halfbyte & 0xF;
        byte pair = (byte) (halfbyte | (halfbyte << 4));
        Arrays.fill(array, pair);
    }

    protected void set(byte[] array)
    {
        int len = Math.min(array.length, this.array.length);
        System.arraycopy(array, 0, this.array, 0, len);
    }

    private void setHalfByte(int offset, int halfbyte, byte[] array)
    {
        int data_offset = offset >> 1;
        byte data = array[data_offset];

        if((offset & 1) > 0)
        {
            data &= 0x0F;
            data |= (byte) ((halfbyte << 4) & 0xF0);
        }
        else
        {
            data &= 0xF0;
            data |= (byte) (halfbyte & 0x0F);
        }

        array[data_offset] = data;
    }

    private int getHalfByte(int offset, byte[] array)
    {
        int data_offset = offset >> 1;
        byte data = array[data_offset];

        if((offset & 1) > 0)
            return (data & 0xF0) >> 4;
        else
            return data & 0x0F;
    }
}