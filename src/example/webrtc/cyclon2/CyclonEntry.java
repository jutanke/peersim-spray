package example.webrtc.cyclon2;

import peersim.core.Node;

import java.util.Comparator;

public class CyclonEntry implements Comparable<CyclonEntry>, Comparator<CyclonEntry> {

    public Node n;
    public int age;

    public CyclonEntry(){}

    public CyclonEntry(Node n, int age) {
        this.n = n;
        this.age = age;
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

    public void increase() {
        this.age++;
    }
}
