package com.asb.pms.tools.ffp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2015/12/9
 * Time: 16:39
 * rongrong.chen@alcatel-sbell.com.cn
 */
public abstract class FormattedFileParser {
    private Log logger = LogFactory.getLog(getClass());


    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    protected String encoding = "gb2312";

    private boolean ignoreFirstLine = false;

    public boolean isIgnoreFirstLine() {
        return ignoreFirstLine;
    }

    public void setIgnoreFirstLine(boolean ignoreFirstLine) {
        this.ignoreFirstLine = ignoreFirstLine;
    }

    abstract public void parseFile(File file, RowHandler handler,List<String> columns) throws IOException ;

    public void parseFile(File file, RowHandler handler) throws IOException {
        parseFile(file, handler,null);
    }

    public static interface RowHandler {
        public String handleRow(DataRow row) throws Exception;
    }

    /**
     * User: Ronnie.Chen
     * Date: 11-5-27
     * Time:
     */
    public static class DataRow {
        private List<String> columns = null;
        private List<String> rowdata = null;
        private int rowNum = -1;


        /**
         * @return the columns
         */
        public List<String> getColumns() {
            return columns;
        }

        public List<String> getRowData() {
            return rowdata;
        }

        public int getRowNum() {
            return rowNum;
        }

        public void setRowNum(int rowNum) {
            this.rowNum = rowNum;
        }

        public String getValue(String columnName) throws Exception {
            int idx = columns.indexOf(columnName);
            if (idx < 0 )
                return null;
            // throw new Exception("Failed to find column:"+columnName);
            if (idx >rowdata.size()-1) return null;
            return rowdata.get(idx);
        }

        public Date getDate(String columnName,String pattern)  throws Exception {
            try {
                String value = getValue(columnName);
                if (value != null) {
                    return new SimpleDateFormat(pattern).parse(value.trim());
                }
            } catch (Exception e) {
                throw e;
            }
            return null;
        }

        public Double getDouble(String columnName)  throws Exception {
            try {
                String value = getValue(columnName);
                if (value != null) {
                    return  Double.parseDouble(value);
                }
            } catch (Exception e) {
                throw e;
            }
            return null;
        }
        public Long getLong(String columnName)  throws Exception {
            try {
                String value = getValue(columnName);
                if (value != null) {
                    return  Long.parseLong(value);
                }
            } catch (Exception e) {
                throw e;
            }
            return null;
        }

        public HashMap<String,String> getValueMap(int rowNumber) {
            HashMap values = new HashMap();

            for (int i = 0; i < columns.size(); i++) {
                String column = columns.get(i);
                String value = rowdata.get(i);
                values.put(column, value);
            }

            return values;
        }
        /**
         * @param columns the columns to set
         */
        public void setColumns(List<String> columns) {
            this.columns = columns;
        }

        public void setRowdata(List<String> row) {
            rowdata = (row);
        }

    }
}
