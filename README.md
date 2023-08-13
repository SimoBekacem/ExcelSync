# ExcelSync
## Set up the database :
for make this program run successfully you should make the tables in the data base fisrt with thos names:
+--------------------------+
| Tables_in_canevas_access |
+--------------------------+
| Entité_Chef_de_Projet    |
| Entité_Client            |
| Entité_département       |
| Entité_Livraison         |
| Entité_Projet            |
+--------------------------+
then connect to the database in MySQLDataReader class :
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Canevas_Access";
    private static final String USER = "root";
    private static final String PASS = "simo2002";
## the format of the Excel table to import :
for the 02 chois the Excel format should be like the Entité_Livraison.xlsx file.
for the 05 chois the Excel format should be like the Update.xlsx file.
