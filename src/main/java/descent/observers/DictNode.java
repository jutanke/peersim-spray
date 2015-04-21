package descent.observers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 26/01/15.
 */
public class DictNode {

    public long id;
    public List<Long> neighbors;

    public DictNode(long id) {
        this.id = id;
        this.neighbors = new ArrayList<Long>();
    }

    public void reset() {this.neighbors.clear();}

    @Override
    public String toString(){
        return "(" + id + ") -> " + this.neighbors.toString();
    }
}
