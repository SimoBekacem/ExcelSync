import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Give the path of you Excel file in you computer :");
        String filePath = scanner.next(); // put here the path of your file exemple ( "/Users/macintoch/Downloads/Notes.xlsx")
        ExcelDataReader reader = new ExcelDataReader(filePath);
        System.out.println(reader.readDataFromExcel().attributes);
    }
}