import org.apache.poi.ss.usermodel.Cell;

import java.util.ArrayList;
import java.util.Objects;

public class Table {
    Cell name ;
    ArrayList<Cell> attributes = new ArrayList<Cell>();
    ArrayList<Cell> values = new ArrayList<Cell>();

    Table(Cell name, ArrayList<Cell> attributes, ArrayList<Cell> values){
        this.name = name;
        this.attributes = attributes;
        this.values = values;
    }

}
