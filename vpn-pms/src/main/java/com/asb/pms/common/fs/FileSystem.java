package com.asb.pms.common.fs;

import com.asb.pms.util.SqliteDataSource;
import com.asb.pms.model.DBObject;
import com.asb.pms.util.IdentityUtil;
import com.asb.pms.util.JdbcTemplateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Author: Ronnie.Chen
 * Date: 2016/3/17
 * Time: 9:02
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FileSystem {
    private Log logger = LogFactory.getLog(getClass());

    private static ConcurrentHashMap<String,FileSystem> map = new ConcurrentHashMap<>();
    private String rootPath = null;
    private File root = null;
    private JdbcTemplate jdbcTemplate = null;
    SqliteDataSource dataSource = null;

    private FileSystem() {

    }
    private FileSystem(String rootPath) {
        this.rootPath = rootPath;
        init();
    }

    private void init() {
        root = new File(rootPath);
        if (!root.exists() || !root.isDirectory())
            root.mkdirs();
        dataSource = new SqliteDataSource(rootPath+"/fs.db");
        jdbcTemplate = new JdbcTemplate(dataSource);
        try {
//            Connection connection = dataSource.getConnection();
//            BJdbcUtil.createTable(connection,FileRecord.class,"FileRecord");
            JdbcTemplateUtil.createTable(jdbcTemplate,FileRecord.class,"FileRecord");
       //     connection.commit();
        } catch (Exception e) {
          //  logger.error(e, e);
        }

    }
    public File read(long id) {
        FileRecord o = null;
        try {
            o = (FileRecord) JdbcTemplateUtil.queryForObjectById(jdbcTemplate, FileRecord.class, id);
        } catch (Exception e) {
            logger.error(e, e);
        }
        if (o != null) {
            String path = o.getPath();
            return new File(path);
        }
        return null;
    }

    public byte[] readBytes(long id) throws IOException {
        File read = read(id);
        if (read != null) {
            return FileUtils.readFileToByteArray(read);
        }
        return null;
    }

    public long save(long id,byte[] bs) throws IOException {
        File read = read(id);
        FileOutputStream fos = new FileOutputStream(read);
        fos.write(bs);
        fos.flush();
        fos.close();
        return id;
    }

    public long save(File tmpFile) throws IOException {
        long id = 0;
        try {
            Connection connection = dataSource.getConnection();
            id = IdentityUtil.getId(connection, "FileRecord");
        } catch (SQLException e) {
            logger.error(e, e);
        }
        File parent = new File(root,id+"");
        File destFile = new File(parent, tmpFile.getName());
        FileUtils.copyFile(tmpFile, destFile);
        FileRecord fileRecord = new FileRecord();
        fileRecord.setFilename(tmpFile.getName());
        fileRecord.setFilesize(tmpFile.length());
        fileRecord.setKey(id+"");
        fileRecord.setId(id);
        fileRecord.setPath(destFile.getAbsolutePath());
        JdbcTemplateUtil.insert(jdbcTemplate,"FileRecord",fileRecord);
        return id;
    }





    public static FileSystem getFileSystem(String rootPath) {
        FileSystem fs = map.get(rootPath);
        if (fs == null) {
            synchronized (map) {
                fs = map.get(rootPath);
                if (fs == null) {
                    fs = new FileSystem(rootPath);
                    map.put(rootPath,fs);
                }
            }
        }
        return fs;
    }

    public static class FileRecord extends DBObject{

        private String key;
        private String path;
        private String filename;
        private Long filesize;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public Long getFilesize() {
            return filesize;
        }

        public void setFilesize(Long filesize) {
            this.filesize = filesize;
        }
    }
}
