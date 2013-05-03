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

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class PriorityCache<K, V>
{
    static final double DEFAULT_FILL_RATIO = 0.9;
    static final boolean DEFAULT_WEAK_REFERENCES = false;
    static final int DEFAULT_CLEANUP_THRESHOLD = 1000;

    int maxCapacity, cleanupThreshold, removedSinceCleanup;
    double fillRatio;
    long decay;
    
    Queue<Entry<K, V>> queue;
    Map<K, Entry<K, V>> cache;
    Map<K, WeakReference<V>> weak;
    
    public PriorityCache(int maxCapacity)
    {
        this(maxCapacity, DEFAULT_FILL_RATIO, DEFAULT_WEAK_REFERENCES, DEFAULT_CLEANUP_THRESHOLD);
    }

    public PriorityCache(int maxCapacity, double fillRatio)
    {
        this(maxCapacity, fillRatio, DEFAULT_WEAK_REFERENCES, DEFAULT_CLEANUP_THRESHOLD);
    }

    public PriorityCache(int maxCapacity, double fillRatio, boolean weakReferences)
    {
        this(maxCapacity, fillRatio, weakReferences, DEFAULT_CLEANUP_THRESHOLD);
    }
        
    public PriorityCache(int maxCapacity, double fillRatio, boolean weakReferences, int cleanupThreshold)
    {
        this.maxCapacity = maxCapacity;
        this.fillRatio = fillRatio;
        this.cleanupThreshold = cleanupThreshold;
        
        queue = new PriorityQueue<Entry<K, V>>();
        cache = new LinkedHashMap<K, Entry<K, V>>();
        
        if(weakReferences)
            weak = new LinkedHashMap<K, WeakReference<V>>();
    }
    
    public V get(K key, int priority)
    {
        Entry<K, V> prev = cache.get(key);

        if(prev == null)
            prev = getWeakAndPromote(key, priority);
        else
            prev.refresh(prev.getValue(), priority + decay);
        
        if(prev == null)
            return null;
        else
            return prev.getValue();
    }
    
    protected Entry<K, V> getWeakAndPromote(K key, int priority)
    {
        if(weak == null)
            return null;
        
        WeakReference<V> ref = weak.remove(key);
        
        if(ref == null)
            return null;

        V value = ref.get();
            
        if(value == null)
            return null;
        
        Entry<K, V> entry = new Entry<K, V>(key, value, priority + decay);
        cache.put(key, entry);
        queue.add(entry);
        balanceCache();
        
        return entry;
    }
        
    public void refresh(K key, int priority)
    {
        Entry<K, V> prev = cache.get(key);

        if(prev != null)
            prev.refresh(prev.getValue(), priority + decay);
    }
    
    public V put(K key, V value, int priority)
    {
        if(key == null || value == null)
            throw new NullPointerException();
        
        Entry<K, V> prev = cache.get(key); 

        if(prev == null)
            prev = getWeakAndPromote(key, priority);
        else
            prev.refresh(value, priority + decay);
            
        if(prev == null)
        {
            Entry<K, V> entry = new Entry<K, V>(key, value, priority + decay);
            cache.put(key, entry);
            queue.add(entry);
            balanceCache();
            return null;
        }
        else
        {
            return prev.getValue();
        }
    }
    
    public V remove(K key)
    {
        Entry<K, V> prev = cache.remove(key);
        weak.remove(key);
        
        if(prev != null)
        {
            V value = prev.getValue();
            prev.dispose();
            
            removedSinceCleanup++;
            if(removedSinceCleanup > cleanupThreshold)
                clearStaleReferences();
            
            return value;
        }
        else
        {
            return null;
        }
    }
    
    public void decay(int priority)
    {
        decay += priority;
    }
    
    public void clear()
    {   
        cache.clear();
        
        while(!queue.isEmpty())
        {
            Entry<K, V> entry = queue.remove();
            if(entry.isDisposed())
                continue;
            
            expired(entry.getKey(), entry.getValue());  
            weak.put(entry.getKey(), new WeakReference<V>(entry.getValue()));
        }
        
        clearStaleReferences();
    }
    
    public int size()
    {
        return cache.size();
    }
    
    public boolean isEmpty()
    {
        return cache.size() <= 0;
    }
    
    public Collection<Entry<K, V>> getKeyValuePairs()
    {
        return Collections.unmodifiableCollection(cache.values());
    }

    public Collection<WeakReference<V>> getWeakReferences()
    {
        if(weak == null)
            return null;
        
        return Collections.unmodifiableCollection(weak.values());
    }
    
    private void balanceCache()
    {
        double safeSize = maxCapacity * fillRatio;
        double threshold = 0;
        if(cache.size() < safeSize)
            return;
            
        if(fillRatio < 1)
        {
            threshold = (cache.size() - safeSize) / (maxCapacity - safeSize);
            if(Math.random() > threshold)
                return;
        }
        
        int removed = 0;        
        while(removed < 2 && !queue.isEmpty())
        {
            Entry<K, V> entry = queue.remove();
            if(entry.isDisposed())
                continue;
            cache.remove(entry.getKey());
            expired(entry.getKey(), entry.getValue());
            weak.put(entry.getKey(), new WeakReference<V>(entry.getValue()));            
            removed++;
        }
    }
          
    private void clearStaleReferences()
    {
        Iterator<WeakReference<V>> iter = weak.values().iterator();
        while(iter.hasNext())
            if(iter.next().get() == null)
                iter.remove();  
        
        removedSinceCleanup = 0;
    }

    protected void expired(K key, V value)
    {
        // implemented by subclass
    }

    public static class Entry<K, V> implements Comparable<Entry<K, V>>
    {
        private K key;
        private V value;
        private long priority;
        
        public Entry(K key, V value, long priority)
        {
            this.key = key;
            this.value = value;
            this.priority = priority;
        }
        
        public K getKey()
        {
            return key;
        }
        
        public V getValue()
        {
            return value;
        }
        
        private void refresh(V value, long priority)
        {
            if(isDisposed())
                throw new IllegalStateException();
            
            this.value = value;
            this.priority = priority;
        }
               
        private void dispose()
        {
            key = null;
            value = null;
            priority = Long.MIN_VALUE;
        }
        
        public boolean isDisposed()
        {
            return key == null;
        }
    
        public int compareTo(Entry<K, V> e)
        {
            if(e.priority < priority)
                return 1;
            else if(e.priority > priority)
                return -1;
            else
                return 0;
        }
    }
}