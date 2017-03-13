package com.asb.pms.tools.ffp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ronnie.Chen
 * Date: 2015/12/9
 * Time: 16:28
 * rongrong.chen@alcatel-sbell.com.cn
 */
public class XlsFileParser extends FormattedFileParser{
    private Log log = LogFactory.getLog(getClass());
    private int sheetNo = 0;

    public void setSheetNo(int sheetNo) {
        this.sheetNo = sheetNo;
    }

    public static void main(String[] args) throws IOException {
        XlsFileParser parser = new XlsFileParser();
        parser.parseFile(new File("D:\\1507\\files\\business\\KDTYIDataAdd_ORDER_LOOP_20151204_20151204104036.xls"), new RowHandler() {
            @Override
            public String handleRow(DataRow row) throws Exception {
                System.out.println(row.getRowNum() + row.getRowData().get(1));
                return "";
            }
        });


    }

    @Override
    public void parseFile(File file, RowHandler handler, List<String> columns) throws IOException {

        try {
            HSSFWorkbook workbook = new HSSFWorkbook(new FileInputStream(file));
            Integer sheetNum = workbook.getNumberOfSheets();

                HSSFSheet sheet = workbook.getSheetAt(sheetNo);


                Row headers = null;
                for (int rowNum = isIgnoreFirstLine() ? 1 : 0 ;rowNum <= sheet.getLastRowNum();rowNum++) {
                    if (rowNum == 0 && columns == null) {
                        headers = sheet.getRow(rowNum);
                        columns = rowToValues(headers);
                    }
                    else {

                        DataRow dataRow = new DataRow();
                        dataRow.setColumns(columns);
                        Row row = sheet.getRow(rowNum);
                        dataRow.setRowdata(rowToValues(row));
                        dataRow.setRowNum(rowNum);
                        try {
                            handler.handleRow(dataRow);
                        } catch (Exception e) {
                            log.error(e, e);
                        }
                    }
                }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(e,e);
        }
    }



    private List<String> rowToValues(Row row) {
        List values = new ArrayList<>();
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell valueCell = row.getCell(i);
            Object value = null;
            if (valueCell != null) {
                switch (valueCell.getCellType()) {
                    case HSSFCell.CELL_TYPE_STRING:
                        value = valueCell.getStringCellValue();
                        break;

                    case HSSFCell.CELL_TYPE_NUMERIC:
                        value = String.valueOf(valueCell.getNumericCellValue());
                        break;

                    case HSSFCell.CELL_TYPE_BOOLEAN:
                        value = String.valueOf(valueCell.getBooleanCellValue());
                        break;

                    default:
                        value = "";
                        break;
                }
            }

            values.add(value);

        }
        return values;
    }


 }
