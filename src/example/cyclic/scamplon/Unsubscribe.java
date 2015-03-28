package example.cyclic.scamplon;/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

import peersim.config.Configuration;
import peersim.core.*;
import peersim.dynamics.DynamicNetwork;

/**
 * A network dynamics manager which can unsubscribe nodes according to the
 * SCAMP protocol. If used for adding nodes then it works like its superclass.
 * Since it is not intended to be used for increasing networks, a warning
 * is given in that case.
 */
public class Unsubscribe extends DynamicNetwork {


// ========================= fields =================================
// ==================================================================


    /**
     * The protocol to operate on. It has to be a scamp protocol.
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /**
     * The protocol we want to wire
     */
    private final int protocolID;


// ====================== initialization ===============================
// =====================================================================


    public Unsubscribe( String prefix ) {

        super(prefix);
        if( add > 0 )
            System.err.println("Scamp.Unsubscribe: not supposed to be"+
                    " used for growing networks");
        protocolID = Configuration.getInt(prefix+"."+PAR_PROT);
    }


// ===================== protected methods ==============================
// ======================================================================

    /**
     * Removes n random nodes from the network. Before removal, the unsubscription
     * protocol is run.
     * @param n
     *          the number of nodes to remove
     */
    protected void remove( int n ) {

        for(int i=0; i<n; ++i)
        {
            Network.swap(
                    Network.size()-1,
                    CommonState.r.nextInt(Network.size()) );
            Scamp.unsubscribe(
                    Network.get(Network.size()-1),
                    protocolID );
            Network.remove();
        }
    }


}
