package infox.vpn4.util;


import com.google.common.eventbus.EventBus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;


import javax.persistence.Entity;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import infox.vpn4.valueobject.DN;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/1
 * Time: 20:40
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class CachedDao<T extends DN>  extends EventBus{
    protected Log logger = LogFactory.getLog(getClass());
    protected DataSource dataSource = null;
    protected JdbcTemplate jdbcTemplate = null;
    protected ConcurrentHashMap<Long,T> idMap = new ConcurrentHashMap<>();
    protected ConcurrentHashMap<String,T> dnMap = new ConcurrentHashMap<>();
    protected Class cls = null;


    protected JdbcTemplate cacheJdbc = null;

    public CachedDao(DataSource dataSource,Class cls) {
        this.cls = cls;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        long t1 = System.currentTimeMillis();
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM " + cls.getSimpleName());
        for (Map<String, Object> map : maps) {
            T o = null;
            try {
                o = (T)JdbcTemplateUtil.mapToObject(map, cls);
                idMap.put(o.getId(),o);
                if (o instanceof DN)
                    dnMap.put(((DN) o).getDn(),o);
            } catch (Exception e) {
                logger.error(e, e);
            }

        }

        logger.info("Init CachedDao : "+cls.getSimpleName()+" size = "+idMap.size()+" spend "+(System.currentTimeMillis() - t1)+"ms");


//        if (Context.getInstance().getServer() != null && (Context.getInstance().getServer().getPmCache() != null)) {
//            cacheJdbc = ((H2PMCache) Context.getInstance().getServer().getPmCache()).getJdbcTemplate();
            try {
                JdbcTemplateUtil.createTable(cacheJdbc,cls,getTableName(cls));
            } catch (SQLException e) {
                logger.error(e, e);
            }
            Collection<T> values = idMap.values();
            for (T value : values) {
                JdbcTemplateUtil.insert(cacheJdbc,getTableName(cls),value);
            }


     //   }
    }

    public JdbcTemplate getCacheJdbc() {
        return cacheJdbc;
    }

    public T insertByDn(DN t) {
        T obj = get(t.getDn());
        if (obj == null)
            return insert((T)t);
        return obj;
    }

    public T insertOrUpdate(T t) {
        if (t.getId() != null) {
            update(t);
            return t;
        }
        return insert(t);
    }

    public T insert(T t) {
        String tableName = getTableName(t.getClass());
        Connection connection = null;
        try {
            connection = dataSource.getConnection();

            if (t.getId() == null) {

                t.setId(IdentityUtil.getId(connection, tableName));
            }

            BJdbcUtil.insertObject(connection,t,t.getClass().getSimpleName());

            idMap.put(t.getId(),t);
            if (t instanceof DN)
                dnMap.put(((DN) t).getDn(),t);

            JdbcTemplateUtil.insert(cacheJdbc,tableName,t);
            post(new DaoEvent(DaoEvent.OP_TYPE_CREATE,null,t));


            return t;
        } catch (SQLException e) {
            logger.error(e, e);
        } catch (IllegalAccessException e) {
            logger.error(e, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e, e);
                }
            }
        }
        return null;

    }

    public void insert(List<T> tList) {
        if (tList != null && tList.size() > 0 ) {
            String tableName = getTableName(tList.get(0).getClass());
            Connection connection = null;
            try {
                connection = dataSource.getConnection();
                BJdbcUtil.insertObjects(connection, tList, tableName);

                for (T t : tList) {
                    JdbcTemplateUtil.insert(cacheJdbc,tableName,t);
                    idMap.put( t.getId(),t);
                    if (t instanceof DN)
                        dnMap.put(((DN) t).getDn(),t);
                    post(new DaoEvent(DaoEvent.OP_TYPE_CREATE,null,t));
                }
            } catch (SQLException e) {
                logger.error(e, e);
            } catch (IllegalAccessException e) {
                logger.error(e, e);
            }  finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        logger.error(e, e);
                    }
                }
            }
        }
    }

    public void delete(T t) {
        JdbcTemplateUtil.remove(jdbcTemplate,getTableName(t.getClass()),t.getId());
        JdbcTemplateUtil.remove(cacheJdbc,getTableName(t.getClass()),t.getId());



        idMap.remove(t.getId());
        if (t instanceof DN)
            dnMap.remove(((DN) t).getDn());
        post(new DaoEvent(DaoEvent.OP_TYPE_DELETE,null,t));
    }

    public void update(T t) {
        Connection connection = null;

        try {
            connection = dataSource.getConnection();

            BJdbcUtil.updateObjectById(connection,t,t.getClass().getSimpleName());
            JdbcTemplateUtil.update(cacheJdbc,getTableName(t.getClass()),t);
            T old = idMap.get(t.getId());
            idMap.put(t.getId(),t);
            if (t instanceof DN)
                dnMap.put(((DN) t).getDn(),t);

            post(new DaoEvent(DaoEvent.OP_TYPE_UPDATE,old,t));
        } catch (SQLException e) {
            logger.error(e, e);
        } catch (IllegalAccessException e) {
            logger.error(e, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e, e);
                }
            }
        }
    }

    public T get(long id) {
        return idMap.get(id);
    }

    public T get(String dn) {
        return dnMap.get(dn);
    }

    public List<T> getAll() {
        return new ArrayList(idMap.values());
    }


    public class DaoEvent<T> {
        public static final int OP_TYPE_CREATE = 0;
        public static final int OP_TYPE_UPDATE = 1;
        public static final int OP_TYPE_DELETE = 2;
        public int opType;
        public T oldObject;
        public T newObject;

        public DaoEvent(int opType, T oldObject, T newObject) {
            this.opType = opType;
            this.oldObject = oldObject;
            this.newObject = newObject;
        }
    }

    public static String getTableName(Class cls) {

        Entity entity = (Entity)cls.getAnnotation(Entity.class);

        String tableName = cls.getSimpleName();
        if (entity != null && !entity.name().isEmpty())
            tableName = entity.name();

        return tableName;
    }

}
