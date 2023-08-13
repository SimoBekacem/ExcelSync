import java.sql.*;
import java.util.*;
import java.util.Date;

public class MySQLDataReader {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Canevas_Access";
    private static final String USER = "root";
    private static final String PASS = "simo2002";

    private Connection connection;

    public MySQLDataReader() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected to the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



//    this methode uses selectRecordByAttribut, selectRecordByAttributes, insertValuesIntoTable in order to ask the use some quetient
//    about a new livraison and create it in the table Entite_Livraison (03).
    public void insertDataFromUserToEntiteLivraison() {
        Scanner scanner = new Scanner(System.in);
        // getting the name of the project :
        System.out.println("Donnez-moi le Intitulé de projet : ");
        String Intitule_value = scanner.next();
        int ID_Projet = getIdOfExisteAttribute("Entité_Projet", "Intitulé", Intitule_value);
        if (ID_Projet == 0){
            System.out.println("Donnez-moi le nom de Chef de Projet :");
            String Nom_Chef = scanner.next();
            System.out.println("Donnez-moi le prenom de Chef de Projet :");
            String Prenom_Chef = scanner.next();
            int ID_Chef_Projet = getIdOfExisteAttributes("Entité_Chef_de_Projet", "Nom", Nom_Chef,"Prénom", Prenom_Chef);
            System.out.println("Donnez-moi le Code de projet");
            String CODE_value_projet = scanner.next();
            if (ID_Chef_Projet != 0){
                insertValuesIntoTable("Entité_Projet", "ID_chef_de_projet, CODE, Intitulé","\""+ID_Chef_Projet+"\", "+"\""+CODE_value_projet +"\", "+"\""+Intitule_value+"\"");
            }else {
                insertValuesIntoTable("Entité_Chef_de_Projet", "Nom, Prénom","\""+Nom_Chef+"\", "+"\""+Prenom_Chef+"\"");
                int ID_Chef_Projet_new = getIdOfExisteAttributes("Entité_Chef_de_Projet", "Nom", Nom_Chef,"Prénom", Prenom_Chef);
                insertValuesIntoTable("Entité_Projet", "ID_chef_de_projet, CODE, Intitulé","\""+ID_Chef_Projet_new+"\", "+"\""+CODE_value_projet +"\", "+"\""+Intitule_value+"\"");
            }
            ID_Projet = getIdOfExisteAttribute("Entité_Projet", "Intitulé", Intitule_value);
        }
        // getting the name of the client :
        System.out.println("Donnez-moi le Nom de Client de ce projet : ");
        String Client_value = scanner.next();
        System.out.println("Donnez-moi le Code de Client :");
        String CODE_value_client = scanner.next();
        int ID_Client = getIdOfExisteAttributes("Entité_Client", "Nom", Client_value, "CODE", CODE_value_client);
        if (ID_Client == 0){
            insertValuesIntoTable("Entité_Client", "CODE, Nom","\""+CODE_value_client+"\", "+"\""+Client_value+"\"");
            ID_Client = getIdOfExisteAttributes("Entité_Client", "Nom", Client_value, "CODE", CODE_value_client);
        }
        // getting the name of the Département :
        System.out.println("Donnez-moi le Nom de Département de ce projet : ");
        String Département_value = scanner.next();
        System.out.println("Donnez-moi le Code de Département");
        String CODE_value_departement = scanner.next();
        int ID_Département = getIdOfExisteAttributes("Entité_département", "Nom",Département_value, "CODE", CODE_value_departement);
        if (ID_Département == 0){
            insertValuesIntoTable("Entité_département", "CODE, Nom","\""+CODE_value_departement+"\", "+"\""+Département_value+"\"");
            ID_Département = getIdOfExisteAttributes("Entité_département", "Nom",Département_value, "CODE", CODE_value_departement);
        }
        System.out.println("Donnez-moi le Numéro livraision de ce projet : ");
        String Numéro_livraision = scanner.next();
        System.out.println("Donnez-moi la Date de livraison de ce projet : ");
        String Date_livraison = scanner.next();
        System.out.println("Donnez-moi la statut facturation de ce projet : ");
        int statut_facturation = scanner.nextBoolean()? 1:0;
        System.out.println("Donnez-moi le Numéro de bon : ");
        String numero_bon = scanner.next();
        System.out.println("Donnez-moi la status livraison de ce projet : ");
        int statut_livraison = scanner.nextBoolean()? 1:0;
        System.out.println("Donnez-moi la status situation de ce projet : ");
        int statut_situation = scanner.nextBoolean()? 1:0;
        System.out.println("Donnez-moi la status PV de réception de ce projet : ");
        int statut_réception = scanner.nextBoolean()? 1:0;
        System.out.println("Donnez-moi le Lettrage de ce projet (si elle n'existe pas, écrivez 'null'): ");
        String Lettrage = scanner.next();
        if(Lettrage.equals("null")){
            insertValuesIntoTable("Entité_Livraison", "ID_Département, ID_Projet, ID_Client, Numéro_livraison, Date_de_livraison, statut_facturation,  Numéro_de_bon, status_livraison, status_situation, status_PV_de_réception","\""+ID_Département+"\", "+"\""+ID_Projet+"\", "+"\""+ID_Client+"\", "+"\""+Numéro_livraision+"\", "+"\""+Date_livraison+"\", "+"\""+statut_facturation+"\", "+"\""+numero_bon+"\", "+"\""+statut_livraison+"\", "+"\""+statut_situation+"\", "+"\""+statut_réception+"\"");
        } else {
            insertValuesIntoTable("Entité_Livraison", "ID_Département, ID_Projet, ID_Client, Numéro_livraison, Date_de_livraison, statut_facturation,  Numéro_de_bon, status_livraison, status_situation, status_PV_de_réception, Lettrage","\""+ID_Département+"\", "+"\""+ID_Projet+"\", "+"\""+ID_Client+"\", "+"\""+Numéro_livraision+"\", "+"\""+Date_livraison+"\", "+"\""+statut_facturation+"\", "+"\""+numero_bon+"\", "+"\""+statut_livraison+"\", "+"\""+statut_situation+"\", "+"\""+statut_réception+"\", "+"\""+Lettrage+"\"");
        }
        System.out.println("Votre nouvelle ligne a été ajoutée avec succès.");
    }

//    this methode take the name of the attribute and the value, and it searches for if it's existe in the table that calls tableName.
    public int getIdOfExisteAttribute(String tableName, String attribute, String attValue) {
        String selectQuery = "SELECT ID FROM " + tableName + " WHERE " + attribute + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setString(1, attValue);
            ResultSet resultSet = statement.executeQuery();
            int id = 0;
            while (resultSet.next()) {
                id = resultSet.getInt("ID");
            }
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

//    this methode is like selectRecordByAttribute methode but instead to take one attribute and one value it take tow .
    public int getIdOfExisteAttributes(String tableName, String attribute1, String attValue1, String attribute2, String attValue2) {
        String selectQuery = "SELECT ID FROM " + tableName + " WHERE " + attribute1 + " = ? AND " + attribute2 + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setString(1, attValue1);
            statement.setString(2, attValue2);
            ResultSet resultSet = statement.executeQuery();
            int id = 0;
            while (resultSet.next()) {
                id = resultSet.getInt("ID");
            }
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

//    this methode take the attributes and there values and insete them into the table that calls tableName.
    public void insertValuesIntoTable(String tableName, String attribute, String attValue) {
        String insertQuery = "INSERT INTO " + tableName + " (" + attribute + ") VALUES (" + attValue + ")";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




//    this methode take the values from the Entite_Livraison in the database and create an Excel table that has those attributes (01):
    public void getExcelTableOfEntiteLivraisonFromDatabase(List<List<Integer>> IDs, String filename){
        Scanner scanner = new Scanner(System.in);
        List<String> attributes = Arrays.asList(new String[]{"Numéro de livraison", "Intitulé de projet", "CODE de projet", "Nom de chef de projet", "Prénom de chef de projet", "Nom de département", "CODE de département", "Nom de client", "CODE de client", "Date de livraiseon", "Statut facturation", "Numéro de bon", "Statut de livraison", "Statut de situation", "Status PV de réception", "Lettrage"});
        ArrayList<Object> values = new ArrayList<>() ;
        for (int i = 0; i < IDs.get(0).size(); i++) {
            String numero = getStringAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "Numéro_livraison");
            values.add(numero);
            String intitule = getStringAttributeValueById("Entité_Projet",IDs.get(2).get(i), "Intitulé");
            values.add(intitule);
            String code_projet = getStringAttributeValueById("Entité_Projet",IDs.get(2).get(i), "CODE");
            values.add(code_projet);
            int id_chef_projet = getIntAttributeValueById("Entité_Projet",IDs.get(2).get(i), "ID_chef_de_projet");
            String Nom_chef = getStringAttributeValueById("Entité_Chef_de_Projet",id_chef_projet, "Nom");
            values.add(Nom_chef);
            String Prenom_chef = getStringAttributeValueById("Entité_Chef_de_Projet",id_chef_projet, "Prénom");
            values.add(Prenom_chef);
            String nom_departement = getStringAttributeValueById("Entité_département",IDs.get(1).get(i), "Nom");
            values.add(nom_departement);
            String code_departement = getStringAttributeValueById("Entité_département",IDs.get(1).get(i), "CODE");
            values.add(code_departement);
            String nom_client = getStringAttributeValueById("Entité_Client",IDs.get(3).get(i), "Nom");
            values.add(nom_client);
            String code_client = getStringAttributeValueById("Entité_Client",IDs.get(3).get(i), "CODE");
            values.add(code_client);
            Date date_livraison = getDateAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "Date_de_livraison");
            values.add(date_livraison);
            Boolean statut_facturation = (getIntAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "statut_facturation") == 1);
            values.add(statut_facturation);
            String numero_bon = getStringAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "Numéro_de_bon");
            values.add(numero_bon);
            Boolean statut_livraison = (getIntAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "status_livraison") == 1);
            values.add(statut_livraison);
            Boolean statut_situation = (getIntAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "status_situation") == 1);
            values.add(statut_situation);
            Boolean statut_pv_reception = (getIntAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "status_PV_de_réception") == 1);
            values.add(statut_pv_reception);
            String Lettrage = getStringAttributeValueById("Entité_Livraison",IDs.get(0).get(i), "Lettrage");
            values.add(Lettrage);
        }
        Table table = new Table(attributes,values);
        ExcelDataReader Exceltable = new ExcelDataReader();
        System.out.println("indiquez-moi l'endroit où vous souhaitez que je place votre dossier :");
        String path = scanner.next();
        Exceltable.generateExcelFile(table,path,filename);
    }
//    this methode get the all ids from EntiteLivraison in order to get the data from other tables :
    public List<List<Integer>> getIdsFromTable(String tableName) {
        List<List<Integer>> IDs = new ArrayList<>();

        List<Integer> idList = new ArrayList<>();
        List<Integer> id_departementList = new ArrayList<>();
        List<Integer> id_projetList = new ArrayList<>();
        List<Integer> id_clientList = new ArrayList<>();

        String selectQuery = "SELECT ID, ID_Département, ID_Projet, ID_Client FROM " + tableName;
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                int id_departement = resultSet.getInt("ID_Département");
                int id_projet = resultSet.getInt("ID_Projet");
                int id_client = resultSet.getInt("ID_Client");
                idList.add(id);
                id_departementList.add(id_departement);
                id_projetList.add(id_projet);
                id_clientList.add(id_client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        IDs.add(idList);
        IDs.add(id_departementList);
        IDs.add(id_projetList);
        IDs.add(id_clientList);
        return IDs;
    }
//    those three methode are a little the same:
//    this method is for getting the datatype String values where the id = ?:
    public String getStringAttributeValueById(String tableName, int id, String attribute) {
        String selectQuery = "SELECT " + attribute + " FROM " + tableName + " WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString(attribute);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    //    this method is for getting the datatype int values where the id = ?:
    public int getIntAttributeValueById(String tableName, int id, String attribute) {
        String selectQuery = "SELECT " + attribute + " FROM " + tableName + " WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(attribute);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // You can choose a default value here
    }
    //    this method is for getting the datatype Date values where the id = ?:
    public java.sql.Date getDateAttributeValueById(String tableName, int id, String attribute) {
        String selectQuery = "SELECT " + attribute + " FROM " + tableName + " WHERE ID = ?";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDate(attribute);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }






    //    this methode take the data from the Excel table and put it in the table Entite_Livraison in the database (02):
    public void insertDataFromExcelToEntiteLivraison() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Donnez-moi le chemin de fichier Excel :");
        String path = scanner.next();
        Table table = new ExcelDataReader().readExcelFile(path);
        List<String> numbersfromExceltabel = new ArrayList<>();
        for (int i = 0; i < table.values.size(); i+=table.attributes.size()) {
            numbersfromExceltabel.add(convertToString(table.values.get(i)));
        }
//    this part is for adding the rows that are in the Excel file an not in the table in database:
        List<String> nonexistednumbers = filterNonExistingNumbers(numbersfromExceltabel,"Numéro_livraison");
        List<Object> nonexisteddata = new ArrayList<>();
        for (int j = 0; j < nonexistednumbers.size(); j++) {
            for (int i = 0; i < table.values.size(); i+=table.attributes.size()) {
                if(table.values.get(i).equals(nonexistednumbers.get(j))){
                    for (int k = 0; k < table.attributes.size(); k++) {
                        nonexisteddata.add(table.values.get(i+k));
                    }
                }
            }
        }
        for (int i = 0; i < nonexisteddata.size(); i+=table.attributes.size()) {
            String Intitule_value = convertToString(nonexisteddata.get(i+1));
            int ID_Projet = getIdOfExisteAttribute("Entité_Projet", "Intitulé", Intitule_value);
            if (ID_Projet == 0){
                String Nom_Chef = convertToString(nonexisteddata.get(i+3));
                String Prenom_Chef = convertToString(nonexisteddata.get(i+4));
                int ID_Chef_Projet = getIdOfExisteAttributes("Entité_Chef_de_Projet", "Nom", Nom_Chef,"Prénom", Prenom_Chef);
                String CODE_value_projet = convertToString(nonexisteddata.get(i+2));
                if (ID_Chef_Projet != 0){
                    insertValuesIntoTable("Entité_Projet", "ID_chef_de_projet, CODE, Intitulé","\""+ID_Chef_Projet+"\", "+"\""+CODE_value_projet +"\", "+"\""+Intitule_value+"\"");
                }else {
                    insertValuesIntoTable("Entité_Chef_de_Projet", "Nom, Prénom","\""+Nom_Chef+"\", "+"\""+Prenom_Chef+"\"");
                    int ID_Chef_Projet_new = getIdOfExisteAttributes("Entité_Chef_de_Projet", "Nom", Nom_Chef,"Prénom", Prenom_Chef);
                    insertValuesIntoTable("Entité_Projet", "ID_chef_de_projet, CODE, Intitulé","\""+ID_Chef_Projet_new+"\", "+"\""+CODE_value_projet +"\", "+"\""+Intitule_value+"\"");
                }
                ID_Projet = getIdOfExisteAttribute("Entité_Projet", "Intitulé", Intitule_value);
            }
            // getting the name of the client :
            String Client_value = convertToString(nonexisteddata.get(i+7));
            String CODE_value_client = convertToString(nonexisteddata.get(i+8));
            int ID_Client = getIdOfExisteAttributes("Entité_Client", "Nom", Client_value, "CODE", CODE_value_client);
            if (ID_Client == 0){
                insertValuesIntoTable("Entité_Client", "CODE, Nom","\""+CODE_value_client+"\", "+"\""+Client_value+"\"");
                ID_Client = getIdOfExisteAttributes("Entité_Client", "Nom", Client_value, "CODE", CODE_value_client);
            }
            // getting the name of the Département :
            String Département_value = convertToString(nonexisteddata.get(i+5));
            String CODE_value_departement = convertToString(nonexisteddata.get(i+6));
            int ID_Département = getIdOfExisteAttributes("Entité_département", "Nom",Département_value, "CODE", CODE_value_departement);
            if (ID_Département == 0){
                insertValuesIntoTable("Entité_département", "CODE, Nom","\""+CODE_value_departement+"\", "+"\""+Département_value+"\"");
                ID_Département = getIdOfExisteAttributes("Entité_département", "Nom",Département_value, "CODE", CODE_value_departement);
            }
            String Numéro_livraision = convertToString(nonexisteddata.get(i));
            String Date_livraison = convertToString(nonexisteddata.get(i+9));
            int statut_facturation =(boolean) nonexisteddata.get(i+10)? 1:0;
            String numero_bon = convertToString(nonexisteddata.get(i+11));
            int statut_livraison = (boolean) nonexisteddata.get(i+12)? 1:0;
            int statut_situation = (boolean) nonexisteddata.get(i+13)? 1:0;
            int statut_réception = (boolean) nonexisteddata.get(i+14)? 1:0;
            String Lettrage = convertToString(nonexisteddata.get(i+15));
            insertValuesIntoTable("Entité_Livraison", "ID_Département, ID_Projet, ID_Client, Numéro_livraison, Date_de_livraison, statut_facturation,  Numéro_de_bon, status_livraison, status_situation, status_PV_de_réception, Lettrage","\""+ID_Département+"\", "+"\""+ID_Projet+"\", "+"\""+ID_Client+"\", "+"\""+Numéro_livraision+"\", "+"\""+Date_livraison+"\", "+"\""+statut_facturation+"\", "+"\""+numero_bon+"\", "+"\""+statut_livraison+"\", "+"\""+statut_situation+"\", "+"\""+statut_réception+"\", "+"\""+Lettrage+"\"");

        }


//     this part is for updating existing livraison numero in database :
        List<String> existednumbers = getExistingAttributeValues("Numéro_livraison");
        List<Object> existeddata = new ArrayList<>();
        for (int j = 0; j < existednumbers.size(); j++) {
            for (int i = 0; i < table.values.size(); i+=table.attributes.size()) {
                if(convertToString(table.values.get(i)).equals(existednumbers.get(j))){
                    for (int k = 0; k < table.attributes.size(); k++) {
                        existeddata.add(table.values.get(i+k));
                    }
                }
            }
        }
        for (int i = 0; i < existeddata.size(); i+=table.attributes.size()) {
            int newstatut_facturation = (Boolean) existeddata.get(i + 10) ? 1 : 0;
            updateDataInTable("Entité_Livraison", "statut_facturation", newstatut_facturation, "Numéro_livraison", convertToString(existeddata.get(i)));
            int newstatus_livraison = (Boolean) existeddata.get(i + 12) ? 1 : 0;
            updateDataInTable("Entité_Livraison", "status_livraison", newstatut_facturation, "Numéro_livraison", convertToString(existeddata.get(i)));
            int newstatus_situation = (Boolean) existeddata.get(i + 13) ? 1 : 0;
            updateDataInTable("Entité_Livraison", "status_situation", newstatut_facturation, "Numéro_livraison", convertToString(existeddata.get(i)));
            int newsstatus_PV_de_réception = (Boolean) existeddata.get(i + 14) ? 1 : 0;
            updateDataInTable("Entité_Livraison", "status_PV_de_réception", newstatut_facturation, "Numéro_livraison", convertToString(existeddata.get(i)));
        }
        System.out.println("vos données dans le fichier excel ont été insérées avec succès.");
    }

//    this method take a list of string and see the items those are not in the EntiteLivraison in column Numéro_livraison;
    public List<String> filterNonExistingNumbers(List<String> numbersToCheck,String attribuet) {
        List<String> nonExistingNumbers = new ArrayList<>();

        String selectQuery = "SELECT Numéro_livraison FROM Entité_Livraison WHERE "+ attribuet +" IN (";
        for (int i = 0; i < numbersToCheck.size(); i++) {
            if (i > 0) {
                selectQuery += ",";
            }
            selectQuery += "?";
        }
        selectQuery += ")";

        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            for (int i = 0; i < numbersToCheck.size(); i++) {
                statement.setString(i + 1, numbersToCheck.get(i));
            }
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String existingNumber = resultSet.getString("Numéro_livraison");
                numbersToCheck.remove(existingNumber);
            }

            nonExistingNumbers.addAll(numbersToCheck);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nonExistingNumbers;
    }

    //    this method returns a list of string those are in EntiteLivraison in column Numéro_livraison;
    public List<String> getExistingAttributeValues(String attribute) {
        List<String> existingAttributeValues = new ArrayList<>();

        String selectQuery = "SELECT " + attribute + " FROM Entité_Livraison";

        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                existingAttributeValues.add(resultSet.getString(attribute));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return existingAttributeValues;
    }

//    this method convert any value and return this string in string datatype
    public String convertToString(Object value) {
        if (value instanceof Integer) {
            return Integer.toString((Integer) value);
        } else if (value instanceof String) {
            return (String) value;
        }else if (value instanceof Date) {
            return value.toString(); // Convert java.sql.Date to string representation
        } else {
            return ""; // Handle other types if needed
        }
    }

//    this method do this "UPDATE tablename SET setAttribute = setValue WHERE whereAttribute = whereValue ;"
    public void updateDataInTable(String tableName, String setAttribute,int setValue, String whereAttribute, String whereValue) {
        String updateQuery = "UPDATE " + tableName + " SET " + setAttribute + " = ? WHERE " + whereAttribute + " = ?";

        try (PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setInt(1, setValue);
            statement.setString(2, whereValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }






// this method ask the use few questions and give hem an excel table with filtered data (04):
    public void gettingFiltredExcelTable(){
        Scanner scanner = new Scanner(System.in);
        List<String> stringschoisList = new ArrayList<>();
        List<Integer> intchoisList = new ArrayList<>();
        System.out.println("Est-ce qu'il est facturé (oui/non) :");
        stringschoisList.add(scanner.next());
        System.out.println("Est-ce qu'il est livré (oui/non) :");
        stringschoisList.add(scanner.next());
        System.out.println("Est-ce qu'il est situation établie (oui/non) :");
        stringschoisList.add(scanner.next());
        System.out.println("Est-ce qu'il est pv de réception etabli (oui/non) :");
        stringschoisList.add(scanner.next());
        System.out.println("Est-ce qu'il est avec lettrrage (oui/non) :");
        stringschoisList.add(scanner.next());
        for (int i = 0; i < stringschoisList.size()-1; i++) {
            if(stringschoisList.get(i).equals("oui")){
                intchoisList.add(1);
            }else if(stringschoisList.get(i).equals("non")){
                intchoisList.add(0);
            }else{
                System.out.println("L'un des choix n'est pas valide, essayez à nouveau.");
                gettingFiltredExcelTable();
            }
        }
        boolean islettrrageexest = true;
        if(stringschoisList.get(4).equals("oui")){
            islettrrageexest = true;
        }else if (stringschoisList.get(4).equals("non")){
            islettrrageexest = false;
        }else {
            System.out.println("L'un des choix n'est pas valide, essayez à nouveau.");
            gettingFiltredExcelTable();
        }
        List<List<Integer>> IDs = filteridfromEntiteLivraison("Entité_Livraison", intchoisList.get(0), intchoisList.get(1), intchoisList.get(2), intchoisList.get(3), islettrrageexest);
        getExcelTableOfEntiteLivraisonFromDatabase(IDs,"Entité_Livraison_filtred.xlsx");

}

// this method filter the ids from EntiteLivraison in the database based on user input:
    public List<List<Integer>> filteridfromEntiteLivraison(String tableName, int facture, int livre, int situation_etablie, int pv_reception_etabli, Boolean avec_lettrrage) {
        String lettrrage = avec_lettrrage ? "IS NOT NULL" : "IS NULL";
        List<List<Integer>> IDs = new ArrayList<>();

        List<Integer> idList = new ArrayList<>();
        List<Integer> id_departementList = new ArrayList<>();
        List<Integer> id_projetList = new ArrayList<>();
        List<Integer> id_clientList = new ArrayList<>();

        String selectQuery = "SELECT ID, ID_Département, ID_Projet, ID_Client FROM " + tableName + " WHERE statut_facturation = " + facture + " AND status_livraison = " + livre + " AND status_situation = " + situation_etablie + " AND status_PV_de_réception = " + pv_reception_etabli + " AND Lettrage " + lettrrage;
        try (PreparedStatement statement = connection.prepareStatement(selectQuery)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("ID");
                int id_departement = resultSet.getInt("ID_Département");
                int id_projet = resultSet.getInt("ID_Projet");
                int id_client = resultSet.getInt("ID_Client");
                idList.add(id);
                id_departementList.add(id_departement);
                id_projetList.add(id_projet);
                id_clientList.add(id_client);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        IDs.add(idList);
        IDs.add(id_departementList);
        IDs.add(id_projetList);
        IDs.add(id_clientList);
        return IDs;
    }




//    this method update the status de facturation in the database from a table excel that have 2 columns (numero de bon , status de facturation ) (05):
    public void updateFacturationStatus(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Donnez-moi le chemin de fichier Excel :");
        String path = scanner.next();
        Table table = new ExcelDataReader().readExcelFile(path);
        for (int i = 0; i < table.values.size(); i+=table.attributes.size()) {
            int factration = (Boolean) table.values.get(i+1)? 1:0;
            System.out.println(convertToString(table.values.get(i)));
            updateDataInTable("Entité_Livraison", "statut_facturation", factration, "Numéro_de_bon", convertToString(table.values.get(i)));
        }
    }
}




















