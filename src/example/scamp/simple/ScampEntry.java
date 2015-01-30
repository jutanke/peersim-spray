package example.scamp.simple;

import peersim.core.Node;

import java.util.Comparator;

/**
 * Created by julian on 29/01/15.
 */
public class ScampEntry implements Comparable<ScampEntry>, Comparator<ScampEntry> {

    public int age;
    public final Node node;

    /**
     *
     * @param n
     * @param age
     */
    public ScampEntry(Node n, int age) {
        this.node = n;
        this.age = age;
    }

    @Override
    public int compareTo(ScampEntry scampEntry) {
        if (scampEntry.age > age) return 1;  // a lower value means
        else if (scampEntry.age == age) return 0;
        return -1;
    }

    @Override
    public int compare(ScampEntry s1, ScampEntry s2) {
        if (s1.age > s2.age) return 1;
        else if (s1.age == s2.age) return 0;
        return -1;
    }

    @Override
    public String toString() {
        return "{" + node.getID() + ", age:" + age +"}";
    }
}
