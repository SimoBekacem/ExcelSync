import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MySQLDataReader {
//    those variable should be private static final so change theme when you finish the class.
    Scanner scanner = new Scanner(System.in);
    public String DB_URL ;// put here the url to your database in it is in a localhost take this like an example  "jdbc:mysql://localhost:3306/Canevas_Access"
    public String USER ;// here put the username of your database example  "root";
    public String PASS ;// and here the pass word "simo2002";
    MySQLDataReader(String DB_URL, String USER, String PASS){
        this.DB_URL = DB_URL;
        this.USER = USER;
        this.PASS = PASS;
    }

    public Table readDataFromMySQL(String table_name) {
//       this DriverManager.getConnection(DB_URL, USER, PASS) is for connecting to database it get 3 private static final variables.
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {



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
        System.out.println("Give the table that you want to get the data from : ");
        String table_name = scanner.next();
        Table table = new MySQLDataReader(this.DB_URL, this.USER, this.PASS).readDataFromMySQL(table_name);
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

    public void insertDataFromExcelToSimpleTables(String excelFilePath) {
        Table table = new ExcelDataReader(excelFilePath).readDataFromExcel();

        if (table != null && table.name != null && (table.name.equals("Entite_Chef_de_Projet") || table.name.equals("Entite_Client")|| table.name.equals("Entite_Projet") || table.name.equals("Entite_departement") || table.name.equals("Entite_Livraison"))) {
            // Assuming that the column names in the Excel file match the table column names
            String sql = "INSERT INTO " + table.name + " (";
            String attributes = String.join(", ", table.attributes);
            sql += attributes + ") VALUES (";
            for (int i = 0; i < table.attributes.size(); i++) {
                sql += "?, ";
            }
            sql = sql.substring(0, sql.length() - 2) + ")";

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                for (int i = 0; i < table.values.size(); i += table.attributes.size()) {
                    boolean isDuplicate = false;
                    for (int j = 0; j < table.attributes.size(); j++) {
                        Object value = table.values.get(i + j);
                        if (value instanceof String) {
                            preparedStatement.setString(j + 1, (String) value);
                        } else if (value instanceof Double) {
                            preparedStatement.setDouble(j + 1, (Double) value);
                        } else if (value instanceof Boolean) {
                            preparedStatement.setBoolean(j + 1, (Boolean) value);
                        } else if (value == null) {
                            preparedStatement.setNull(j + 1, java.sql.Types.NULL);
                        }

                        // Check if the current row already exists in the table based on a unique key column (e.g., ID)
                        if (table.attributes.get(j).equalsIgnoreCase("ID")) {
                            String uniqueKeyValue = String.valueOf(value);
                            String checkIfExistsSQL = "SELECT COUNT(*) FROM " + table.name + " WHERE " + table.attributes.get(j) + " = ?";
                            try (PreparedStatement checkIfExistsStmt = connection.prepareStatement(checkIfExistsSQL)) {
                                checkIfExistsStmt.setString(1, uniqueKeyValue);
                                ResultSet resultSet = checkIfExistsStmt.executeQuery();
                                resultSet.next();
                                int count = resultSet.getInt(1);
                                if (count > 0) {
                                    isDuplicate = true;
                                    break; // Skip inserting the current row
                                }
                            }
                        }
                    }

                    if (!isDuplicate) {
                        preparedStatement.addBatch();
                    }
                }

                int[] batchResults = preparedStatement.executeBatch();
                int totalRowsAffected = 0;
                for (int rowsAffected : batchResults) {
                    totalRowsAffected += rowsAffected;
                }
                System.out.println("Rows inserted: " + totalRowsAffected);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid table name or failed to read data from the Excel file.");
        }
    }

    public void insertDataFromUserToSimpleTable(String tableName) {
        if (tableName != null && (tableName.equals("Entite_Chef_de_Projet") || tableName.equals("Entite_Client") || tableName.equals("Entite_departement"))) {
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet columns = metaData.getColumns(null, null, tableName, null);

                ArrayList<String> attributes = new ArrayList<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    attributes.add(columnName);
                }

                String sql = "INSERT INTO " + tableName + " (";
                String attributesStr = String.join(", ", attributes);
                sql += attributesStr + ") VALUES (";
                for (int i = 0; i < attributes.size(); i++) {
                    sql += "?, ";
                }
                sql = sql.substring(0, sql.length() - 2) + ")";

                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    for (String attribute : attributes) {
                        System.out.print(attribute + ": ");
                        String value = scanner.next();
                        preparedStatement.setString(attributes.indexOf(attribute) + 1, value);
                    }

                    int rowsAffected = preparedStatement.executeUpdate();
                    System.out.println("Rows inserted: " + rowsAffected);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid table name.");
        }
    }

    public void insertDataFromUserToSimpleTable(String tableName, int foreignKeyID) {
        if (tableName != null && (tableName.equals("Entite_Chef_de_Projet") || tableName.equals("Entite_Client") || tableName.equals("Entite_departement"))) {
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
                DatabaseMetaData metaData = connection.getMetaData();
                ResultSet columns = metaData.getColumns(null, null, tableName, null);

                ArrayList<String> attributes = new ArrayList<>();
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    attributes.add(columnName);
                }

                String sql = "INSERT INTO " + tableName + " (";
                String attributesStr = String.join(", ", attributes);
                sql += attributesStr + ") VALUES (";
                for (int i = 0; i < attributes.size(); i++) {
                    sql += "?, ";
                }
                sql = sql.substring(0, sql.length() - 2) + ")";

                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    for (String attribute : attributes) {
                        if (attribute.equals("ID")) {
                            preparedStatement.setInt(attributes.indexOf(attribute) + 1, foreignKeyID);
                        } else {
                            System.out.print(attribute + ": ");
                            String value = scanner.next();
                            preparedStatement.setString(attributes.indexOf(attribute) + 1, value);
                        }
                    }

                    int rowsAffected = preparedStatement.executeUpdate();
                    System.out.println("Rows inserted: " + rowsAffected);

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid table name.");
        }
    }

    public void insertDataToEntiteProjetWithForeignKeyCheck(String excelFilePath) {
        Table table = new ExcelDataReader(excelFilePath).readDataFromExcel();
        List<Object> unexistkeys;
        unexistkeys = getNonExistingForeignKeys("Entite_Chef_de_Projet", "ID", "ID_chef_de_projet", excelFilePath);
        for (int i = 0; i < unexistkeys.size(); i++) {
            int foreignKey = ((Number) unexistkeys.get(i)).intValue(); // Cast to int
            insertDataFromUserToSimpleTable("Entite_Chef_de_Projet", foreignKey);
        }
        insertDataFromExcelToSimpleTables(excelFilePath);
    }



    public void insertDataToEntiteLivraisonWithForeignKeyCheck(String excelFilePath) {
        Table table = new ExcelDataReader(excelFilePath).readDataFromExcel();

        List<Object> unexistkeysforID_Departement = getNonExistingForeignKeys("Entite_departement", "ID", "ID_Departement", excelFilePath);
        for (int i = 0; i < unexistkeysforID_Departement.size(); i++) {
            int foreignKey = ((Number) unexistkeysforID_Departement.get(i)).intValue();
            insertDataFromUserToSimpleTable("Entite_departement", foreignKey);
        }

        List<Object> unexistkeysforID_Client = getNonExistingForeignKeys("Entite_Client", "ID", "ID_Client", excelFilePath);
        for (int i = 0; i < unexistkeysforID_Client.size(); i++) {
            int foreignKey = ((Number) unexistkeysforID_Client.get(i)).intValue();
            insertDataFromUserToSimpleTable("Entite_Client", foreignKey);
        }
        List<Object> unexistkeysforID_Projet = getNonExistingForeignKeys("Entite_Projet", "ID", "ID_Projet", excelFilePath);
        for (int i = 0; i < unexistkeysforID_Projet.size(); i++) {
            int foreignKey = ((Number) unexistkeysforID_Projet.get(i)).intValue(); // Corrected line
            insertDataToEntiteProjetWithForeignKeys(foreignKey);
        }


        insertDataFromExcelToSimpleTables(excelFilePath);
    }




    public void insertDataToEntiteProjetWithForeignKeys(int id) {
        Scanner scanner = new Scanner(System.in);

        // Check if the ID exists in the Entite_Projet table
        String checkIdSQL = "SELECT ID FROM Entite_Projet WHERE ID = ?";
        boolean idExists = false;
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement checkIdStmt = connection.prepareStatement(checkIdSQL)) {
            checkIdStmt.setInt(1, id);
            ResultSet resultSet = checkIdStmt.executeQuery();
            idExists = resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (!idExists) {
            // Ask user for ID_chef_de_projet
            System.out.print("Enter ID_chef_de_projet: ");
            int idChefDeProjet = scanner.nextInt();

            // Check if ID_chef_de_projet exists in Entite_Chef_de_Projet table
            String checkChefIdSQL = "SELECT ID FROM Entite_Chef_de_Projet WHERE ID = ?";
            boolean chefIdExists = false;
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement checkChefIdStmt = connection.prepareStatement(checkChefIdSQL)) {
                checkChefIdStmt.setInt(1, idChefDeProjet);
                ResultSet resultSet = checkChefIdStmt.executeQuery();
                chefIdExists = resultSet.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            if (!chefIdExists) {
                // ID_chef_de_projet does not exist in Entite_Chef_de_Projet table, prompt user for chef de projet information
                System.out.println("ID_chef_de_projet does not exist. Please provide chef de projet information:");

                // Ask user for Nom and Prenom
                System.out.print("Nom: ");
                String nom = scanner.next();
                System.out.print("Prenom: ");
                String prenom = scanner.next();

                // Insert chef de projet into Entite_Chef_de_Projet table
                String insertChefSQL = "INSERT INTO Entite_Chef_de_Projet (ID, Nom, Prenom) VALUES (?, ?, ?)";
                try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                     PreparedStatement insertChefStmt = connection.prepareStatement(insertChefSQL)) {
                    insertChefStmt.setInt(1, idChefDeProjet);
                    insertChefStmt.setString(2, nom);
                    insertChefStmt.setString(3, prenom);
                    int rowsAffected = insertChefStmt.executeUpdate();
                    System.out.println("Rows inserted: " + rowsAffected);
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
            }

            // Prompt user for other Entite_Projet information
            System.out.print("Enter CODE: ");
            String code = scanner.next();
            System.out.print("Enter Intitule: ");
            String intitule = scanner.next();

            // Insert data into Entite_Projet table
            String insertProjetSQL = "INSERT INTO Entite_Projet (ID, ID_chef_de_projet, CODE, Intitule) VALUES (?, ?, ?, ?)";
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement insertProjetStmt = connection.prepareStatement(insertProjetSQL)) {
                insertProjetStmt.setInt(1, id);
                insertProjetStmt.setInt(2, idChefDeProjet);
                insertProjetStmt.setString(3, code);
                insertProjetStmt.setString(4, intitule);
                int rowsAffected = insertProjetStmt.executeUpdate();
                System.out.println("Rows inserted: " + rowsAffected);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
















    public List<Object> getNonExistingForeignKeys(String table_name, String key, String foreignkey, String excelFilePath) {
        List<Object> nonExistingForeignKeys = new ArrayList<>();

        Table table = new ExcelDataReader(excelFilePath).readDataFromExcel();

        if (table != null && table.name != null) {
            List<Object> foreignKeysFromExcel = new ArrayList<>();

            // Get the foreign keys from the Excel data based on the specified column (foreignkey)
            for (int i = 0; i < table.values.size(); i += table.attributes.size()) {
                int foreignKeyIndex = table.attributes.indexOf(foreignkey);
                if (foreignKeyIndex >= 0 && foreignKeyIndex < table.values.size()) {
                    Object foreignKeyValue = table.values.get(i + foreignKeyIndex);
                    foreignKeysFromExcel.add(foreignKeyValue);
                }
            }

            // Check if the foreign keys exist in the specified table (table_name) in the database
            String checkForeignKeySQL = "SELECT " + key + " FROM " + table_name + " WHERE " + key + " = ?";
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement checkForeignKeyStmt = connection.prepareStatement(checkForeignKeySQL)) {

                for (Object foreignKey : foreignKeysFromExcel) {
                    checkForeignKeyStmt.setObject(1, foreignKey);
                    ResultSet resultSet = checkForeignKeyStmt.executeQuery();
                    if (!resultSet.next()) {
                        nonExistingForeignKeys.add(foreignKey);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Invalid table name or failed to read data from the Excel file.");
        }

        // Convert back the foreign keys to their original data types (e.g., integers) before returning
        List<Object> convertedForeignKeys = new ArrayList<>();
        for (Object foreignKey : nonExistingForeignKeys) {
            // Assuming the foreign keys are integers, you can convert them as follows:
            if (foreignKey instanceof Number) {
                convertedForeignKeys.add(((Number) foreignKey).intValue());
            } else {
                // Handle other data types if necessary
                // For example, if foreign keys are Strings, you can add them directly to the list
                convertedForeignKeys.add(foreignKey.toString());
            }
        }

        return convertedForeignKeys;
    }






































    public void insertDataFromExcel(String excelFilePath) {
        Table table = new ExcelDataReader(excelFilePath).readDataFromExcel();

        if (table != null) {
            String sql = "INSERT INTO " + table.name + " (";
            String attributes = String.join(", ", table.attributes);
            sql += attributes + ") VALUES (";
            for (int i = 0; i < table.attributes.size(); i++) {
                sql += "?, ";
            }
            sql = sql.substring(0, sql.length() - 2) + ")";

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                int uniqueKeyIndex = -1; // Index of the unique key in the Excel file
                String uniqueKey = "ID"; // Assuming 'ID' is the unique key attribute (you can change it if needed)
                if (table.attributes.contains(uniqueKey)) {
                    uniqueKeyIndex = table.attributes.indexOf(uniqueKey);
                }

                for (int i = 0; i < table.values.size(); i += table.attributes.size()) {
                    String uniqueKeyValue = String.valueOf(table.values.get(i + uniqueKeyIndex));
                    String checkIfExistsSQL = "SELECT COUNT(*) FROM " + table.name + " WHERE " + uniqueKey + " = ?";
                    try (PreparedStatement checkIfExistsStmt = connection.prepareStatement(checkIfExistsSQL)) {
                        checkIfExistsStmt.setString(1, uniqueKeyValue);
                        ResultSet resultSet = checkIfExistsStmt.executeQuery();
                        resultSet.next();
                        int count = resultSet.getInt(1);

                        if (count > 0) {
                            continue;
                        }
                    }

                    for (int j = 0; j < table.attributes.size(); j++) {
                        Object value = table.values.get(i + j);
                        if (value != null && value instanceof Integer) {
                            String attributeName = table.attributes.get(j);
                            String tableName = attributeName.substring(3);

                            if (attributeName.startsWith("ID_")) {
                                String checkForeignKeySQL = "SELECT COUNT(*) FROM " + tableName + " WHERE ID = ?";
                                try (PreparedStatement checkForeignKeyStmt = connection.prepareStatement(checkForeignKeySQL)) {
                                    checkForeignKeyStmt.setInt(1, (int) value);
                                    ResultSet fkResultSet = checkForeignKeyStmt.executeQuery();
                                    fkResultSet.next();
                                    int fkCount = fkResultSet.getInt(1);

                                    if (fkCount == 0) {
                                        System.out.println("Foreign key " + attributeName + " does not exist in table " + tableName);
                                        System.out.println("Please provide the necessary information to create this foreign key:");
                                        System.out.print("ID: ");
                                        int fkId = scanner.nextInt();
                                        System.out.print("CODE: ");
                                        String fkCode = scanner.next();
                                        System.out.print("Nom: ");
                                        String fkNom = scanner.next();

                                        String insertForeignKeySQL = "INSERT INTO " + tableName + " (ID, CODE, Nom) VALUES (?, ?, ?)";
                                        try (PreparedStatement insertForeignKeyStmt = connection.prepareStatement(insertForeignKeySQL)) {
                                            insertForeignKeyStmt.setInt(1, fkId);
                                            insertForeignKeyStmt.setString(2, fkCode);
                                            insertForeignKeyStmt.setString(3, fkNom);
                                            insertForeignKeyStmt.executeUpdate();
                                        }

                                        value = fkId;
                                    }
                                }
                            }
                        }

                        if (value instanceof String) {
                            preparedStatement.setString(j + 1, (String) value);
                        } else if (value instanceof Double) {
                            preparedStatement.setDouble(j + 1, (Double) value);
                        } else if (value instanceof Boolean) {
                            preparedStatement.setBoolean(j + 1, (Boolean) value);
                        } else if (value == null) {
                            preparedStatement.setNull(j + 1, java.sql.Types.NULL);
                        }
                    }

                    preparedStatement.addBatch();
                }

                int[] batchResults = preparedStatement.executeBatch();

                int totalRowsAffected = 0;
                for (int rowsAffected : batchResults) {
                    totalRowsAffected += rowsAffected;
                }
                System.out.println("Rows inserted: " + totalRowsAffected);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Failed to read data from the Excel file.");
        }
    }
















}





















