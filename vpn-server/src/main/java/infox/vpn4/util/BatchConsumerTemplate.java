package infox.vpn4.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Ronnie
 * Date: 12-6-13
 * Time: 下午1:58
 */
public abstract class BatchConsumerTemplate<T> extends Thread{

    private long delayTimeMilis = 1000;
    private int batchSize = 1000;
    protected Log logger = LogFactory.getLog(getClass());
    private LinkedBlockingQueue<T> queue = new LinkedBlockingQueue();
    private int capacity = -1;

    public BatchConsumerTemplate(int batchSize) {
        this.batchSize = batchSize;
    }
    public BatchConsumerTemplate(int batchSize,int capacity) {
        this.batchSize = batchSize;
        this.capacity = capacity;
    }
    public BatchConsumerTemplate() {

    }

    public void offerOne(T object ) {
        if (capacity < 0 || capacity > queue.size())
            queue.offer(object);
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        while (true) {
            try {
                T firstEvent = queue.take();
                int fetchSize = queue.size() > batchSize ? batchSize : queue.size();
                List<T> events = new ArrayList();
                events.add(firstEvent);
                for (int i = 0; i < fetchSize; i++) {
                    events.add(queue.take());
                }

                processObjects(events);

            } catch (Throwable e) {
                logger.error(e,e);
            }

        }

    }

    protected abstract void processObjects(List<T> events);


}
