import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ExcelDataReader {
    public static void generateExcelFile(Table table, String directoryPath, String filename) {
        String filePath = directoryPath + File.separator + filename;

        try {
            File file = new File(filePath);
            File parentDir = file.getParentFile();

            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir);
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Table Data");

                // Create header row
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < table.attributes.size(); i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(table.attributes.get(i));
                }

                // Populate data rows
                int numRows = table.values.size() / table.attributes.size();
                for (int i = 0; i < numRows; i++) {
                    Row dataRow = sheet.createRow(i + 1);
                    for (int j = 0; j < table.attributes.size(); j++) {
                        Cell cell = dataRow.createCell(j);
                        Object value = table.values.get(i * table.attributes.size() + j);

                        if (value instanceof String) {
                            cell.setCellValue((String) value);
                        } else if (value instanceof Integer) {
                            cell.setCellValue((Integer) value);
                        }else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);
                        } else if (value instanceof java.sql.Date) {
                            cell.setCellValue((java.sql.Date) value);
                            CellStyle dateCellStyle = workbook.createCellStyle();
                            CreationHelper createHelper = workbook.getCreationHelper();
                            dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));
                            cell.setCellStyle(dateCellStyle);
                        }
                    }
                }

                // Resize columns for better readability
                for (int i = 0; i < table.attributes.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write the workbook to the file
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                    System.out.println("Excel file created successfully at: " + filePath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




}
    public static Table readExcelFile(String filePath) {
        List<String> attributes = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(filePath))) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet

            // Read header row (attributes)
            Row headerRow = sheet.getRow(0);
            for (Cell cell : headerRow) {
                attributes.add(cell.getStringCellValue());
            }

            // Read data rows (values)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row dataRow = sheet.getRow(i);
                for (int j = 0; j < attributes.size(); j++) {
                    Cell cell = dataRow.getCell(j);
                    if (cell == null) {
                        values.add(null);
                    } else {
                        Object value;
                        if (cell.getCellType() == CellType.STRING) {
                            value = cell.getStringCellValue();
                        } else if (cell.getCellType() == CellType.NUMERIC) {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                value = new java.sql.Date(cell.getDateCellValue().getTime());
                            } else {
                                value = (int) cell.getNumericCellValue();
                            }
                        } else if (cell.getCellType() == CellType.BOOLEAN) {
                            value = cell.getBooleanCellValue();
                        } else if (cell.getCellType() == CellType.BLANK) {
                            value = null; // Handle blank cells
                        } else {
                            value = null; // Handle other cell types as needed
                        }
                        values.add(value);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Table(attributes, values);
    }


}
