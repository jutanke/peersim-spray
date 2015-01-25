package example.webrtc.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 24/01/15.
 */
public class GraphNode {

    public int id;
    public List<Integer> neighbors;

    public GraphNode(int id) {
        this.id = id;
        this.neighbors = new ArrayList<Integer>();
    }

    public void reset(){
        this.neighbors.clear();
    }

}
