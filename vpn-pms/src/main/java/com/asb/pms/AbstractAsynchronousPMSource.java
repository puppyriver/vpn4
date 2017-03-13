package com.asb.pms;

import com.asb.pms.model.PM_DATA;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/22
 * Time: 19:23
 * rongrong.chen@alcatel-sbell.com.cn
 */
public abstract class AbstractAsynchronousPMSource implements PMSource,Runnable {
    protected Log logger = LogFactory.getLog(getClass());

    private String name = this.toString();

    public void setName(String name) {
        this.name = name;
    }

    public void start() {
        new Thread(this).start();
    }
    @Override
    public String getName() {
        return name;
    }

    protected LinkedBlockingQueue<PM_DATA> queue = new LinkedBlockingQueue<>();

    public void addToQueue(PM_DATA pm_data) {
        queue.add(pm_data);
    }

    @Override
    public PM_DATA take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public List<PM_DATA> takeList(int maxSize) throws InterruptedException {
        List<PM_DATA> list = new ArrayList<>();
        if (queue.size() <= maxSize) {
            for (Object o : queue.toArray()) {
                if (o instanceof PM_DATA)
                    list.add((PM_DATA) o);
            }

        }
        else {
            for (int i = 0 ; i < maxSize; i++)
                list.add(queue.take());
        }

        return list;

    }
}
