package infox.vpn4.modules.oslog;

import infox.vpn4.valueobject.FAlarmItem;
import infox.vpn4.valueobject.FAlarmRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Administrator on 2017/3/2.
 */
public class OSTupleCollector {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private TimeWindowBuffer buffer = null;
    private final Integer timeWindowInSecond = 60;

    private AlarmItemMgr alarmItemMgr = null;

    public void offer(OSTuple alarm ) {
        if (buffer == null) {
            buffer = new TimeWindowBuffer<OSTuple>(timeWindowInSecond,
                    (list,startTime)-> save(list,startTime));
        }
        if (alarm == null || !alarm.valid()) {
            return;
        }

        alarm.getAlarmTime();
    }

    private void save(FAlarmRecord record) {
        String key = record.getAlarmItemDn();
        OSTuple osTuple = OSTuple.parseKey(key);
        alarmItemMgr.add(new FAlarmItem(osTuple.getVendorName(),osTuple.getDomainName(),osTuple.getAlarmName(),record.getOccurCount()));
    }

    private void save(List<OSTuple> list, Date startTime) {
        Map<String, List<FAlarmRecord>> collect = list.stream()
                .map(ele -> new FAlarmRecord(ele.getItemKey(), startTime, timeWindowInSecond, 1))
                .collect(Collectors.groupingBy(r -> r.getAlarmItemDn()));

        collect.forEach((key,rs)->{
            FAlarmRecord record = rs.get(0);
            record.setOccurCount(list.size());
            save(record);
        });
    }


    class TimeWindowBuffer<T> {
        private Integer timeWindowInSecond ;
        private Date startTime;
        private Date endTime;
        private BiConsumer<List<T>,Date> consumer = null;
        private List<T> elements = new ArrayList<T>();

        public TimeWindowBuffer(Integer timeWindowInSecond, BiConsumer<List<T>,Date>  consumer) {
            this.timeWindowInSecond = timeWindowInSecond;
            this.consumer = consumer;
        }

        public void push(Date time,T t) {
            if (startTime == null)
                startTime = time;
            else
                if (time.getTime() - startTime.getTime() > timeWindowInSecond * 1000l) {
                    consumer.accept(elements,startTime);
                    elements.clear();
                }
            elements.add(t);
        }
    }
}
