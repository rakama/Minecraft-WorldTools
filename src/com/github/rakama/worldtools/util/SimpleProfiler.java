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

package com.github.rakama.worldtools.util;

import java.util.Arrays;

public class SimpleProfiler
{
    private final long[] milliseconds;
    private long initTime, modeTime;
    private int numModes, currentMode, defaultMode;

    public SimpleProfiler(int numModes)
    {
        this(numModes, 0);
    }

    public SimpleProfiler(int numModes, int defaultMode)
    {
        this.initTime = System.currentTimeMillis();
        this.modeTime = initTime;
        this.numModes = numModes;
        this.milliseconds = new long[numModes];
        this.currentMode = defaultMode;
        this.defaultMode = defaultMode;
    }
    
    public void setMode(int mode)
    {
        if(mode < 0 || mode >= numModes)
            throw new IndexOutOfBoundsException(Integer.toString(mode));
        
        updateModeTime();
        currentMode = mode;
    }

    private void updateModeTime()
    {
        long time = System.currentTimeMillis();
        milliseconds[currentMode] += time - modeTime;
        modeTime = time;
    }

    public int getMode()
    {
        return currentMode;
    }

    public long getMilliseconds(int mode)
    {
        if(mode < 0 || mode >= numModes)
            throw new IndexOutOfBoundsException(Integer.toString(mode));
        
        if(mode == currentMode)
            updateModeTime();

        return milliseconds[mode];
    }

    public long getMilliseconds()
    {
        return System.currentTimeMillis() - initTime;
    }

    public int getNumModes()
    {
        return numModes;
    }

    public void reset()
    {
        Arrays.fill(milliseconds, 0);
        initTime = System.currentTimeMillis();
        modeTime = initTime;
        currentMode = defaultMode;
    }
}