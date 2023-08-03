import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        printBanner();
        System.out.println("Indiquez-moi l'URL de votre base de données Exemple (jdbc:mysql://localhost:3306/Canevas_Access) :");
        String DB_URL = scanner.next();
        System.out.println("Donnez-moi le nom d'utilisateur de votre base de données :");
        String USER = scanner.next();
        System.out.println("Donnez-moi le MOT DE PASSE de votre base de données :");
        String PASS = scanner.next();
        MySQLDataReader Database = new MySQLDataReader(DB_URL,USER,PASS);
        Menu(Database);

    }
    private static void printBanner() {
        System.out.println("=====================================");
        System.out.println("      Bienvenue à ExcelSync ");
        System.out.println("=====================================");
    }
    private static void Menu(MySQLDataReader Database) {
        String choice ;
        Scanner scanner = new Scanner(System.in);
        System.out.println("01 Pour exporter un tableau de la base de données.");
        System.out.println("02 Pour importer un tableau à partir d'un fichier Excel.");
        System.out.println("03 Pour ajouter une nouvelle livraison.");
        System.out.println("04 Pour Sortir.");
        System.out.println();
        choice = scanner.next();
        switch (choice){
            case("01"):
                Database.PutDataFromMySQLToExcelFile();
                Menu(Database);
                break;
            case ("02"):
                System.out.println("Donnez-moi le chemin de votre tableau Excel que vous voulez mettre dans la base de données :");
                String ExcelFilePath = scanner.next();
                if (ExcelFilePath.equals("/Users/macintoch/Desktop/Entite_Projet.xlsx")){
                    Database.insertDataToEntiteProjetWithForeignKeyCheck(ExcelFilePath);
                } else if (ExcelFilePath.equals("/Users/macintoch/Desktop/Entite_Livraison.xlsx")) {
                    Database.insertDataToEntiteLivraisonWithForeignKeyCheck(ExcelFilePath);
                } else {
                    Database.insertDataFromExcelToSimpleTables(ExcelFilePath);
                }
                Menu(Database);
                break;
            case ("03"):
                Database.insertDataFromUserToEntiteLivraisonWithForeignKeyCheck("Entite_Livraison");
                Menu(Database);
            case ("04"):
                break;
        }
    }


}