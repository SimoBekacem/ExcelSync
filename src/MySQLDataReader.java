import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MySQLDataReader {
    //    those variable should be private static final so change theme when you finish the class.
    Scanner scanner = new Scanner(System.in);
    static private String DB_URL;// put here the url to your database in it is in a localhost take this like an example  "jdbc:mysql://localhost:3306/Canevas_Access"
    static private String USER;// here put the username of your database example  "root";
    static private String PASS;// and here the pass word "simo2002";

    MySQLDataReader(String DB_URL, String USER, String PASS) {
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

    void PutDataFromMySQLToExcelFile() {
//        we get the table from the database by the methode readDataFromMySQL();
        System.out.println("Indiquez-moi le tableau à partir duquel vous souhaitez obtenir les données :");
        String table_name = scanner.next();
        Table table = new MySQLDataReader(this.DB_URL, this.USER, this.PASS).readDataFromMySQL(table_name);
        System.out.println("Indiquez-moi le chemin où vous voulez que je place votre fichier :");
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
            try (FileOutputStream fileOutputStream = new FileOutputStream(filepath + "/" + table.name + ".xlsx")) {
                workbook.write(fileOutputStream);
                System.out.println("le fichier a été créé avec succès !");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insertDataFromExcelToSimpleTables(String excelFilePath) {
        Table table = new ExcelDataReader(excelFilePath).readDataFromExcel();

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
            System.out.println("le tableau " + table.name + " a " + totalRowsAffected + " lignes insérées. ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertDataFromUserToSimpleTable(String tableName, int foreignKeyID) {
        System.out.println("La table " + tableName + " ne contient pas cet identifiant " + foreignKeyID + " , je vous poserai donc la même question pour le créer,");
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
                            System.out.print("Donner moi le " + attribute + " (nous travaillons sur la table " + tableName + " ) : ");
                            String value = scanner.next();
                            preparedStatement.setString(attributes.indexOf(attribute) + 1, value);
                        }
                    }

                    int rowsAffected = preparedStatement.executeUpdate();
                    System.out.println("le tableau " + tableName + " a " + rowsAffected + " lignes insérées. ");

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Nom de table non valide.");
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
        System.out.println("La table Entite_Projet ne contient pas cet identifiant " + id + " , je vous poserai donc la même question pour le créer,");

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
            System.out.print("Donner moi le  ID_chef_de_projet ( nous travaillons sur la table Entite_Chef_de_Projet ): ");
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
                System.out.print("Donner moi le Nom ( nous travaillons sur la table Entite_Chef_de_Projet ): ");
                String nom = scanner.next();
                System.out.print("Donner moi le Prenom ( nous travaillons sur la table Entite_Chef_de_Projet ): ");
                String prenom = scanner.next();

                // Insert chef de projet into Entite_Chef_de_Projet table
                String insertChefSQL = "INSERT INTO Entite_Chef_de_Projet (ID, Nom, Prenom) VALUES (?, ?, ?)";
                try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);
                     PreparedStatement insertChefStmt = connection.prepareStatement(insertChefSQL)) {
                    insertChefStmt.setInt(1, idChefDeProjet);
                    insertChefStmt.setString(2, nom);
                    insertChefStmt.setString(3, prenom);
                    int rowsAffected = insertChefStmt.executeUpdate();
                    System.out.println("le tableau Entite_Chef_de_Projet a " + rowsAffected + " lignes insérées.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
            }

            // Prompt user for other Entite_Projet information
            System.out.print("Donner moi le CODE ( nous travaillons sur la table Entite_Projet ): ");
            String code = scanner.next();
            System.out.print("Donner moi le Intitule ( nous travaillons sur la table Entite_Projet ): ");
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

    public List<Object> getNonExistingForeignKeys(String table_name, String key, String foreignkey, Table table) {
        List<Object> nonExistingForeignKeys = new ArrayList<>();

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

    public void insertDataFromExcelToSimpleTables(Table table) {
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
            System.out.println("le tableau " + table.name + " a " + totalRowsAffected + " lignes insérées. ");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertDataFromUserToEntiteLivraisonWithForeignKeyCheck(String tableName) {
        Scanner scanner = new Scanner(System.in);

        // Read existing data from the table
        Table table = new MySQLDataReader(DB_URL, USER, PASS).readDataFromMySQL(tableName);

        // Create a list to store user input values
        ArrayList<Object> userInputs = new ArrayList<>();

        // Collect additional user input for new data
        System.out.print("Donnez-moi la valeur pour ID (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextDouble());
        System.out.print("Donnez-moi la valeur pour ID_Departement (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextDouble());
        System.out.print("Donnez-moi la valeur pour ID_Projet (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextDouble());
        System.out.print("Donnez-moi la valeur pour ID_Client (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextDouble());
        System.out.print("Donnez-moi la valeur pour Numero_livraison (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.next());
        System.out.print("Donnez-moi la valeur pour Date_de_livraison (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.next());
        System.out.print("Donnez-moi la valeur pour Status_facturation (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextBoolean());
        System.out.print("Donnez-moi la valeur pour Status_livraison (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextBoolean());
        System.out.print("Donnez-moi la valeur pour Status_situation (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextBoolean());
        System.out.print("Donnez-moi la valeur pour Status_PV_de_reception (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.nextBoolean());
        System.out.print("Donnez-moi la valeur pour Lettrage (nous travaillons sur la table Entite_Livraison): ");
        userInputs.add(scanner.next());

        // Create a new table with user input values
        Table updatedTable = new Table(tableName, table.attributes, userInputs);

        List<Object> unexistkeysforID_Departement = getNonExistingForeignKeys("Entite_departement", "ID", "ID_Departement", updatedTable);
        for (int i = 0; i < unexistkeysforID_Departement.size(); i++) {
            int foreignKey = ((Number) unexistkeysforID_Departement.get(i)).intValue();
            insertDataFromUserToSimpleTable("Entite_departement", foreignKey);
        }

        List<Object> unexistkeysforID_Client = getNonExistingForeignKeys("Entite_Client", "ID", "ID_Client", updatedTable);
        for (int i = 0; i < unexistkeysforID_Client.size(); i++) {
            int foreignKey = ((Number) unexistkeysforID_Client.get(i)).intValue();
            insertDataFromUserToSimpleTable("Entite_Client", foreignKey);
        }
        List<Object> unexistkeysforID_Projet = getNonExistingForeignKeys("Entite_Projet", "ID", "ID_Projet", updatedTable);
        for (int i = 0; i < unexistkeysforID_Projet.size(); i++) {
            int foreignKey = ((Number) unexistkeysforID_Projet.get(i)).intValue(); // Corrected line
            insertDataToEntiteProjetWithForeignKeys(foreignKey);
        }
        insertDataFromExcelToSimpleTables(updatedTable);
    }

}





















