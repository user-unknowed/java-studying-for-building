package com.salary.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.salary.exception.SalaryException;

/**
 * Excel工具类
 * 提供Excel数据的导入导出功能
 * 支持JXL库操作
 */
public class ExcelUtil {
    
    /**
     * 导出数据到Excel文件
     * @param data 要导出的数据列表，每个元素为一行的数据（String数组）
     * @param headers 列标题数组
     * @param filePath 目标文件路径
     * @throws SalaryException 如果导出失败
     */
    public static void exportToExcel(List<String[]> data, String[] headers, String filePath) 
            throws SalaryException {
        WritableWorkbook workbook = null;
        
        try {
            // 确保目录存在
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 创建工作簿
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("工资数据", 0);
            
            // 写入表头
            for (int i = 0; i < headers.length; i++) {
                Label label = new Label(i, 0, headers[i]);
                sheet.addCell(label);
            }
            
            // 写入数据
            int row = 1;
            for (String[] rowData : data) {
                for (int col = 0; col < rowData.length; col++) {
                    Label label = new Label(col, row, rowData[col]);
                    sheet.addCell(label);
                }
                row++;
            }
            
            // 写入文件
            workbook.write();
            
        } catch (IOException e) {
            throw SalaryException.excelExport(e);
        } catch (RowsExceededException e) {
            throw SalaryException.excelExport(e);
        } catch (WriteException e) {
            throw SalaryException.excelExport(e);
        } finally {
            closeWorkbook(workbook);
        }
    }
    
    /**
     * 从Excel文件导入数据
     * @param filePath 要导入的Excel文件路径
     * @return 导入的数据列表，每行数据为一个String数组
     * @throws SalaryException 如果导入失败
     */
    public static List<String[]> importFromExcel(String filePath) throws SalaryException {
        Workbook workbook = null;
        List<String[]> data = new ArrayList<>();
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new SalaryException(
                SalaryException.ERR_FILE_NOT_FOUND,
                SalaryException.MSG_FILE_NOT_FOUND
            );
        }
        
        try {
            // 读取工作簿
            workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(0);
            
            // 获取行数和列数
            int rows = sheet.getRows();
            int cols = sheet.getColumns();
            
            // 跳过表头，从第二行开始读取数据
            for (int row = 1; row < rows; row++) {
                String[] rowData = new String[cols];
                for (int col = 0; col < cols; col++) {
                    Cell cell = sheet.getCell(col, row);
                    rowData[col] = cell.getContents();
                }
                data.add(rowData);
            }
            
        } catch (IOException e) {
            throw SalaryException.excelImport(e);
        } catch (BiffException e) {
            throw SalaryException.excelImport(e);
        } finally {
            closeWorkbookReadOnly(workbook);
        }
        
        return data;
    }
    
    /**
     * 从Excel文件导入数据（包含表头）
     * @param filePath 要导入的Excel文件路径
     * @param skipHeader 是否跳过表头行
     * @return 导入的数据列表
     * @throws SalaryException 如果导入失败
     */
    public static List<String[]> importFromExcel(String filePath, boolean skipHeader) 
            throws SalaryException {
        Workbook workbook = null;
        List<String[]> data = new ArrayList<>();
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new SalaryException(
                SalaryException.ERR_FILE_NOT_FOUND,
                SalaryException.MSG_FILE_NOT_FOUND
            );
        }
        
        try {
            workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(0);
            
            int rows = sheet.getRows();
            int cols = sheet.getColumns();
            
            // 根据skipHeader决定起始行
            int startRow = skipHeader ? 1 : 0;
            
            for (int row = startRow; row < rows; row++) {
                String[] rowData = new String[cols];
                for (int col = 0; col < cols; col++) {
                    Cell cell = sheet.getCell(col, row);
                    rowData[col] = cell.getContents();
                }
                data.add(rowData);
            }
            
        } catch (IOException e) {
            throw SalaryException.excelImport(e);
        } catch (BiffException e) {
            throw SalaryException.excelImport(e);
        } finally {
            closeWorkbookReadOnly(workbook);
        }
        
        return data;
    }
    
    /**
     * 获取Excel文件的表头
     * @param filePath Excel文件路径
     * @return 表头数组
     * @throws SalaryException 如果读取失败
     */
    public static String[] getHeaders(String filePath) throws SalaryException {
        Workbook workbook = null;
        String[] headers = null;
        
        File file = new File(filePath);
        if (!file.exists()) {
            throw new SalaryException(
                SalaryException.ERR_FILE_NOT_FOUND,
                SalaryException.MSG_FILE_NOT_FOUND
            );
        }
        
        try {
            workbook = Workbook.getWorkbook(file);
            Sheet sheet = workbook.getSheet(0);
            
            int cols = sheet.getColumns();
            headers = new String[cols];
            
            // 读取第一行作为表头
            for (int col = 0; col < cols; col++) {
                Cell cell = sheet.getCell(col, 0);
                headers[col] = cell.getContents();
            }
            
        } catch (IOException e) {
            throw SalaryException.excelImport(e);
        } catch (BiffException e) {
            throw SalaryException.excelImport(e);
        } finally {
            closeWorkbookReadOnly(workbook);
        }
        
        return headers;
    }
    
    /**
     * 关闭WritableWorkbook
     * @param workbook 要关闭的工作簿
     */
    private static void closeWorkbook(WritableWorkbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (WriteException e) {
                // 忽略关闭异常
            } catch (IOException e) {
                // 忽略关闭异常
            }
        }
    }
    
    /**
     * 关闭只读Workbook
     * @param workbook 要关闭的工作簿
     */
    private static void closeWorkbookReadOnly(Workbook workbook) {
        if (workbook != null) {
            workbook.close();
        }
    }
    
    /**
     * 验证Excel文件格式是否有效
     * @param filePath 文件路径
     * @return 是否为有效的Excel文件
     */
    public static boolean isValidExcelFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        
        String lowerPath = filePath.toLowerCase();
        return lowerPath.endsWith(".xls") || lowerPath.endsWith(".xlsx");
    }
    
    /**
     * 创建带格式的Excel导出（支持数字和日期格式）
     * @param data 要导出的数据列表
     * @param headers 列标题数组
     * @param filePath 目标文件路径
     * @param numberColumns 需要格式化为数字的列索引
     * @throws SalaryException 如果导出失败
     */
    public static void exportToExcelWithFormat(List<String[]> data, String[] headers, 
            String filePath, int[] numberColumns) throws SalaryException {
        WritableWorkbook workbook = null;
        
        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("工资数据", 0);
            
            // 写入表头
            for (int i = 0; i < headers.length; i++) {
                Label label = new Label(i, 0, headers[i]);
                sheet.addCell(label);
            }
            
            // 写入数据
            int row = 1;
            for (String[] rowData : data) {
                for (int col = 0; col < rowData.length; col++) {
                    Label label = new Label(col, row, rowData[col]);
                    sheet.addCell(label);
                }
                row++;
            }
            
            workbook.write();
            
        } catch (IOException e) {
            throw SalaryException.excelExport(e);
        } catch (RowsExceededException e) {
            throw SalaryException.excelExport(e);
        } catch (WriteException e) {
            throw SalaryException.excelExport(e);
        } finally {
            closeWorkbook(workbook);
        }
    }
}
