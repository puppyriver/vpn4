package com.asb.pms.api.pm;

import com.asb.pms.model.PM_DATA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2016/11/30
 * Time: 13:59
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class PMQueryResult {
    private Logger logger = LoggerFactory.getLogger(PMQueryResult.class);
    private Map<String, List<PM_DATA>> dataMap = new HashMap<>();

    public PMQueryResult(Map<String, List<PM_DATA>> dataMap) {
        this.dataMap = dataMap;
    }

    static Comparator<PM_DATA> cp = (c1, c2) -> (int) (c1.getTimePoint().getTime() - c2.getTimePoint().getTime());




    public void merge(PMQueryResult pmQueryResult) {
        Map<String, List<PM_DATA>> _data = pmQueryResult.dataMap;

        for (String statId : _data.keySet()) {
            List<PM_DATA> pm_datas = dataMap.get(statId);
            if (pm_datas == null) {
                dataMap.put(statId,_data.get(statId));
            } else {

                List<PM_DATA> pm_datas2 = _data.get(statId);


                pm_datas.addAll(pm_datas2);

                pm_datas.sort(cp);

                List l = new ArrayList<>();
                if (pm_datas.size() >1 ) {
                    for (int i = 0; i < pm_datas.size() - 1; i++) {
                        PM_DATA p1 = pm_datas.get(i);
                        PM_DATA p2 = pm_datas.get(i+1);
                        if (p1.getTimePoint().getTime() != p2.getTimePoint().getTime()) {
                            l.add(p1);
                            if (i == pm_datas.size()-2) {
                                l.add(p2);
                            }
                        }

                    }
                }

                if (pm_datas.size() > l.size()) {
                    pm_datas.clear();
                    pm_datas.addAll(l);
                }



            }
        }

    }

    public void shrink(long minTimeSlotInMilis) {
        for (String statId : dataMap.keySet()) {
            List<PM_DATA> pm_datas = dataMap.get(statId);
            pm_datas.sort(cp);

            List<PM_DATA> l = new ArrayList<>();
            PM_DATA temp = null;
            if (pm_datas.size() > 1) {
                for (PM_DATA pm_data : pm_datas) {
                    if (temp == null || pm_data.getTimePoint().getTime() - temp.getTimePoint().getTime() > minTimeSlotInMilis) {
                        l.add(pm_data);
                        temp = pm_data;
                    }
                }
            }

            if (l.size() < pm_datas.size()) {
                pm_datas.clear();
                pm_datas.addAll(l);
            }
        }
    }

    public Map<String, List<PM_DATA>> getData() {
        return dataMap;
    }
}
