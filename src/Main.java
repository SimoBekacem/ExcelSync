import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
//        System.out.println("Give the path of you Excel file in you computer :");
//        String filePath = scanner.next(); // put here the path of your file exemple ( "/Users/macintoch/Desktop/Entite_Client.xlsx")
//        ExcelDataReader reader = new ExcelDataReader(filePath);



//        System.out.println("Give me the DataBase Url :");
//        String DB_URL = scanner.next();
//        System.out.println("Give me the User name :");
//        String USER = scanner.next();
//        System.out.println("Give me the Password :");
//        String PASS = scanner.next();
        MySQLDataReader test = new MySQLDataReader("jdbc:mysql://localhost:3306/Canevas_Access","root","simo2002");
        test.PutDataFromMySQLToExcelFile();
//        test.insertDataFromExcelToSimpleTables("/Users/macintoch/Desktop/Entite_Livraison.xlsx");
//        test.insertDataToEntiteProjetWithForeignKeyCheck("/Users/macintoch/Desktop/Entite_Projet.xlsx");
//        test.insertDataFromUserToSimpleTable("Entite_departement", 10101010);
//         System.out.println(test.getNonExistingForeignKeys("Entite_Chef_de_Projet","ID","ID_chef_de_projet","/Users/macintoch/Desktop/Entite_Projet.xlsx" ));
//    test.insertDataToEntiteLivraisonWithForeignKeyCheck("/Users/macintoch/Desktop/Entite_Livraison.xlsx");

// /Users/macintoch/Desktop   Entite_departement Entite_Projet  Entite_Livraison



    }

}