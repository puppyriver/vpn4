package com.asb.pms.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * Author: Ronnie.Chen
 * Date: 2017/3/13
 * Time: 14:52
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SqlitePool{
    private Logger logger = LoggerFactory.getLogger(SqlitePool.class);

    private int poolSize = 5;
    private Function<String, SqliteDataSource> dsCreator = null;

    public SqlitePool(int poolSize, Function<String, SqliteDataSource> dsCreator) {
        this.poolSize = poolSize;
        this.dsCreator = dsCreator;
    }

    private OrderedConcurrentHashMap<String,SqliteDataSource> cache = new OrderedConcurrentHashMap<>();

    public SqliteDataSource getDatasource(String key,boolean write) {
        SqliteDataSource ds = cache.get(key);
        if (ds == null) {

        }
        return null;
    }


}
