import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class MySQLDataReader {
//    those variable should be private static final so change theme when you finish the class.
    Scanner scanner = new Scanner(System.in);
    public String DB_URL ;// put here the url to your database in it is in a localhost take this like an example  "jdbc:mysql://localhost:3306/Canevas_Access"
    public String USER ;// here put the user name of your database example  "root";
    public String PASS ;// and here the pass word "simo2002";
    MySQLDataReader(String DB_URL, String USER, String PASS){
        this.DB_URL = DB_URL;
        this.USER = USER;
        this.PASS = PASS;
    }

    public Table readDataFromMySQL() {
//       this DriverManager.getConnection(DB_URL, USER, PASS) is for connecting to database it get 3 private static final variables.
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Give the table that you want to get the data from : ");
            String table_name = scanner.next();


//              get a arraylist of attributes from the tabel :
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, table_name, null);

            ArrayList<String> attributes = new ArrayList<>();

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                attributes.add(columnName);
            }

//            System.out.println("Attributes of the table: " + attributes);


//            get the data from the tables and put it into array list:

            String sql = "SELECT * FROM " + table_name;
            ArrayList<Object> values = new ArrayList<>();

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(sql)) {

                while (resultSet.next()) {

                    for (int i = 0; i < attributes.size(); i++) {
                        values.add(resultSet.getObject(attributes.get(i)));
                    }
                }
//                System.out.println(values);
// here we put the data that we collect from the database into a table object
                Table table = new Table(table_name, attributes, values);
                return table;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }




    void PutDataFromMySQLToExcelFile(){
//        we get the table from the database by the methode readDataFromMySQL();

        Table table = new MySQLDataReader(this.DB_URL, this.USER, this.PASS).readDataFromMySQL();
        System.out.println("Give me the path where you want me to put you file: ");
        String filepath = scanner.next();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Table Data");

            // Write table name to the first row
            Row tableNameRow = sheet.createRow(0);
            Cell tableNameCell = tableNameRow.createCell(0);
            tableNameCell.setCellValue(table.name);

            // Write attributes to the second row
            Row attributesRow = sheet.createRow(1);
            for (int i = 0; i < table.attributes.size(); i++) {
                Cell attributeCell = attributesRow.createCell(i);
                attributeCell.setCellValue(table.attributes.get(i));
            }

            // Write data to subsequent rows
            int rowIndex = 2;
            int dataIndex = 0;

            while (dataIndex < table.values.size()) {
                Row dataRow = sheet.createRow(rowIndex);

                for (int i = 0; i < table.attributes.size(); i++) {
                    Cell dataCell = dataRow.createCell(i);
                    Object value = table.values.get(dataIndex);
                    if (value instanceof String) {
                        dataCell.setCellValue((String) value);
                    } else if (value instanceof Number) {
                        dataCell.setCellValue(((Number) value).doubleValue());
                    } else if (value instanceof Boolean) {
                        dataCell.setCellValue((Boolean) value);
                    } else if (value == null) {
                        dataCell.setCellValue("NULL");
                    } else {
                        // For other unsupported types, convert to a string representation
                        dataCell.setCellValue(value.toString());
                    }
                    dataIndex++;
                }
                rowIndex++;
            }

            // Write the workbook content to the file in the path that you chose
            try (FileOutputStream fileOutputStream = new FileOutputStream(filepath+"/"+table.name+".xlsx")) {
                workbook.write(fileOutputStream);
                System.out.println("the file was created successfully  !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
