package example.Scamplon;

import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by julian on 2/5/15.
 */
public class Subscribe implements Control {

    public Subscribe(String prefix) {}

    @Override
    public boolean execute() {

        for (int i = 0; i < Network.size(); i++) {
            Node n = Network.get(i);
            Scamplon.subscribe(n, i);
        }

        return false;
    }
}
