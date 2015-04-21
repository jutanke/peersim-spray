package example.cyclon;

import peersim.core.Node;
import java.util.Comparator;

/**
 * Heavily inspired by https://code.google.com/p/peersim-sn/source/browse/trunk/src/example/cyclon/
 *
 * Created by julian on 26/01/15.
 */
public class CyclonEntry implements Comparable<CyclonEntry>, Comparator<CyclonEntry> {
    public int age;
    public Node n;

    public CyclonEntry(){}

    public CyclonEntry(int age, Node n) {
        this.age = age;
        this.n = n;
    }

    @Override
    public int compareTo(CyclonEntry ce) {
        if (ce.age > age)
            return 1;
        else if (ce.age == age)
            return 0;
        return -1;
    }

    @Override
    public int compare(CyclonEntry ce1, CyclonEntry ce2) {
        if (ce1.age > ce2.age)
            return 1;
        else if (ce1.age == ce2.age)
            return 0;
        return -1;
    }

    @Override
    public String toString(){
        return "{" + n.getID() + "|age:" + this.age + "}";
    }
}
