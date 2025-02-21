/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.outdial;

class DefaultOdlFactory extends OdlFactory {

    /** Default Constructor */
    DefaultOdlFactory() {
    }

    @Override
    public IEventStore createEventStore() {
        return new MfsEventStore();
    }
}
