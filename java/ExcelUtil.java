import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 
 * Excel ������
 * 
 * @author zhangyi
 * @version 1.0 2016/01/27
 *
 */
public class ExcelUtil {

    private Workbook workbook;
    private OutputStream os;
    private String pattern;// ���ڸ�ʽ

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public ExcelUtil(Workbook workboook) {
        this.workbook = workboook;
    }

    public ExcelUtil(InputStream is, String version) throws FileNotFoundException, IOException {
        if ("2003".equals(version)) {
            workbook = new HSSFWorkbook(is);
        } else {
            workbook = new XSSFWorkbook(is);
        }
    }

    public String toString() {

        return "���� " + getSheetCount() + "��sheet ҳ��";
    }

    public String toString(int sheetIx) throws IOException {

        return "�� " + (sheetIx + 1) + "��sheet ҳ�����ƣ� " + getSheetName(sheetIx) + "���� " + getRowCount(sheetIx) + "�У�";
    }

    /**
     * 
     * ���ݺ�׺�ж��Ƿ�Ϊ Excel �ļ�����׺ƥ��xls��xlsx
     * 
     * @param pathname
     * @return
     * 
     */
    public static boolean isExcel(String pathname) {
        if (pathname == null) {
            return false;
        }
        return pathname.endsWith(".xls") || pathname.endsWith(".xlsx");
    }

    /**
     * 
     * ��ȡ Excel ��һҳ��������
     * 
     * @return
     * @throws Exception
     * 
     */
    public List<List<String>> read() throws Exception {
        return read(0, 0, getRowCount(0) - 1);
    }

    /**
     * 
     * ��ȡָ��sheet ҳ��������
     * 
     * @param sheetIx
     *            ָ�� sheet ҳ���� 0 ��ʼ
     * @return
     * @throws Exception
     */
    public List<List<String>> read(int sheetIx) throws Exception {
        return read(sheetIx, 0, getRowCount(sheetIx) - 1);
    }

    /**
     * 
     * ��ȡָ��sheet ҳָ��������
     * 
     * @param sheetIx
     *            ָ�� sheet ҳ���� 0 ��ʼ
     * @param start
     *            ָ����ʼ�У��� 0 ��ʼ
     * @param end
     *            ָ�������У��� 0 ��ʼ
     * @return
     * @throws Exception
     */
    public List<List<String>> read(int sheetIx, int start, int end) throws Exception {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        List<List<String>> list = new ArrayList<List<String>>();

        if (end > getRowCount(sheetIx)) {
            end = getRowCount(sheetIx);
        }

        int cols = sheet.getRow(0).getLastCellNum(); // ��һ��������

        for (int i = start; i <= end; i++) {
            List<String> rowList = new ArrayList<String>();
            Row row = sheet.getRow(i);
            for (int j = 0; j < cols; j++) {
                if (row == null) {
                    rowList.add(null);
                    continue;
                }
                rowList.add(getCellValueToString(row.getCell(j)));
            }
            list.add(rowList);
        }

        return list;
    }

    /**
     * 
     * ������д�뵽 Excel Ĭ�ϵ�һҳ�У��ӵ�1�п�ʼд��
     * 
     * @param rowData
     *            ����
     * @return
     * @throws IOException
     * 
     */
    public boolean write(List<List<String>> rowData) throws IOException {
        return write(0, rowData, 0);
    }

    /**
     * 
     * ������д�뵽 Excel �´����� Sheet ҳ
     * 
     * @param rowData
     *            ����
     * @param sheetName
     *            ����Ϊ1-31�����ܰ���������һ�ַ�: ��\ / ? * [ ]
     * @return
     * @throws IOException
     */
    public boolean write(List<List<String>> rowData, String sheetName, boolean isNewSheet) throws IOException {
        Sheet sheet = null;
        if (isNewSheet) {
            sheet = workbook.createSheet(sheetName);
        } else {
            sheet = workbook.createSheet();
        }
        int sheetIx = workbook.getSheetIndex(sheet);
        return write(sheetIx, rowData, 0);
    }

    /**
     * 
     * ������׷�ӵ�sheetҳ���
     * 
     * @param rowData
     *            ����
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param isAppend
     *            �Ƿ�׷��,true ׷�ӣ�false ����sheet�����
     * @return
     * @throws IOException
     */
    public boolean write(int sheetIx, List<List<String>> rowData, boolean isAppend) throws IOException {
        if (isAppend) {
            return write(sheetIx, rowData, getRowCount(sheetIx));
        } else {// ��������
            clearSheet(sheetIx);
            return write(sheetIx, rowData, 0);
        }
    }

    /**
     * 
     * ������д�뵽 Excel ָ�� Sheet ҳָ����ʼ����,ָ���к�����������ƶ�
     * 
     * @param rowData
     *            ����
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param startRow
     *            ָ����ʼ�У��� 0 ��ʼ
     * @return
     * @throws IOException
     */
    public boolean write(int sheetIx, List<List<String>> rowData, int startRow) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        int dataSize = rowData.size();
        if (getRowCount(sheetIx) > 0) {// ���С�ڵ���0����һ�ж�������
            sheet.shiftRows(startRow, getRowCount(sheetIx), dataSize);
        }
        for (int i = 0; i < dataSize; i++) {
            Row row = sheet.createRow(i + startRow);
            for (int j = 0; j < rowData.get(i).size(); j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(rowData.get(i).get(j) + "");
            }
        }
        return true;
    }

    /**
     * 
     * ����cell ��ʽ
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param colIndex
     *            ָ���У��� 0 ��ʼ
     * @return
     * @throws IOException
     */
    public boolean setStyle(int sheetIx, int rowIndex, int colIndex, CellStyle style) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        // sheet.autoSizeColumn(colIndex, true);// �����п������Ӧ
        sheet.setColumnWidth(colIndex, 4000);

        Cell cell = sheet.getRow(rowIndex).getCell(colIndex);
        cell.setCellStyle(style);

        return true;
    }

    /**
     * 
     * ������ʽ
     * 
     * @param type
     *            1������ 2����һ��
     * @return
     */
    public CellStyle makeStyle(int type) {
        CellStyle style = workbook.createCellStyle();

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("@"));// // ������ʽ ���õ�Ԫ�����ݸ�ʽ���ı�
        style.setAlignment(CellStyle.ALIGN_CENTER);// ���ݾ���

        // style.setBorderTop(CellStyle.BORDER_THIN);// �߿���ʽ
        // style.setBorderRight(CellStyle.BORDER_THIN);
        // style.setBorderBottom(CellStyle.BORDER_THIN);
        // style.setBorderLeft(CellStyle.BORDER_THIN);

        Font font = workbook.createFont();// ������ʽ

        if (type == 1) {
            // style.setFillForegroundColor(HSSFColor.LIGHT_BLUE.index);//��ɫ��ʽ
            // ǰ����ɫ
            // style.setFillBackgroundColor(HSSFColor.LIGHT_BLUE.index);//����ɫ
            // style.setFillPattern(CellStyle.ALIGN_FILL);// ��䷽ʽ
            font.setBold(true);
            font.setFontHeight((short) 500);
        }

        if (type == 2) {
            font.setBold(true);
            font.setFontHeight((short) 300);
        }

        style.setFont(font);

        return style;
    }

    /**
     * 
     * �ϲ���Ԫ��
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param firstRow
     *            ��ʼ��
     * @param lastRow
     *            ������
     * @param firstCol
     *            ��ʼ��
     * @param lastCol
     *            ������
     */
    public void region(int sheetIx, int firstRow, int lastRow, int firstCol, int lastCol) {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    /**
     * 
     * ָ�����Ƿ�Ϊ��
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ����ʼ�У��� 0 ��ʼ
     * @return true ��Ϊ�գ�false ����Ϊ��
     * @throws IOException
     */
    public boolean isRowNull(int sheetIx, int rowIndex) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        return sheet.getRow(rowIndex) == null;
    }

    /**
     * 
     * �����У����д��ڣ������
     * 
     * @param sheetIx
     *            ָ�� sheet ҳ���� 0 ��ʼ
     * @param rownum
     *            ָ�������У��� 0 ��ʼ
     * @return
     * @throws IOException
     */
    public boolean createRow(int sheetIx, int rowIndex) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        sheet.createRow(rowIndex);
        return true;
    }

    /**
     * 
     * ָ����Ԫ���Ƿ�Ϊ��
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ����ʼ�У��� 0 ��ʼ
     * @param colIndex
     *            ָ����ʼ�У��� 0 ��ʼ
     * @return true �в�Ϊ�գ�false ��Ϊ��
     * @throws IOException
     */
    public boolean isCellNull(int sheetIx, int rowIndex, int colIndex) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        if (!isRowNull(sheetIx, rowIndex)) {
            return false;
        }
        Row row = sheet.getRow(rowIndex);
        return row.getCell(colIndex) == null;
    }

    /**
     * 
     * ������Ԫ��
     * 
     * @param sheetIx
     *            ָ�� sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У��� 0 ��ʼ
     * @param colIndex
     *            ָ�������У��� 0 ��ʼ
     * @return true ��Ϊ�գ�false �в�Ϊ��
     * @throws IOException
     */
    public boolean createCell(int sheetIx, int rowIndex, int colIndex) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        Row row = sheet.getRow(rowIndex);
        row.createCell(colIndex);
        return true;
    }

    /**
     * ����sheet �е�����
     * 
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @return
     */
    public int getRowCount(int sheetIx) {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        if (sheet.getPhysicalNumberOfRows() == 0) {
            return 0;
        }
        return sheet.getLastRowNum() + 1;

    }

    /**
     * 
     * ���������е�����
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У���0��ʼ
     * @return ����-1 ��ʾ������Ϊ��
     */
    public int getColumnCount(int sheetIx, int rowIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        Row row = sheet.getRow(rowIndex);
        return row == null ? -1 : row.getLastCellNum();

    }

    /**
     * 
     * ����row �� column λ�õĵ�Ԫ��ֵ
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У���0��ʼ
     * @param colIndex
     *            ָ���У���0��ʼ
     * @param value
     *            ֵ
     * @return
     * @throws IOException
     */
    public boolean setValueAt(int sheetIx, int rowIndex, int colIndex, String value) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        sheet.getRow(rowIndex).getCell(colIndex).setCellValue(value);
        return true;
    }

    /**
     * 
     * ���� row �� column λ�õĵ�Ԫ��ֵ
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У���0��ʼ
     * @param colIndex
     *            ָ���У���0��ʼ
     * @return
     * 
     */
    public String getValueAt(int sheetIx, int rowIndex, int colIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        return getCellValueToString(sheet.getRow(rowIndex).getCell(colIndex));
    }

    /**
     * 
     * ����ָ���е�ֵ
     * 
     * @param rowData
     *            ����
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У���0��ʼ
     * @return
     * @throws IOException
     */
    public boolean setRowValue(int sheetIx, List<String> rowData, int rowIndex) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        Row row = sheet.getRow(rowIndex);
        for (int i = 0; i < rowData.size(); i++) {
            row.getCell(i).setCellValue(rowData.get(i));
        }
        return true;
    }

    /**
     * 
     * ����ָ���е�ֵ�ļ���
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У���0��ʼ
     * @return
     */
    public List<String> getRowValue(int sheetIx, int rowIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        Row row = sheet.getRow(rowIndex);
        List<String> list = new ArrayList<String>();
        if (row == null) {
            list.add(null);
        } else {
            for (int i = 0; i < row.getLastCellNum(); i++) {
                list.add(getCellValueToString(row.getCell(i)));
            }
        }
        return list;
    }

    /**
     * 
     * �����е�ֵ�ļ���
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У���0��ʼ
     * @param colIndex
     *            ָ���У���0��ʼ
     * @return
     */
    public List<String> getColumnValue(int sheetIx, int rowIndex, int colIndex) {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        List<String> list = new ArrayList<String>();
        for (int i = rowIndex; i < getRowCount(sheetIx); i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                list.add(null);
                continue;
            }
            list.add(getCellValueToString(sheet.getRow(i).getCell(colIndex)));
        }
        return list;
    }

    /**
     * 
     * ��ȡexcel ��sheet ��ҳ��
     * 
     * @return
     */
    public int getSheetCount() {
        return workbook.getNumberOfSheets();
    }

    public void createSheet() {
        workbook.createSheet();
    }

    /**
     * 
     * ����sheet���ƣ�����Ϊ1-31�����ܰ���������һ�ַ�: ��\ / ? * [ ]
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ��//
     * @param name
     * @return
     * @throws IOException
     */
    public boolean setSheetName(int sheetIx, String name) throws IOException {
        workbook.setSheetName(sheetIx, name);
        return true;
    }

    /**
     * 
     * ��ȡ sheet����
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @return
     * @throws IOException
     */
    public String getSheetName(int sheetIx) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        return sheet.getSheetName();
    }

    /**
     * ��ȡsheet����������0��ʼ
     * 
     * @param name
     *            sheet ����
     * @return -1��ʾ��δ�ҵ����ƶ�Ӧ��sheet
     */
    public int getSheetIndex(String name) {
        return workbook.getSheetIndex(name);
    }

    /**
     * 
     * ɾ��ָ��sheet
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @return
     * @throws IOException
     */
    public boolean removeSheetAt(int sheetIx) throws IOException {
        workbook.removeSheetAt(sheetIx);
        return true;
    }

    /**
     * 
     * ɾ��ָ��sheet���У��ı����֮���е�����
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @param rowIndex
     *            ָ���У���0��ʼ
     * @return
     * @throws IOException
     */
    public boolean removeRow(int sheetIx, int rowIndex) throws IOException {
        Sheet sheet = workbook.getSheetAt(sheetIx);
        sheet.shiftRows(rowIndex + 1, getRowCount(sheetIx), -1);
        Row row = sheet.getRow(getRowCount(sheetIx) - 1);
        sheet.removeRow(row);
        return true;
    }

    /**
     * 
     * ����sheet ҳ������
     * 
     * @param sheetname
     *            Sheet ����
     * @param pos
     *            Sheet ��������0��ʼ
     */
    public void setSheetOrder(String sheetname, int sheetIx) {
        workbook.setSheetOrder(sheetname, sheetIx);
    }

    /**
     * 
     * ���ָ��sheetҳ����ɾ������Ӳ�ָ��sheetIx��
     * 
     * @param sheetIx
     *            ָ�� Sheet ҳ���� 0 ��ʼ
     * @return
     * @throws IOException
     */
    public boolean clearSheet(int sheetIx) throws IOException {
        String sheetname = getSheetName(sheetIx);
        removeSheetAt(sheetIx);
        workbook.createSheet(sheetname);
        setSheetOrder(sheetname, sheetIx);
        return true;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * 
     * �ر���
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        if (os != null) {
            os.close();
        }
        workbook.close();
    }

    /**
     * 
     * ת����Ԫ�������ΪString Ĭ�ϵ� <br>
     * Ĭ�ϵ��������ͣ�CELL_TYPE_BLANK(3), CELL_TYPE_BOOLEAN(4),
     * CELL_TYPE_ERROR(5),CELL_TYPE_FORMULA(2), CELL_TYPE_NUMERIC(0),
     * CELL_TYPE_STRING(1)
     * 
     * @param cell
     * @return
     * 
     */
    private String getCellValueToString(Cell cell) {
        String strCell = "";
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
        case Cell.CELL_TYPE_BOOLEAN:
            strCell = String.valueOf(cell.getBooleanCellValue());
            break;
        case Cell.CELL_TYPE_NUMERIC:
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                Date date = cell.getDateCellValue();
                if (pattern != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern);
                    strCell = sdf.format(date);
                } else {
                    strCell = date.toString();
                }
                break;
            }
            // �������ڸ�ʽ�����ֹ�����ֹ���ʱ�Կ�ѧ��������ʾ
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            strCell = cell.toString();
            break;
        case Cell.CELL_TYPE_STRING:
            strCell = cell.getStringCellValue();
            break;
        default:
            break;
        }
        return strCell;
    }

}