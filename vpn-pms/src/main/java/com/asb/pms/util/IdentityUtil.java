package com.asb.pms.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Author: Ronnie.Chen
 * Date: 2016/2/22
 * Time: 14:56
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class IdentityUtil {
    private static Log log = LogFactory.getLog(IdentityUtil.class);
    static class IdPool {
        public IdPool(long startId) {
            this.startId = startId;
        //    this.maxId = startId + Long.MAX_VALUE;
            this.maxId =  Long.MAX_VALUE;
            this.latestUsedId = -1;
        }

        public synchronized long nextId() {
            if (latestUsedId == -1) {
                latestUsedId = startId;
                return latestUsedId;
            }
            if (latestUsedId < maxId)
                return ++latestUsedId;
            return -1;
        }


        long startId;
        long maxId;
        long latestUsedId;
    }
    private static Hashtable<String,IdPool> idPoolMap = new Hashtable<String, IdPool>();
    private static ReentrantLock lock = new ReentrantLock();
    private static IdPool initPool(Connection connection,String tableName) throws SQLException {
        if (tableName.contains("#")) tableName = tableName.substring(tableName.indexOf("#")+1);
        ResultSet resultSet = connection.createStatement().executeQuery("select max(id) from " + tableName);
        resultSet.next();
        long maxId = resultSet.getLong(1);
        resultSet.close();
        return new IdPool(maxId + 1);
    }
    public static long getId(Connection connection,String tableName) throws SQLException {
        IdPool idPool = idPoolMap.get(tableName);
        if (idPool == null) {
            lock.lock();

            try {
                if (idPoolMap.get(tableName) == null) {
                    idPool = initPool(connection,tableName);
                    idPoolMap.put(tableName, idPool);
                }
            } catch (SQLException e) {
                throw e;
            } catch (Throwable e) {
                log.error(e,e);
            }

            finally {
                lock.unlock();
            }
        }

        long nextId = idPool.nextId();
        if (nextId < 0) {
            idPoolMap.remove(tableName);
            return getId(connection, tableName);
        }

        return nextId;
    }

}
