package com.asb.pms.tools.ffp;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

/**
 * Author: Ronnie.Chen
 * Date: 2015/12/8
 * Time: 9:53
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class FileReaderWithDefinedLineSplitter extends InputStreamReader {

    private String lineSplitter = "####";
    public FileReaderWithDefinedLineSplitter(String fileName, String charset) throws FileNotFoundException, UnsupportedEncodingException {
        super(new FileInputStream(fileName),charset);
    }

    public FileReaderWithDefinedLineSplitter(String fileName) throws FileNotFoundException {
        super(new FileInputStream(fileName));
    }

    public FileReaderWithDefinedLineSplitter(InputStream in) {
        super(in);
    }

    public FileReaderWithDefinedLineSplitter(InputStream in, String charsetName) throws UnsupportedEncodingException {
        super(in, charsetName);
    }

    public FileReaderWithDefinedLineSplitter(InputStream in, Charset cs) {
        super(in, cs);
    }

    public FileReaderWithDefinedLineSplitter(InputStream in, CharsetDecoder dec) {
        super(in, dec);
    }

    public String getLineSplitter() {
        return lineSplitter;
    }

    public void setLineSplitter(String lineSplitter) {
        this.lineSplitter = lineSplitter;
    }



    public String readLine() throws IOException {
        SegmentHolder holder = new SegmentHolder();
        int s = lineSplitter.charAt(0);


        for (;ready();) {

            if (holder.put((char)read()))
                break;
        }
        if (holder.ready)
            return holder.getSegment();
        else
            return holder.getContent();

    }



    class SegmentHolder {
        char e = lineSplitter.charAt(lineSplitter.length()-1);
        int slength = lineSplitter.length();
        char[] content = new char[128];
        int offset = -1;
        boolean ready = false;
        public boolean put(char c) {
            if (c == -1) {
                return true;
            }


            if (offset == content.length -1) {
                content = Arrays.copyOf(content,content.length * 2);
            }
            content[++offset] = c;

            if (c == e) {
                if (Arrays.equals( Arrays.copyOfRange(content,offset-slength+1,offset+1),lineSplitter.toCharArray())) {
                    ready = true;
                     return true;
                }
            }

            return false;
        }
        public String getContent() {
            if (offset < 0) return null;
            return new String(content);
        }
        public String getSegment() {
            return new String(Arrays.copyOfRange(content,0,offset-slength+1));
        }
    }


    public static void main(String[] args) throws IOException {
        FileReaderWithDefinedLineSplitter reader = new FileReaderWithDefinedLineSplitter("d:\\1507\\files\\GDGK_TROUBLE_TICKET_DAY-20151207000000-20151208000000-20151208132951.csv","gb2312");
        reader.setLineSplitter("####");


//        String[] columns = reader.readLine().split(",");
//        String[] row1 = reader.readLine().split(",");
//        String[] row2 = reader.readLine().split(",");
//        String[] row3 = reader.readLine().split(",");
//        String[] row8 = reader.readLine().split(",");
//        String[] row4 = reader.readLine().split(",");
//        String[] row5 = reader.readLine().split(",");
//        String[] row6 = reader.readLine().split(",");
//        String[] row7 = reader.readLine().split(",");


        while (true) {
            String s = reader.readLine();
            if (s == null) break;
            System.out.println(s.split("\\|\\|\\|\\|").length);
        }

       // System.out.println();


    }




}
