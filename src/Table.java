import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.Objects;

public class Table {
    String name ;
    ArrayList<String> attributes;
    ArrayList<Object> values ;

    Table(String name, ArrayList<String> attributes, ArrayList<Object> values){
        this.name = name;
        this.attributes = attributes;
        this.values = values;
    }

}
