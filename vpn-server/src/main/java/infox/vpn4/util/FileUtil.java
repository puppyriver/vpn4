package infox.vpn4.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.MessageDigest;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
    public FileUtil() {
    }

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);          //<span style="color: rgb(51, 51, 51); font-family: arial; font-size: 13px; line-height: 20px;">将流类型字符串转换为String类型字符串</span>

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5"); //如果想使用SHA-1或SHA-256，则传入SHA-1,SHA-256
        int numRead;

        do {
            numRead = fis.read(buffer);    //从文件读到buffer，最多装满buffer
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);  //用读到的字节进行MD5的计算，第二个参数是偏移量
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring(1);//加0x100是因为有的b[i]的十六进制只有1位
        }
        return result;
    }




    public static void newFolder(String folderPath) {
        try {
            String filePath = folderPath;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            if (!myFilePath.exists()) {
                myFilePath.mkdir();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static File newFile(String strPath) {
        File file = new File(strPath);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return file;
    }

    public static BufferedWriter getBufferedWriter(File file, boolean isAppend) {
        FileWriter writer = null;
        BufferedWriter bufWriter = null;
        try {
            writer = new FileWriter(file, isAppend);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        bufWriter = new BufferedWriter(writer);
        return bufWriter;
    }

    public static BufferedReader getBufferedReader(File file) {
        FileReader reader = null;
        BufferedReader bufReader = null;
        try {
            reader = new FileReader(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        bufReader = new BufferedReader(reader);
        return bufReader;
    }

    public static boolean writelnToFile(BufferedWriter writer, String msg) {
        return writeToFile(writer, msg + "\n");
    }

    public static boolean writeToFile(BufferedWriter writer, String msg) {
        try {
            writer.write(msg);
            writer.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }



    public static void writeToFile(byte[] bytes, File file) {
        if (bytes == null) {
            return;
        }
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(bytes, 0,
                            bytes.length);
            outStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static byte[] readFile(File file) {
        byte[] content = new byte[(int) file.length()];
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            inStream.read(content);
            inStream.close();
            return content;
        } catch (FileNotFoundException ex) {
            return new byte[0];
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    public static boolean moveFile(File source,File dest) throws IOException {
        if (!dest.getParentFile().exists()) dest.getParentFile().mkdirs();
        boolean b = source.renameTo(dest);
        if (!b) {
            if (copyFile(source, dest)) {
                boolean c = source.delete();
                if (!c)
                    new IOException("Exception on delete file "+source.getAbsolutePath());
                return true;
            }
            throw new IOException("Exception on move file "+source.getAbsolutePath()+" to "+dest.getAbsolutePath());

        }
        return true;
    }


    public static boolean copyFile(File source, File dest) throws IOException {
        try {
            if (!dest.exists()) {
                dest.createNewFile();
            }
            InputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(dest);
            byte[] by = new byte[1024 * 10];
            int c;
            while ((c = in.read(by)) != -1) {
                out.write(by, 0, c);
            }
            out.close();
            in.close();
        } catch (IOException ex) {
              throw ex;
        }
        return true;
    }

    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return file.delete();
        }
        return true;
    }

    public static boolean deleteDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) return true;
        if (file.isDirectory()) {
            File[] f = file.listFiles();
            for (File file1 : f) {
                deleteDir(file1.getAbsolutePath());
            }
            return file.delete();
        } else {
            return file.delete();
        }
    }

    public static boolean clearDir(String dir) {
        File file = new File(dir);
        if (!file.exists()) return true;
        if (file.isDirectory()) {
            File[] f = file.listFiles();
            for (File file1 : f) {
                deleteDir(file1.getAbsolutePath());
            }

        }

        return file.list().length == 0;
    }

    public static String getFileName(String fullFileName) {
        String result = "";
        if (fullFileName != null && fullFileName.lastIndexOf(".") > 0) {
            result = fullFileName.substring(0, fullFileName.lastIndexOf("."));
        }
        return result;
    }

    public static void checkDirectoryWritePermission(File dir) {
        if (!dir.exists() || !dir.canWrite()) {
            throw new RuntimeException(dir.getAbsolutePath()+" not exists,or need write privilege !");
        }
        logger.info(" check write privilege : {} ok! .",dir.getAbsolutePath());

    }

    public static void main(String[] args) {
        newFolder("newfold");
        File file = newFile("newfold\\newfile");
    }
}
