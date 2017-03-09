package com.infox.vpn4.jstorm.simple;

import backtype.storm.Config;
import backtype.storm.*;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/9
 * Time: 15:29
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class Main {
    private Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
//        conf.put(Config.STORM_LOCAL_DIR, "/Volumes/Study/data/storm");
//        conf.put(Config.STORM_CLUSTER_MODE, "local");
//        //conf.put("storm.local.mode.zmq", "false");
//        conf.put("storm.zookeeper.root", "/storm");
//        conf.put("storm.zookeeper.session.timeout", 50000);
//        conf.put("storm.zookeeper.servers", "nowledgedata-n15");
//        conf.put("storm.zookeeper.port", 2181);
//        //conf.setDebug(true);
//        //conf.setNumWorkers(2);

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("words", new TestWordSpout(), 2);

        builder.setBolt("exclaim2", new TestWordCounter(), 5)
                .localOrShuffleGrouping("words");

//
//        LocalCluster cluster = new LocalCluster();
//        cluster.submitTopology("test", conf, builder.createTopology());

        StormSubmitter.submitTopology("test", conf, builder.createTopology());
    }}
