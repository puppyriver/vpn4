package infox.vpn4.boot.services;


//import com.alcatelsbell.nms.valueobject.BObject;
import infox.vpn4.valueobject.BObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2017/1/23
 * Time: 14:23
 * rongrong.chen@alcatel-sbell.com.cn
 */
@Service
public class JpaService {
    private Logger logger = LoggerFactory.getLogger(JpaService.class);

    @PersistenceContext
    private EntityManager em;


    public long findObjectsCount(String ql,Object... params) {
       List l = findObjects(ql,params);
       if (l != null && l.size() > 0) return ((Number)l.get(0)).longValue();
       return -1;
    }

    public List findObjects(String ql,Object... params) {
        Query query =  em.createQuery(ql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        return query.getResultList();
    }

    public BObject findObjectByDn(Class  cls,String dn) throws Exception {
        List<BObject> objs = this.findObjects("select c from "+cls.getSimpleName()+" as c where c.dn = '"+dn+"'");
        if (objs != null && objs.size() > 0)
            return objs.get(0);
        return null;
    }

    public BObject storeObjectByDn(BObject _obj) throws Exception {
        BObject result = null;
        BObject bobjDB =// null;
                (BObject) findObjectByDn(_obj.getClass(),_obj.getDn());
        if (bobjDB == null)
        {
            result = insert(_obj);
        } else {
            _obj.setId(bobjDB.getId());
            _obj.setCreateDate(bobjDB.getCreateDate());
            result = update(_obj);
        }
        return result;
    }

    public BObject save(BObject bo) {
        if (bo.getId() == null) {
            return insert(bo);
        } else {
            return update(bo);
        }


    }

    public BObject insert(BObject bo) {
        bo.setCreateDate(new Date());
        em.persist(bo);
        return bo;
    }

    public BObject update(BObject bo) {
        bo.setUpdateDate(new Date());
        BObject newObject = (BObject)em.merge(bo);
        em.persist(newObject);
        return newObject;
    }

    public void delete(BObject bo) {
        em.remove(em.merge(bo));

    }

    public List findObjects(String jpql, Integer start, Integer limit,Object... params) throws Exception {
        Query query = em.createQuery(jpql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        if (start != null)
            query.setFirstResult(start.intValue());
        if (limit != null)
            query.setMaxResults(limit.intValue());

        return query.getResultList();
    }
    public List findAllObjects(Class cls) throws Exception{
        return findObjects("select c from "+cls.getSimpleName()+" as c");
    }


    public Object findObjectById( Class objClass, long id) throws Exception
    {
        List result = null;
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery c = cb.createQuery(objClass);
        Root emp = c.from(objClass);
        c.select(emp).where(cb.equal(emp.get("id"), Long.valueOf(id)));
        result = em.createQuery(c).getResultList();
        if (result.isEmpty())
            return null;
        return result.get(0);
    }

    public List  queryNativeSQL( String _queryStr,Class clazz)
    {
        Query query = em.createNativeQuery(_queryStr,clazz);
        return query.getResultList();
    }

    public List  queryNativeSQL (String _queryStr)
    {
        Query query = em.createNativeQuery(_queryStr);
        return query.getResultList();
    }

    public int executeQl(String ql,Object... params) {

        Query query = em.createQuery(ql);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i + 1, params[i]);
            }
        }
        int re = query.executeUpdate();
        return re;
    }

    public int executeNativeSql(String sql,Object... params) {
        Query query = em.createNativeQuery(sql);
        int re = query.executeUpdate();
        return re;
    }




}
