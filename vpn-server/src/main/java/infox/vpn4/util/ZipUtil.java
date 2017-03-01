package infox.vpn4.util;

import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Author: Ronnie.Chen
 * Date: 14-7-8
 * Time: 下午11:19
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class ZipUtil {


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


}

