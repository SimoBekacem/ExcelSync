import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
//        System.out.println("Give the path of you Excel file in you computer :");
//        String filePath = scanner.next(); // put here the path of your file exemple ( "/Users/macintoch/Downloads/Notes.xlsx")
//        ExcelDataReader reader = new ExcelDataReader(filePath);
//        System.out.println(reader.readDataFromExcel().name);
//        System.out.println(reader.readDataFromExcel().attributes);
//        System.out.println(reader.readDataFromExcel().values);



//        System.out.println("Give me the DataBase Url :");
//        String DB_URL = scanner.next();
//        System.out.println("Give me the User name :");
//        String USER = scanner.next();
//        System.out.println("Give me the Password :");
//        String PASS = scanner.next();
        MySQLDataReader test = new MySQLDataReader("jdbc:mysql://localhost:3306/Canevas_Access","root","simo2002");
        test.PutDataFromMySQLToExcelFile();

    }

}