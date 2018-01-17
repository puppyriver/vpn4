package com.asb.pms.repository;


import com.asb.pms.Configuration;
import com.asb.pms.Constants;
import com.asb.pms.PMLastestCache;
import com.asb.pms.SqliteDBUtil;
import com.asb.pms.api.pm.PMQueryUtil;
import com.asb.pms.model.PM_DATA;
import com.asb.pms.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/3
 * Time: 13:46
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PmDataRepositorySqliteNoCacheImpl implements PmDataRepository {
    private Logger logger = LoggerFactory.getLogger(PmDataRepositorySqliteNoCacheImpl.class);

//    private OrderedConcurrentHashMap<String,SqliteDataSource> sqliteDBMap = new OrderedConcurrentHashMap();

    public PmDataRepositorySqliteNoCacheImpl() {
        logger.info("Configuration.writeCacheSize = "+Configuration.writeCacheSize);
        logger.info("Configuration.readCacheSize = "+Configuration.readCacheSize);
        logger.info("Configuration.maxQueryRangeInHours = "+Configuration.maxQueryRangeInHours);
        logger.info("Configuration.maxQueryNoExtractInHours = "+Configuration.maxQueryNoExtractInHours);
        logger.info("Configuration.maxSqliteConnections = "+Configuration.maxSqliteConnections);


        template.start();
    }


    private String getPartitionKey(Date date) {
       return  new SimpleDateFormat("yyyyMMddHH").format(date);
        //return DateUtils.getDayString(date);
    }

    private List<String> getPartitionKeys(Date start,Date end) {
        List<String> keys = new ArrayList<>();
        for (long time = start.getTime();  time < end.getTime(); time+= 3600l * 1000l) {
            String key = getPartitionKey(new Date(time));
            if (!keys.contains(key))
                keys.add(key);
        }
        String key = getPartitionKey(end);
        if (!keys.contains(key))
            keys.add(key);

        return keys;

    }

    BatchConsumerTemplate<PM_DATA> template = new BatchConsumerTemplate<PM_DATA>(1000) {
        @Override
        protected void processObjects(List<PM_DATA> events, Queue<PM_DATA> queue) {
            if (!Thread.currentThread().getName().contains("BatchConsumerTemplate2"))
                Thread.currentThread().setName("BatchConsumerTemplate2");
            long t1 = System.currentTimeMillis();
            Map<String, List<PM_DATA>> collect = events.stream()
                    .collect(Collectors.groupingBy(event -> getPartitionKey(event.getTimePoint())));
            collect.forEach((dayString,list)-> {
                synchronized (dayString) {
                    SqliteDataSource sqliteDatasource = getDS(dayString, true, true);
                    try {
                        JdbcTemplate jdbcTemplate = new JdbcTemplate(sqliteDatasource);
                        insertList(jdbcTemplate, "PM_DATA", list);

                    } catch(Exception e){
                        logger.error(e.getMessage(), e);
                    } finally{
                        sqliteDatasource.close();
                    }
                }

            });
            long t = System.currentTimeMillis() - t1;
            if (t > 5000)
                logger.info("processObject size = "+events.size()+" spend time : "+t+"ms, "+queue.size()+" left");

        }
    };

    public  void insertList(JdbcTemplate jdbcTemplate, String tableName, List list) {
        Connection connection = null;

        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            BJdbcUtil.insertObjects(connection,list,"PM_DATA");
            connection.commit();
      //      logger.debug("commit : {} objects",list.size());
        } catch ( Exception e) {
            logger.error(e.getMessage()+" :: datasource:"+jdbcTemplate.getDataSource(), e);
            try {
                connection.rollback();
            } catch (SQLException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }  

    }
    
    @Override
    public void insert(PM_DATA event) {
        template.offerOne(event);
    }






    public void init(JdbcTemplate jdbcTemplate) throws SQLException {
        JdbcTemplateUtil.createTable(jdbcTemplate,PM_DATA.class,"PM_DATA");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(statPointId)");
        jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(timepoint)");

    }


    private SqliteDataSource getDS(String dayString,boolean createIfNotExist,boolean pool) {
        return SqliteDBUtil.getDaySqliteDatasource(dayString,PM_DATA.class,createIfNotExist,jdbcTemplate -> {
                            jdbcTemplate.execute("CREATE INDEX IDX_AR_ITEMDN on PM_DATA(statPointId)");
                            jdbcTemplate.execute("CREATE INDEX IDX_AR_TIMEPOINT on PM_DATA(timePoint)");
                        });
    }

    @Override
    public List<PM_DATA> query(Date startTime,Date endTime,List<String> stpKeys, HashMap queryAttributes) throws Exception {
        logger.info("query :: startTime = {}, endTime = {} ,keys = {}",startTime,endTime,stpKeys == null ? null : stpKeys.stream().reduce((a1,a2)->a1+","+a2));

        if (endTime.before(startTime)) {
            throw new Exception("start time can not before end time");
        }
        if (Configuration.maxQueryRangeInHours > 0 && (endTime.getTime() - startTime.getTime() > 3600l * 1000l * Configuration.maxQueryRangeInHours)) {
            throw new Exception("query time out of range !");
        }

        boolean extract = false;
        if (Configuration.maxQueryNoExtractInHours > 0 && (endTime.getTime() - startTime.getTime() > 3600l * 1000l * Configuration.maxQueryNoExtractInHours)) {
            extract = true;
        }


        long t1 = System.currentTimeMillis();
        String start = getPartitionKey(startTime);
        String end = getPartitionKey(endTime);
        JdbcTemplate jdbcTemplate = null;
        List<PM_DATA> result = null;
//        List<Long> stpIds = stpKeys == null ? new ArrayList<>() :
//                stpKeys.stream().map(key-> StpKey.parse(key).getStpId())
//                        .collect(Collectors.toList());

        String inIds = "("+PMQueryUtil.toEntityIdInString(stpKeys)+")";


        if (start.equals(end)) {
            DataSource ds = getDS(start,false,false);
            jdbcTemplate = new JdbcTemplate(ds);
            List<PM_DATA> list = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where timePoint <= ? and timePoint >= ? and statPointId in "+inIds, endTime, startTime);
           if (!list.isEmpty())
                logger.info("1::Query in {}: start {}, end {} ,result = {}",ds,startTime,endTime,list.size());
            result = list;
        } else {

            List<String> partitionKeys = getPartitionKeys(startTime, endTime);
            result = new ArrayList();
            for (int i = 0; i < partitionKeys.size(); i++) {
                String key = partitionKeys.get(i);
                boolean useCache = true;
                DataSource ds  = getDS(key,false,false);

                if (ds == null)
                    continue;
                try {
                    jdbcTemplate = new JdbcTemplate(ds);
                    List<PM_DATA> list1 = null;

                    try {
                        if (i == 0) {
                            list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where  timePoint >= ? and statPointId in " + inIds, startTime);
                        } else if (i == partitionKeys.size() - 1) {
                            list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where timePoint <= ?  and statPointId in " + inIds, endTime);
                        } else {
                            list1 = JdbcTemplateUtil.queryForList(jdbcTemplate, PM_DATA.class, "SELECT * FROM PM_DATA where statPointId in " + inIds);
                        }
                    } catch (Throwable e) {
                        logger.error(e.getMessage(),e);
                    }
                    logger.info("2::Query in {}: start {}, end {} ,result = {}",ds,startTime,endTime,list1 == null ? null : list1.size());

//                    if (list1 != null && extract && list1.size() > 24) {
//                        list1 = extract(list1,24);
//                    }
                    if (list1 != null)
                        result.addAll(list1);
                } finally {
                    if (ds instanceof SqliteDataSource
                            //&& !sqliteDBMap.contains(key)
                            )
                        ((SqliteDataSource) ds).close();
                }



            }
        }

        long t2 = System.currentTimeMillis() - t1;

        if (t2 > 5)
            logger.info("query spend : "+t2+"ms");

        logger.info("bbbb: result szie = "+result.size());

        if (queryAttributes != null && queryAttributes.containsKey("query.granularityInMin")) {
            int granularityInMin = Integer.parseInt(queryAttributes.get("query.granularityInMin").toString());
            Map<Long, List<PM_DATA>> collect = result.stream().collect(Collectors.groupingBy(p -> p.getStatPointId()));
            result.clear();
            for (List<PM_DATA> datas : collect.values()) {
                List<PM_DATA> c = extractByMinutes(datas, granularityInMin);
                if (granularityInMin == 5) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    long rangeStart = sdf.parse("20170801").getTime();
                    long rangeEnd = sdf.parse("20170911").getTime();
                    if (SysProperty.getString("pms.enforceTimeSlot","").equalsIgnoreCase("true") && (startTime.getTime() >= rangeStart && startTime.getTime() <= rangeEnd) ||
                            (startTime.getTime() < rangeStart && endTime.getTime() >= rangeStart)) {
                        logger.info("between 20170801 and 20170911 ,process data : ");
                        try {
                            logger.info("bbbb: putdataintimeslot");
                            c = putDataInTimeSlot(c);
                        } catch (Exception e) {
                            logger.error(e.getMessage(),e);
                        }
                    }
                }
                result.addAll(c);
            }

            logger.info("bbbb: result2 szie = "+result.size());


            //result = extractByMinutes(result,granularityInMin);
        } else if (result != null && result.size() > 48) {
           // result = extract(result,48);
            Map<Long, List<PM_DATA>> collect = result.stream().collect(Collectors.groupingBy(p -> p.getStatPointId()));
            result.clear();
            for (List<PM_DATA> datas : collect.values()) {
                result.addAll(extract(datas,48));
            }
        }
        logger.info("bbbb: retrun result szie = "+result.size());
        return result;
    }

    private List<PM_DATA> putDataInTimeSlot(List<PM_DATA> result) {
        if (result == null || result.size() < 2) return result;

        int timeSlotInMinutes = 5;
        long timeSlotInMilis = timeSlotInMinutes * 60 * 1000l;
        result.sort((o1,o2)-> (o1.getTimePoint().getTime() > o2.getTimePoint().getTime()  ? 1 : -1));


        Date d_st = result.get(0).getTimePoint();
        Date d_ed = result.get(result.size() - 1).getTimePoint();

        List<Pair<Date, PM_DATA[]>> timeSlots = new ArrayList<>();
        Function<Date,Date> adjustTime = (date)-> {
            Calendar ca = Calendar.getInstance();
            ca.setTime(date);
            int min = ca.get(Calendar.MINUTE);
            min = min - min % 5;
            ca.set(Calendar.MINUTE,min);
            ca.set(Calendar.SECOND,0);
            return ca.getTime();
        };

        Date s = adjustTime.apply(d_st);
        Date e = adjustTime.apply(d_ed);
        for (long t = s.getTime(); t <= e.getTime(); t += timeSlotInMilis) {
            timeSlots.add(Pair.of(new Date(t),new PM_DATA[1] ));
        }


        for (PM_DATA pm_data : result) {
            long timePoint = pm_data.getTimePoint().getTime();
            timeSlots.stream().filter(ts->
                    (ts.getFirst().getTime() >= timePoint && ts.getFirst().getTime() - timePoint < timeSlotInMilis)
                    || (ts.getFirst().getTime() <= timePoint && timePoint - ts.getFirst().getTime() < timeSlotInMilis)
            ).findAny().ifPresent(slot->{
                pm_data.setSource(new SimpleDateFormat("yyyyMMdd-HHmmss").format(pm_data.getTimePoint()));
                pm_data.setTimePoint(slot.getFirst());
                slot.getSecond()[0] = pm_data;
            });
        }

        int makeupsize = 0;
        for (int i = 0; i < timeSlots.size(); i++) {
            Pair<Date, PM_DATA[]> pair = timeSlots.get(i);
            if (pair.getSecond()[0] == null) {
                for (int j = 1; j < 3; j++) {
                    PM_DATA sample = timeSlots.get(i + j).getSecond()[0];
                    if (sample != null) {
                        PM_DATA makeup = new PM_DATA();
                        BeanUtils.copyProperties(sample,makeup);

                        makeup.setTimePoint(pair.getFirst());
                        if (i > 0 && timeSlots.get(i-1).getSecond()[0] != null)
                            makeup.setValue(MathUtil.formatFloat((sample.getValue() + timeSlots.get(i-1).getSecond()[0].getValue()) / 2));
                        else
                            makeup.setValue(sample.getValue());

                        makeup.setValue((float)(makeup.getValue() * ((Math.random() + 9) / 10)   ));

                        makeup.setSource("MAKEUP");
                        makeupsize ++;
                        pair.getSecond()[0] = makeup;

                        break;
                    }
                }
            }
        }
        logger.info("bbbb makeup size = "+makeupsize);

        return timeSlots.stream()
                .filter(pair -> pair.getSecond()[0] != null)
                .map(pair -> pair.getSecond()[0])
                .collect(Collectors.toList());
    }

    private List<PM_DATA> extractByMinutes(List<PM_DATA> list,int granularityInMin) {
        list.sort((o1,o2)-> (o1.getTimePoint().getTime() > o2.getTimePoint().getTime()  ? 1 : -1));

        List result = new ArrayList();
        long t = -1;
        List<PM_DATA> bulk = new ArrayList<>();
        for (PM_DATA pm_data : list) {
            bulk.add(pm_data);
            if (t < 0 || pm_data.getTimePoint().getTime() - t >=  (granularityInMin-1) * 60 * 1000l - 10000l) {
                t = pm_data.getTimePoint().getTime();
                result.add(pm_data);


                if (bulk.size() > 0) {
                    Comparator<PM_DATA> cp = (p1, p2) -> (p1.getValue() - p2.getValue() > 0 ? 1 : -1);
                    Optional<PM_DATA> max = bulk.stream().max(cp);
                    Optional<PM_DATA> min = bulk.stream().min(cp);
                    Optional<Float> total = bulk.stream().map(pm_data1 -> pm_data1.getValue()).reduce((v1, v2) -> v1 + v2);
                    float v = total.get() / bulk.size();

                    pm_data.getDataMap().put("query_max",max.isPresent() ? max.get().getValue() : null);
                    pm_data.getDataMap().put("query_min",min.isPresent() ? min.get().getValue() : null);
                    pm_data.getDataMap().put("query_averageValue",v);

                }
                bulk.clear();

            } else {
                bulk.add(pm_data);
            }
        }
        return result;
    }



    private List<PM_DATA> extract(List<PM_DATA> list1,int sampleNumber) {
        list1.sort((o1,o2)-> (o1.getTimePoint().getTime() > o2.getTimePoint().getTime()  ? 1 : -1));
        int rate = list1.size() / sampleNumber;
   //     int idx = 0;
        List result = new ArrayList();
        for (int i = 0; i < list1.size(); i++) {
            if (i % rate == 0)
                result.add(list1.get(i));
        }
        return result;
        //list1.stream().peek()
    }


    private PMLastestCache lastestCache = new PMLastestCache();

    @Override
    public PM_DATA getLatest(long stpId) {
        PM_DATA data = lastestCache.get(stpId);
        if (data != null && data.getDataMap() != null)
            data.getDataMap().clear();
        return data;
    }

    //@Override
    private boolean addToLatestCache(PM_DATA pm_data) {
        //   jdbcTemplate.execute("UPDATE PM_DATA SET STATUS="+Constants.PM_DATA_STATUS_CACHE+" WHERE STATPOINTID = "+pm_data.getStatPointId());
        pm_data.setStatus(Constants.PM_DATA_STATUS_CURRENT);
        //    JdbcTemplateUtil.insert(jdbcTemplate,"PM_DATA",pm_data);

        lastestCache.add(pm_data);
        return true;
    }


    public static void main(String[] args) throws SQLException {

        List<PM_DATA> list = new ArrayList<>();
        for (int i=0; i < 10; i++) {
            PM_DATA pm_data = new PM_DATA();
            pm_data.setValue((float)i);
            list.add(pm_data);
        }
        Comparator<PM_DATA> cp = (p1, p2) -> (p1.getValue() > p2.getValue() ? 1 : -1);
        Optional<PM_DATA> max = list.stream().max(cp);
        System.out.println("max.get() = " + max.get().getValue());

//        List list = new ArrayList();
//
//        for (int j = 0 ; j < 1000; j++) {
//            System.out.println(j);
//            H2DataSource h2DataSource = new H2DataSource("jdbc:h2:mem:abc_"+j,false);
//            list.add(h2DataSource);
//            JdbcTemplate jd = new JdbcTemplate(h2DataSource);
//            JdbcTemplateUtil.createTable(jd,PM_DATA.class,"PM_DATA");
//            for(int i = 0; i < 10000; i++) {
//                PM_DATA record = new PM_DATA();
//
//                JdbcTemplateUtil.insert(jd, "PM_DATA", record);
//            }
//            h2DataSource.release();
//            list.remove(h2DataSource);
//        }



    }
}
