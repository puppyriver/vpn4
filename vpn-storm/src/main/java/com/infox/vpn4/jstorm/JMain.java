package com.infox.vpn4.jstorm;

import backtype.storm.*;
import backtype.storm.topology.*;
import com.infox.vpn4.jstorm.sequence.SequenceTopologyDef;
import com.infox.vpn4.jstorm.sequence.spout.SequenceSpout;
import com.infox.vpn4.jstorm.userdefined.TotalCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/9
 * Time: 14:02
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class JMain {
    private Logger logger = LoggerFactory.getLogger(JMain.class);

    public static void main(String[] args) throws Exception {
        Map conf = new HashMap();
//topology所有自定义的配置均放入这个Map

        TopologyBuilder builder = new TopologyBuilder();
//创建topology的生成器

        int spoutParal = get("spout.parallel", 1);
//获取spout的并发设置

        SpoutDeclarer spout = builder.setSpout(SequenceTopologyDef.SEQUENCE_SPOUT_NAME,
                new SequenceSpout(), spoutParal);
//创建Spout， 其中new SequenceSpout() 为真正spout对象，SequenceTopologyDef.SEQUENCE_SPOUT_NAME 为spout的名字，注意名字中不要含有空格

        int boltParal = get("bolt.parallel", 1);
//获取bolt的并发设置

        BoltDeclarer totalBolt = builder.setBolt(SequenceTopologyDef.TOTAL_BOLT_NAME, new TotalCount(),
                boltParal).shuffleGrouping(SequenceTopologyDef.SEQUENCE_SPOUT_NAME);
//创建bolt， SequenceTopologyDef.TOTAL_BOLT_NAME 为bolt名字，TotalCount 为bolt对象，boltParal为bolt并发数，
//shuffleGrouping（SequenceTopologyDef.SEQUENCE_SPOUT_NAME），
//表示接收SequenceTopologyDef.SEQUENCE_SPOUT_NAME的数据，并且以shuffle方式，
//即每个spout随机轮询发送tuple到下一级bolt中

        int ackerParal = get("acker.parallel", 1);
        Config.setNumAckers(conf, ackerParal);
//设置表示acker的并发数

        int workerNum = get("worker.num", 10);
        conf.put(Config.TOPOLOGY_WORKERS, workerNum);
//表示整个topology将使用几个worker

        conf.put(Config.STORM_CLUSTER_MODE, "distributed");
//设置topolog模式为分布式，这样topology就可以放到JStorm集群上运行

        StormSubmitter.submitTopology("streamTester", conf,
                builder.createTopology());
//提交topology
    }

    private static int get(String s, int i) {
        return i;
    }
}
