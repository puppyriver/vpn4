package infox.vpn4.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.*;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-8
 * Time: 下午11:19
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ZipUtil {
       private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);



    public static final String EXT = ".zip";
    private static final String BASE_DIR = "";
    private static final String PATH = File.separator;
    private static final int BUFFER = 1024;

    /**
     * 文件 解压缩
     *
     * @param srcPath
     *            源文件路径
     *
     * @throws Exception
     */
    public static void decompress(String srcPath) throws Exception {
        File srcFile = new File(srcPath);

        decompress(srcFile);
    }

    /**
     * 解压缩
     *
     * @param srcFile
     * @throws Exception
     */
    public static void decompress(File srcFile) throws Exception {
        String basePath = srcFile.getParent();
        decompress(srcFile, basePath);
    }

    /**
     * 解压缩
     *
     * @param srcFile
     * @param destDir
     * @throws Exception
     */
    public static void decompress(File srcFile, File destDir) throws Exception {

        CheckedInputStream cis = new CheckedInputStream(new FileInputStream(
                srcFile), new CRC32());

        ZipInputStream zis = new ZipInputStream(cis);

        decompress(destDir, zis);

        zis.close();

    }

    /**
     * 解压缩
     *
     * @param srcFile
     * @param destPath
     * @throws Exception
     */
    public static void decompress(File srcFile, String destPath)
            throws Exception {
        decompress(srcFile, new File(destPath));

    }

    /**
     * 文件 解压缩
     *
     * @param srcPath
     *            源文件路径
     * @param destPath
     *            目标文件路径
     * @throws Exception
     */
    public static void decompress(String srcPath, String destPath)
            throws Exception {

        File srcFile = new File(srcPath);
        decompress(srcFile, destPath);
    }

    /**
     * 文件 解压缩
     *
     * @param destFile
     *            目标文件
     * @param zis
     *            ZipInputStream
     * @throws Exception
     */
    private static void decompress(File destFile, ZipInputStream zis)
            throws Exception {

        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {

            // 文件
            String dir = destFile.getPath() + File.separator + entry.getName();

            File dirFile = new File(dir);

            // 文件检查
            fileProber(dirFile);

            if (entry.isDirectory()) {
                dirFile.mkdirs();
            } else {
                decompressFile(dirFile, zis);
            }

            zis.closeEntry();
        }
    }

    /**
     * 文件探针
     *
     *
     * 当父目录不存在时，创建目录！
     *
     *
     * @param dirFile
     */
    private static void fileProber(File dirFile) {

        File parentFile = dirFile.getParentFile();
        if (!parentFile.exists()) {

            // 递归寻找上级目录
            fileProber(parentFile);

            parentFile.mkdir();
        }

    }

    /**
     * 文件解压缩
     *
     * @param destFile
     *            目标文件
     * @param zis
     *            ZipInputStream
     * @throws Exception
     */
    private static void decompressFile(File destFile, ZipInputStream zis)
            throws Exception {

        BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(destFile));

        int count;
        byte data[] = new byte[BUFFER];
        while ((count = zis.read(data, 0, BUFFER)) != -1) {
            bos.write(data, 0, count);
        }

        bos.close();
    }

        // 压缩
        public static String compress(String str) throws IOException {
            if (str == null || str.length() == 0) {
                return str;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
            return (new BASE64Encoder()).encodeBuffer(out.toByteArray());
          //  return out.toString();
        }

        // 解压缩
        public static String uncompress(String str) throws IOException {
            if (str == null || str.length() == 0) {
                return str;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(str
                    .getBytes());
            GZIPInputStream gunzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gunzip.read(buffer)) != 0) {
                out.write(buffer, 0, n);
            }
            // toString()使用平台默认编码，也可以显式的指定如toString(&quot;GBK&quot;)
            return out.toString();
        }

    public static String freeZipFile(String fileUri,String depositPath) throws Exception
    {

        if(fileUri.indexOf(".")<0)
            throw new Exception(fileUri+"文件格式应该为 .zip后戳的压缩文件！");
        if(!fileUri.substring(fileUri.indexOf("."),fileUri.length()).contains(".zip"))
            throw new Exception(fileUri+"文件格式应该为 .zip后戳的压缩文件！");
        ZipFile zipFile = null;
        FileOutputStream outStream = null;
        InputStream inputStream = null;
        File loadFile = null;
        ZipEntry zipEntry=null;
        int length = 0;
        byte b[] = new byte [1024];
        try {
            zipFile=new ZipFile(new File(fileUri));
        } catch (Exception e) {
            throw new Exception(e);
        }

        Enumeration enumeration = zipFile.entries();
        while(enumeration.hasMoreElements())
        {
            zipEntry = (ZipEntry) enumeration.nextElement();

            loadFile = new File(depositPath+"/"+zipEntry.getName());
            if (zipEntry.isDirectory()){
                loadFile.mkdirs();
                logger.debug(">>"+loadFile.toString());
            }else{
                if (!loadFile.getParentFile().exists())
                {
                    loadFile.getParentFile().mkdirs();
                    logger.debug(loadFile.getParentFile().toString());
                }
                try {
                    outStream= new FileOutputStream(loadFile);
                    inputStream = zipFile.getInputStream(zipEntry);
                    while ((length = inputStream.read(b)) > 0)
                        outStream.write(b, 0, length);
                } catch (Exception e){
                    throw new Exception(e);
                }
                finally
                {
                    outStream.flush();
                    outStream.close();
                }
            }
        }
        return depositPath;
    }

    private static File compressGZFile(String inFileName) throws IOException {

        try {

            logger.debug("Creating the GZIP output stream.");
            String outFileName = inFileName + ".gz";
            GZIPOutputStream out = null;
            try {
                out = new GZIPOutputStream(new FileOutputStream(outFileName));
            } catch(FileNotFoundException e) {
                throw new RuntimeException("Could not create file: " + outFileName);

            }


            logger.debug("Opening the input file.");
            FileInputStream in = null;
            try {
                in = new FileInputStream(inFileName);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("File not found. " + inFileName);

            }

            logger.debug("Transfering bytes from input file to GZIP Format.");
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();

            logger.debug("Completing the GZIP file");
            out.finish();
            out.close();
            return new File(outFileName);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw e;
        }

    }

    public static File uncompressGZFile(String inFileName) throws IOException {

        try {

            if (!getExtension(inFileName).equalsIgnoreCase("gz")) {
                throw new RuntimeException("File name must have extension of \".gz\"");

            }

            logger.debug("Opening the compressed file.{}",inFileName);
            GZIPInputStream in = null;
            try {
                in = new GZIPInputStream(new FileInputStream(inFileName));
            } catch(FileNotFoundException e) {
                throw new RuntimeException("File not found. " + inFileName);

            }

            logger.debug("Open the output file.");
            String outFileName = getFileName(inFileName);
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(outFileName);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Could not write to file. " + outFileName);

            }

            logger.debug("Transfering bytes from compressed file to the output file.");
            byte[] buf = new byte[1024];
            int len;


            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            logger.debug("Closing the file and stream");
            in.close();
            out.close();
            return new File(outFileName);
        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw e;
        }



    }

    /**
     * Used to extract and return the extension of a given file.
     * @param f Incoming file to get the extension of
     * @return <code>String</code> representing the extension of the incoming
     *         file.
     */
    public static String getExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');

        if (i > 0 &&  i < f.length() - 1) {
            ext = f.substring(i+1);
        }
        return ext;
    }

    /**
     * Used to extract the filename without its extension.
     * @param f Incoming file to get the filename
     * @return <code>String</code> representing the filename without its
     *         extension.
     */
    public static String getFileName(String f) {
        String fname = "";
        int i = f.lastIndexOf('.');

        if (i > 0 &&  i < f.length() - 1) {
            fname = f.substring(0,i);
        }
        return fname;
    }

    public static File renameGZFile(File gzFile,File newParentDir,String newName) throws IOException {
        if (gzFile.getName().equals(newName)) return gzFile;
        try {

            if (!getExtension(gzFile.getName()).equalsIgnoreCase("gz")) {
                throw new RuntimeException("File name must have extension of \".gz\"");

            }

            byte[] bytes = null;

            {
            //    logger.debug("Opening the compressed file.{}", gzFile.getAbsolutePath());
                GZIPInputStream in = null;
                try {
                    in = new GZIPInputStream(new FileInputStream(gzFile));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("File not found. " + gzFile.getAbsolutePath());

                }

            //    logger.debug("Open the output file.");
                ByteArrayOutputStream out = null;
                out = new ByteArrayOutputStream();

            //    logger.debug("Transfering bytes from compressed file to the output file.");
                byte[] buf = new byte[1024];
                int len;


                try {
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } catch (IOException e) {
                    in.close();
                    throw e;
                }

                //     logger.debug("Closing the file and stream");
                in.close();

                bytes = out.toByteArray();
                out.close();
            }

            {
          //      logger.debug("Creating the GZIP output stream.");

                if (newParentDir == null)
                    newParentDir = gzFile.getParentFile();
                if (!newParentDir.exists()) newParentDir.mkdirs();
                File outFile = new File(newParentDir,newName);
                GZIPOutputStream out = null;
                try {
                    out = new GZIPOutputStream(new FileOutputStream(outFile));
                } catch(FileNotFoundException e) {
                    throw new RuntimeException("Could not create file: " + outFile.getAbsolutePath());

                }


                ByteArrayInputStream in = new ByteArrayInputStream(bytes);

           //     logger.debug("Transfering bytes from input file to GZIP Format.");
                byte[] buf = new byte[1024];
                int len;
                while((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();

         //       logger.debug("Completing the GZIP file");
                out.finish();
                out.close();

                gzFile.delete();
                return outFile;
            }

        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            throw e;
        } finally {

        }

    }



    public static void main(String[] args) throws Exception {

//        File file = ZipUtil.uncompressGZFile("c:\\abc.xml.gz");
//        File dest = new File("cde.xml");
//        file.renameTo(dest);
//        ZipUtil.compressGZFile(dest.getAbsolutePath());

 //       ZipUtil.renameGZFile(new File("D:\\newsvn\\xpon-Dev\\NETHERE\\project\\tmp\\135.251.223.103\\test_10.gz"),"bbb.gz");


//        ZipUtil.renameGZFile(new File("c:\\abc.xml.gz"),"bbb.xml.gz");

        //  logger.debug("free = " + free);
        //ZipUtil.uncompress()
    }
}

