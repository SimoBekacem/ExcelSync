import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        printBanner();
        MySQLDataReader Database = new MySQLDataReader();
        Menu(Database);
    }

    private static void printBanner() {
        System.out.println("=====================================");
        System.out.println("      Bienvenue à ExcelSync ");
        System.out.println("=====================================");
    }
    private static void Menu(MySQLDataReader Database) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("01 Pour exporter un tableau de la base de données.");
        System.out.println("02 Pour importer un tableau à partir d'un fichier Excel.");
        System.out.println("03 Pour ajouter une nouvelle livraison.");
        System.out.println("04 Pour filtre le tableau livraison.");
        System.out.println("05 Pour mise a jour des bon et status de factoration.");
        System.out.println("06 Pour Sortir.");
        System.out.println();
        String choice = scanner.next();
        switch (choice){
            case("01"):
                Database.getExcelTableOfEntiteLivraisonFromDatabase(Database.getIdsFromTable("Entité_Livraison"),"Entité_Livraison.xlsx");
                Menu(Database);
                break;
            case ("02"):
                Database.insertDataFromExcelToEntiteLivraison();
                Menu(Database);
                break;
            case ("03"):
                Database.insertDataFromUserToEntiteLivraison();
                Menu(Database);
            case ("04"):
                Database.gettingFiltredExcelTable();
                Menu(Database);
            case ("05"):
                Database.updateFacturationStatus();
                Menu(Database);
            case ("06"):
                break;
        }
    }


}