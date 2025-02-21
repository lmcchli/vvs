package com.mobeon.masp.chargingaccountmanager;

/**
 * Contains a group of Elements that should be part of structure defined by the parent name.
 * <p/>
 * Date: 2007-dec-05
 *
 * @author emahagl
 */
public class ElementGroup {
    enum StructType {
        Struct, Array
    }

    private String parent;
    private Element[] memberElements;
    private StructType structType;

    /**
     * Constructor.
     *
     * @param parent
     * @param memberElements
     * @param structType
     */
    public ElementGroup(String parent, Element[] memberElements, StructType structType) {
        this.parent = parent;
        this.memberElements = memberElements;
        this.structType = structType;
    }

    /**
     * Constructor.
     *
     * @param parent
     * @param memberElements
     * @param structStr      array or struct
     */
    public ElementGroup(String parent, Element[] memberElements, String structStr) {
        this.parent = parent;
        this.memberElements = memberElements;
        if (structStr.equalsIgnoreCase("array")) structType = StructType.Array;
        else
            structType = StructType.Struct;
    }

    /**
     * Returns parent name
     *
     * @return parent name
     */
    public String getParent() {
        return parent;
    }

    /**
     * Returns member elements
     *
     * @return array with member elements
     */
    public Element[] getMemberElements() {
        return memberElements;
    }

    /**
     * Returns structtype
     *
     * @return the structtype
     */
    public StructType getStructType() {
        return structType;
    }
}
