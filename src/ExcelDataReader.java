import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExcelDataReader {

    String path ;
    public ExcelDataReader(String path){
        this.path = path;
    }

//    this is the methode where we can take the data from the file and put it into a object name table;
    public Table readDataFromExcel() {
        try (
             FileInputStream fileInputStream = new FileInputStream(path);
             Workbook workbook = new XSSFWorkbook(fileInputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet




//            this is for getting the name of the table which is in the first cell of the first row :
             Cell name = sheet.getRow(0).getCell(0) ;





//              this is for getting the attributes for the second row :
            ArrayList<Cell> Attributs = new ArrayList<Cell>();
            for (Cell cell: sheet.getRow(1)) {
                Attributs.add(cell);
            }






//            here we get the first line of the data , but the last line is nut the line were the data finished so the finish will detect by the if statement:
            int FirstLine = sheet.getFirstRowNum()+2;
            int LastLine = sheet.getLastRowNum();
//            this is the array of data :
            ArrayList<Cell> values = new ArrayList<Cell>();

//            this for is looping on the rows :
            for (int i=FirstLine; i <= LastLine; i++) {
                Row row = sheet.getRow(i);
                Cell firstCell = row.getCell(0);


                //  this if is for stopping the loop when we're finishing the data of the table
                if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
                    // If the first cell is blank, the row is likely empty, so break the loop
                    break;
                }


//                this loop is looping on the cells of each row:
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            values.add(cell);
                            break;
                        case NUMERIC:
                            values.add(cell);
                            break;
                        case BOOLEAN:
                            values.add(cell);
                            break;
                        case BLANK:
                            break;
                        default:
                            System.out.print("UNKNOWN TYPE" + "\t");
                    }
                }
            }



//            we can now take all the information that we get from file and put it in the table object;
            Table table = new Table(name, Attributs, values);
            return table;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
