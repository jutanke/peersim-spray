package example.scamp.simple;

import peersim.core.Node;

import java.util.Comparator;

/**
 * Created by julian on 29/01/15.
 */
public class ScampEntry implements Comparable<ScampEntry>, Comparator<ScampEntry> {

    public int birthdate;
    public final Node node;

    /**
     *
     * @param n
     * @param birthdate
     */
    public ScampEntry(Node n, int birthdate) {
        this.node = n;
        this.birthdate = birthdate;
    }

    @Override
    public int compareTo(ScampEntry scampEntry) {
        if (scampEntry.birthdate > birthdate) return 1;  // a lower value means
        else if (scampEntry.birthdate == birthdate) return 0;
        return -1;
    }

    @Override
    public int compare(ScampEntry s1, ScampEntry s2) {
        if (s1.birthdate > s2.birthdate) return 1;
        else if (s1.birthdate == s2.birthdate) return 0;
        return -1;
    }

    @Override
    public String toString() {
        return "{" + node.getID() + ", bd:" + birthdate +"}";
    }
}
