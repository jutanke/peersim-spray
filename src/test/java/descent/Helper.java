package descent;

import peersim.core.Node;
import peersim.core.Protocol;

/**
 * Created by julian on 04/05/15.
 */
public final class Helper {
    private Helper(){}


    public static Node createNode(final long id) {
        return new Node() {
            public Protocol getProtocol(int i) {
                return null;
            }

            public int protocolSize() {
                return 0;
            }

            public void setIndex(int i) {

            }

            public int getIndex() {
                return 0;
            }

            public long getID() {
                return id;
            }

            public int getFailState() {
                return 0;
            }

            public void setFailState(int i) {

            }

            public Object clone() {
                try {
                    return super.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            public boolean isUp() {
                return true;
            }
        };
    }
}
