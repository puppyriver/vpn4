package com.asb.pms.util;

import java.lang.reflect.Field;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/29
 * Time: 11:26
 * rongrong.chen@alcatel-sbell.com.cn
 */
public interface DBTypeMapper {
    public String getDBTypeName(Field field);
}
