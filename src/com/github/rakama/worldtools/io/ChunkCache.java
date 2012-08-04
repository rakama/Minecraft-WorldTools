package com.github.rakama.worldtools.io;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import com.github.rakama.worldtools.coord.Coordinate2D;

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

class ChunkCache
{    
    static Random rand = new Random(0);
    static double cache_ratio = 0.8;
    static double low_priority_ratio = 0.05;
    
    ChunkManager manager;
    PriorityQueue<PriorityChunk> queue;
    HashMap<ChunkID, PriorityChunk> cache;
    HashMap<ChunkID, WeakChunkReference> weak;
    int maxCapacity, lowPriority, highPriority;

    public ChunkCache(ChunkManager manager, int maxCapacity)
    {
        this.manager = manager;
        this.maxCapacity = maxCapacity;
        this.weak = new LinkedHashMap<ChunkID, WeakChunkReference>();
        this.cache = new LinkedHashMap<ChunkID, PriorityChunk>();
        this.queue = new PriorityQueue<PriorityChunk>();
        this.highPriority = maxCapacity;
        this.lowPriority = (int)(maxCapacity * low_priority_ratio);
    }
    
    public TrackedChunk get(int x, int z, boolean priority)
    {
        ChunkID key = new ChunkID(x, z);
        PriorityChunk pc = cache.get(key);
        refresh(pc, priority);
        
        if(pc == null)
            return recoverWeakReference(key, priority);
        else
            return pc.chunk;
    }

    public void put(TrackedChunk chunk, boolean priority)
    {
        int p = priority ? highPriority : lowPriority;
        PriorityChunk pc = PriorityChunk.getPriorityChunk(chunk, p);        
        queue.add(pc);
        cache.put(chunk.getID(), pc);
        balanceCache();
    }
        
    public void refresh(TrackedChunk chunk, boolean priority)
    {
        PriorityChunk pc = cache.get(chunk.getID());
        
        if(pc == null)
            return;

        int p = priority ? highPriority : lowPriority;
        pc.priority = p + PriorityChunk.getCounter();
    }

    private void refresh(PriorityChunk pc, boolean priority)
    {
        if(pc == null)
            return;

        int p = priority ? highPriority : lowPriority;
        pc.priority = p + PriorityChunk.getCounter();
    }
    
    public int size()
    {
        return queue.size();
    }
    
    private TrackedChunk recoverWeakReference(ChunkID key, boolean priority)
    {
        WeakChunkReference wref = weak.remove(key);
        if(wref == null)
            return null;
                        
        TrackedChunk chunk = wref.get();                
        if(chunk == null)
            return null;
        
        // promote to normal cache
        put(chunk, priority);        
        return chunk;
    }
    
    public void clear()
    {
        for(PriorityChunk pc : queue)
            if(pc.chunk != null)
                weak.put(pc.chunk.getID(), new WeakChunkReference(pc.chunk));

        cleanupWeakReferences();            
        cache.clear();
        queue.clear();
    }
    
    private void cleanupWeakReferences()
    {
        Iterator<WeakChunkReference> iter = weak.values().iterator();
        while(iter.hasNext())
            if(iter.next().get() == null)
                iter.remove();
    }

    public Iterator<TrackedChunk> getChunkIterator()
    {
        List<PriorityChunk> list = new ArrayList<PriorityChunk>(queue);
        return new PriorityChunkIterator(list.iterator()); 
    }
    
    public Iterator<WeakChunkReference> getWeakIterator()
    {
        return new ArrayList<WeakChunkReference>(weak.values()).iterator(); 
    }
    
    private void balanceCache()
    {       
        double safeSize = maxCapacity * cache_ratio;
        double threshold = 0;

        if(cache.size() < safeSize)
            return;
            
        threshold = (cache.size() - safeSize) / (maxCapacity - safeSize);

        if(rand.nextDouble() > threshold)
            return;
                
        for(int i=0; i<2 && !queue.isEmpty(); i++)
        {
            TrackedChunk eldest = queue.remove().chunk;                
            cache.remove(eldest.getID());
            weak.put(eldest.getID(), new WeakChunkReference(eldest));
            if(eldest.isDirty())
                manager.requestCleanup(eldest);
        }
    }
}

final class PriorityChunk implements Comparable<PriorityChunk>
{
    private static long counter;
    
    public final TrackedChunk chunk;
    public long priority;
    
    private PriorityChunk(TrackedChunk chunk, long priority)
    {
        this.chunk = chunk;
        this.priority = priority;
    }
    
    public static PriorityChunk getPriorityChunk(TrackedChunk chunk, int priority)
    {
        counter++;
        return new PriorityChunk(chunk, priority + counter);
    }

    public static long getCounter()
    {
        return counter;
    }
    
    public int compareTo(PriorityChunk pc)
    {
        if(pc.priority < priority)
            return 1;
        else if(pc.priority > priority)
            return -1;
        else
            return 0;
    }
}

final class PriorityChunkIterator implements Iterator<TrackedChunk>
{
    Iterator<PriorityChunk> iter;    
    
    public PriorityChunkIterator(Iterator<PriorityChunk> iter)
    {
        this.iter = iter;
    }
    
    public boolean hasNext()
    {
        return iter.hasNext();
    }

    public TrackedChunk next()
    {
        return iter.next().chunk;
    }

    public void remove()
    {
    }
}

final class WeakChunkReference extends WeakReference<TrackedChunk>
{
    public WeakChunkReference(TrackedChunk chunk)
    {
        super(chunk);
    }
}

final class ChunkID extends Coordinate2D
{
    public ChunkID(int x, int z)
    {
        super(x, z);
    }
}