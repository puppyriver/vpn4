package com.asb.pms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/13
 * Time: 15:20
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class OrderedConcurrentHashMap<K, V>  extends ConcurrentHashMap<K, V> {
    private Logger logger = LoggerFactory.getLogger(OrderedConcurrentHashMap.class);
    private List<K> order = new ArrayList<K>();
    public V put(K key, V value)
    {
        if (!order.contains(key))
            order.add(key);
//        if (keepSorted)
//            sort();
        return super.put(key, value);
    }

    public V put(K key, V value, int position)
    {
        if (!order.contains(key))
            order.add(position, key);

        return super.put(key, value);
    }

    public void putAll(HashMap<K, V> map)
    {
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    public V get(int index)
    {
        return super.get(order.get(index));
    }

    public K getKey(int index)
    {
        return order.get(index);
    }

    public V remove(Object key)
    {
        order.remove(key);
        return super.remove(key);
    }

    public V remove(int index)
    {
        K key = order.remove(index);
        return super.remove(key);
    }

    public void clear()
    {
        super.clear();
        this.order.clear();
    }

    public synchronized V peekFirst() {
        if (order.size() > 0) {
            K key = order.get(0);
            order.remove(0);
            return remove(key);
        }
        return null;
    }

    public static void main(String[] args) {
        OrderedConcurrentHashMap map = new OrderedConcurrentHashMap();
        map.put("1","1");
        map.put("2","1");
        map.put("3","1");
        map.put("4","1");
        Object o = map.peekFirst();
          o = map.peekFirst();
        System.out.println("o = " + map);
    }






}
