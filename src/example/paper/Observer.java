package example.paper;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Network;

/**
 * Created by julian on 3/15/15.
 */
public class Observer implements Control {

    private static final String PROTOCOL = "1";

    // =============================================
    // C T O R
    // =============================================

    private int pid;

    public Observer(String name) {
        //this.pid = Configuration.lookupPid(PROTOCOL);
    }

    // =============================================
    // E X E C
    // =============================================

    @Override
    public boolean execute() {

        for (int i = 0; i < Network.size(); i++) {
            GeneralNode n = (GeneralNode) Network.get(i);
            //System.out.println(n);

        }

        return false;
    }
}
