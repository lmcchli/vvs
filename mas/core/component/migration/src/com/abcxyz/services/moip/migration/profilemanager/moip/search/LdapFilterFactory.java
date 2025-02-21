/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip.search;

import com.abcxyz.services.moip.migration.profilemanager.moip.ProfileMetaData;
import com.mobeon.common.util.criteria.Criteria;
import com.mobeon.masp.profilemanager.UnknownAttributeException;

import java.util.List;
import java.util.Map;

/**
 * Factory for creating LDAP filter from Profile Criteria trees
 */
public class LdapFilterFactory extends ProfileCriteraIsomorphBuilder<String> {

    private Map<String, ProfileMetaData> attributeMap;

    private boolean unknownAttribute = false;

    /**
     * Creates an LDAP filter from a ProfileCriteria tree
     *
     * @param c            start node for the ProfileCriteria tree
     * @param attributeMap the application attribute name meta data map
     * @return an LDAP filter corresponding to the ProfileCriteria tree
     * @throws UnknownAttributeException if an application attribute cannot be mapped to a corresponding
     *                                   user register attribute name. The unknown attributes are listed in the exception as: " >>unknown<< =value"
     *                                   where "unknown" is the name of the unknown attribute.
     */
    public static String getLdapFilter(Criteria<ProfileCriteriaVisitor> c, Map<String, ProfileMetaData> attributeMap)
            throws UnknownAttributeException {

        LdapFilterFactory f = new LdapFilterFactory(c.getParent(), attributeMap);
        c.accept(f);
        if (f.isUnknownAttribute()) {
            throw new UnknownAttributeException("Unknown attribute(s): " + f.getIsomorph());
        }
        return f.getIsomorph();
    }


    public LdapFilterFactory(Object startParent, Map<String, ProfileMetaData> attributeMap) {
        super(startParent);
        this.attributeMap = attributeMap;
    }


    protected String createProfileStringCriteriaIsomorph(ProfileStringCriteria stringCriteria) throws UnknownAttributeException {
        StringBuffer b = new StringBuffer();
        b.append("(");
        b.append(getUserRegisterName(stringCriteria.getName()));
        b.append("=");
        b.append(stringCriteria.getValue());
        b.append(")");
        return b.toString();
    }

    protected String createProfileBooleanCriteriaIsomorph(ProfileBooleanCriteria booleanCriteria) {
        StringBuffer b = new StringBuffer();
        b.append("(");
        b.append(getUserRegisterName(booleanCriteria.getName()));
        b.append("=");
        b.append(Boolean.toString(booleanCriteria.getValue()));
        b.append(")");
        return b.toString();
    }

    protected String createProfileIntegerCriteriaIsomorph(ProfileIntegerCriteria integerCriteria) {
        StringBuffer b = new StringBuffer();
        b.append("(");
        b.append(getUserRegisterName(integerCriteria.getName()));
        b.append("=");
        b.append(Integer.toString(integerCriteria.getValue()));
        b.append(")");
        return b.toString();
    }

    /**
     * Creates a NOT criteria isomorph from another isomorph.
     *
     * @param isomorph isomorph to negated.
     * @return a NOT criteria isomorph.
     */
    protected String createNotCriteriaIsomorph(String isomorph) {
        return "(!" + isomorph + ")";
    }

    /**
     * Creates an AND criteria isomorph from a collection of isomorphs.
     *
     * @param isomorphs isomorphs to be conjunctioned.
     * @return a AND criteria isomorph.
     */
    protected String createAndCriteriaIsomorph(List<String> isomorphs) {
        StringBuffer sb = new StringBuffer("(&");
        for (String s : isomorphs) {
            sb.append(s);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Creates an OR criteria isomorph from a collection of isomorphs.
     *
     * @param isomorphs isomorphs to be disjunctioned.
     * @return an OR criteria isomorph.
     */
    protected String createOrCriteriaIsomorph(List<String> isomorphs) {
        StringBuffer sb = new StringBuffer("(|");
        for (String s : isomorphs) {
            sb.append(s);
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Retrieves the User Register attribute name for the supplied application attribute name
     *
     * @param name application attribute name to get User Register representation of
     * @return the User Register attribute name corresponding to the application name
     */
    private String getUserRegisterName(String name) {
        if (attributeMap.containsKey(name)) {
            return attributeMap.get(name).getUserRegisterName();
        } else {
            unknownAttribute = true;
            return " >>" + name + "<< ";
        }
    }

    public boolean isUnknownAttribute() {
        return unknownAttribute;
    }
}
