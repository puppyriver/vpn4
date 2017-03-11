package infox.vpn4.modules.oslog;

import infox.vpn4.util.VendorUtil;
import infox.vpn4.valueobject.FAlarmItem;
import infox.vpn4.valueobject.FAlarmItemAsso;
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
    private final Integer sampleTimeWindowInMin = 5;

    private AlarmItemMgr alarmItemMgr = null;
    private AlarmEventRepository alarmEventRepository = null;
    private AlarmAssoMgr alarmAssoMgr = null;

    public OSTupleCollector(AlarmItemMgr alarmItemMgr,AlarmAssoMgr alarmAssoMgr, AlarmEventRepository alarmEventRepository) {
        this.alarmItemMgr = alarmItemMgr;
        this.alarmEventRepository = alarmEventRepository;
        this.alarmAssoMgr = alarmAssoMgr;
    }

    public void offer(OSTuple alarm ) {
        if (buffer == null) {
            buffer = new TimeWindowBuffer<OSTuple>(timeWindowInSecond,
                    (list,startTime)-> save(list,startTime));
        }
        if (alarm == null || !alarm.valid()) {
            return;
        }

        buffer.push(alarm.getAlarmTime(),alarm);
    }

    private void save(FAlarmRecord record) {
        String key = record.getAlarmItemDn();
        OSTuple osTuple = OSTuple.parseKey(key);

        FAlarmItem item = new FAlarmItem(key, VendorUtil.getVendorDn(osTuple.getVendorName()), osTuple.getDomainName(), osTuple.getAlarmName(),record.getSeverity(), record.getOccurCount());

        FAlarmItem alarmItem = alarmItemMgr.add(item);
        try {
            analysis(alarmItem ,record);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        alarmEventRepository.insert(record);
    }

    private void analysis(FAlarmItem alarmItem,FAlarmRecord record) throws Exception {
        Date time = record.getAlarmTime();
        Date startTime = new Date(time.getTime() -  sampleTimeWindowInMin * 60l * 1000l);
        Date endTime = new Date(time.getTime() +  sampleTimeWindowInMin * 60l * 1000l);
        List<FAlarmRecord> hisRecords = alarmEventRepository.query(startTime, endTime);
        for (FAlarmRecord hisRecord : hisRecords) {
            if (!record.getAlarmItemDn().equals(hisRecord.getAlarmItemDn())) {
                FAlarmItemAsso asso = new FAlarmItemAsso();
                FAlarmItem bItem = alarmItemMgr.getByDn(hisRecord.getAlarmItemDn());
                asso.setAlarmItemAId(alarmItem.getId());
                asso.setAlarmItemBId(bItem.getId());
                asso.setAlarmItemAName(alarmItem.getName());
                asso.setAlarmItemBName(bItem.getName());
                asso.setCount(record.getOccurCount() * hisRecord.getOccurCount());
                alarmAssoMgr.updateAlarmAsso(asso);
            }
        }
    }

    private void save(List<OSTuple> list, Date startTime) {
        Map<String, List<FAlarmRecord>> collect = list.stream()
                .map(ele -> new FAlarmRecord(ele.getItemKey(), startTime, timeWindowInSecond, ele.getSeverity(),1))
                .collect(Collectors.groupingBy(r -> r.getAlarmItemDn()));

        collect.forEach((key,rs)->{
            if (rs.size() > 100)
                logger.info(list.size()+"");
            FAlarmRecord record = rs.get(0);
            record.setOccurCount(rs.size());
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
            else {
                if (time.getTime() < startTime.getTime()) {
                    startTime = null;
                    elements.clear();
                    return;
                }
                else if (time.getTime() - startTime.getTime() > timeWindowInSecond * 1000l) {
                    consumer.accept(elements, startTime);
                    elements.clear();
                    startTime = null;
                }
            }
            elements.add(t);
        }
    }
}
