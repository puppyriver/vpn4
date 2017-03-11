package com.infox.vpn4.pms2.api.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/15
 * Time: 15:24
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class QueryCondition implements Serializable {
    private int pageSize = -1;
    private int currentPage = 1;
    private String sortField = null;
    private String sortOrder = null;
    private String searchTxt = null;

    public String getSearchTxt() {
        return searchTxt;
    }

    public void setSearchTxt(String searchTxt) {
        this.searchTxt = searchTxt;
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

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
