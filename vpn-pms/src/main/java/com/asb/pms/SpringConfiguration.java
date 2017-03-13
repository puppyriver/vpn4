package com.asb.pms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;

/**
 * Author: Ronnie.Chen
 * Date: 2016/10/17
 * Time: 13:12
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class SpringConfiguration implements InitializingBean{
    private DataSource dataSource = null;

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;

    }

    private Logger logger = LoggerFactory.getLogger(SpringConfiguration.class);


    @Override
    public void afterPropertiesSet() throws Exception {
        Configuration.setDataSource(dataSource);
    }
}
