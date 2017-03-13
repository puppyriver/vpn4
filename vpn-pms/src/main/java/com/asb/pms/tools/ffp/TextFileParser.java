package com.asb.pms.tools.ffp;



import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * User: Ronnie.Chen
 * Date: 11-5-27
 * Time:
 */
public class TextFileParser extends FormattedFileParser{

    private String columnSeperator = "\t";
    private String lineSplitter = null;     // default /r /n

    public TextFileParser(String columnSeperator, String lineSplitter) {
        this.columnSeperator = columnSeperator;
        this.lineSplitter = lineSplitter;
    }

    public TextFileParser( ) {

    }
    public String getLineSplitter() {
        return lineSplitter;
    }

    public void setLineSplitter(String lineSplitter) {
        this.lineSplitter = lineSplitter;
    }

    public void parseFile(File file, RowHandler handler,List<String> columns) throws IOException {
        if (lineSplitter != null) {
            parseFileWithDefinedLineSplitter(file,handler);
            return;
        }

        BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        int rownum = 0;
        while (true) {

            String line = fr.readLine();
            if (line == null) break;

            if (rownum == 0 && isIgnoreFirstLine()) continue;

            if (line.trim().isEmpty()) continue;

            String[] values = line.split(columnSeperator);
            for (int i = 0; i < values.length; i++) {
                String v = values[i];
                values[i] = v.trim();
            }
            if (columns == null || columns.size() == 0)
                columns = (Arrays.asList(values));
            else {
                List data = (Arrays.asList(values));
                DataRow row = new DataRow();
                row.setColumns(columns);
                row.setRowdata(data);
                row.setRowNum(++rownum);

                try {
                    handler.handleRow(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
        fr.close();

    }




    public void setColumnSeperator(String seperator) {
        this.columnSeperator = seperator;
    }

    public void parseFileWithDefinedLineSplitter(File file,RowHandler handler) throws IOException {
        List<String> columns = null;

        FileReaderWithDefinedLineSplitter fr = new FileReaderWithDefinedLineSplitter( new FileInputStream(file), encoding);
        fr.setLineSplitter(lineSplitter);
        int rownum = 0;
        while (true) {
            String line = fr.readLine();
            if (line == null) break;

            if (line.trim().isEmpty()) continue;

            String[] values = line.split(columnSeperator);
            for (int i = 0; i < values.length; i++) {
                String v = values[i];
                values[i] = v.trim();
            }
            if (columns == null || columns.size() == 0)
                columns = (Arrays.asList(values));
            else {
                List data = (Arrays.asList(values));
                DataRow row = new DataRow();
                row.setColumns(columns);
                row.setRowdata(data);
                row.setRowNum(++rownum);

                try {
                    handler.handleRow(row);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
        fr.close();



    }




    public static void main(String[] args) throws IOException {

    }



}
