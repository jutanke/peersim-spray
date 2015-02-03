package example.scampXcyclon;

import peersim.core.Node;

/**
 * Created by julian on 2/3/15.
 */
public class PartialViewEntry {

    public final Node node;
    public int age;


    public PartialViewEntry(Node node) {
        this.node = node;
        this.age = 0;
    }

    public static PartialViewEntry copy(PartialViewEntry e) {
        PartialViewEntry result = new PartialViewEntry(e.node);
        result.age = e.age;
        return result;
    }

    @Override
    public String toString() {
        return "{" + node.getID() + "|" + age + "}";
    }
}
