package com.asb.pms.api.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/15
 * Time: 15:39
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class QueryResult implements Serializable{

    private int pageSize = 100;
    private int currentPage = 1;
    private List data;
    private int total = 0;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }



    public QueryResult(int total,int pageSize, int currentPage, List data) {
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.data = data;
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }
}
