/*
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
package example.scamp.old;

import peersim.config.Configuration;
import peersim.core.Control;

/**
 * CODE TAKEN FROM: https://github.com/csko/Peersim/tree/master/scamp
 *
 * <p/>
 * Created by julian on 28/01/15.
 */
public class HealthTest implements Control {

    // ===================== fields =======================================
    // ====================================================================

    /**
     * The protocol to operate on.
     *
     * @config
     */
    private static final String PAR_PROT = "protocol";

    /**
     * The name of this observer in the configuration
     */
    private final String name;

    private final int protocolID;


    // ===================== initialization ================================
    // =====================================================================


    public HealthTest(String name) {

        this.name = name;
        protocolID = Configuration.getInt(name + "." + PAR_PROT);
    }


    // ====================== methods ======================================
    // =====================================================================


    public boolean execute() {

        System.out.println(name + ": " + Scamp.test(protocolID));

        return false;
    }
}
