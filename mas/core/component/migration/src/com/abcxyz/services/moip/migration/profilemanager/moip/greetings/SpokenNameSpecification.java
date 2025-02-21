package com.abcxyz.services.moip.migration.profilemanager.moip.greetings;

import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingType;

/**
 * GreetingSpecification allowing GreetingType instead of String for specifying greeting type. GreetingSpecification
 * is used externally and does not allow all types in the GreetingType enum. Internally all greeting types, including
 * spoken names, are using a GreetingSpecification
 *
 * @author mande
 */
public class SpokenNameSpecification extends GreetingSpecification {
    public SpokenNameSpecification(GreetingType type, GreetingFormat format) {
        super(type, format);
    }

    public SpokenNameSpecification(GreetingType type, GreetingFormat format, String subId) {
        super(type, format, subId);
    }
}
