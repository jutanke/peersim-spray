package example.scamp;

import peersim.dynamics.DynamicNetwork;

/**
 * CODE TAKEN FROM: https://github.com/csko/Peersim/tree/master/scamp
 *
 * Created by julian on 28/01/15.
 */
public class Unsubscribe extends DynamicNetwork {

    /**
     * Standard constructor that reads the configuration parameters.
     * Invoked by the simulation engine.
     *
     * @param prefix the configuration prefix for this class
     */
    public Unsubscribe(String prefix) {
        super(prefix);
    }
}
