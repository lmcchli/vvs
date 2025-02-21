/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "InstanceVariableMayNotBeInitialized"})
public class TagType {

    private List<TagType> type;
    private String typeInstance;


    public TagType() {
    }

    private TagType(TagType tt) {
        if (tt.type != null) {
            type = new ArrayList<TagType>(tt.type);
        }
    }

    private TagType(TagType superType, String subType) {
        if (superType.type == null)
            superType.type = new ArrayList<TagType>(5);
        type = superType.type;
        typeInstance = subType;
        type.add(this);
    }

    public TagType refine(String subType) {
        return new TagType(this, subType);
    }

    public TagType broaden(String subType) {
        for (int i = type.size(); i > 0; i--) {
            if (checkType(type.get(i), subType))
                return type.get(i);
            else
                type.remove(i);
        }
        return type.get(0);
    }

    public boolean isTypeOf(String ... subType) {
        if (type == null) {
            return subType.length == 0;
        }
        int typeLength = type.size() - subType.length;
        if (typeLength >= 0) {
            for (int i = typeLength, j = 0; i < type.size(); i++, j++) {
                if (!checkType(type.get(i), subType[j]))
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isLeafTypeOf(String ... subType) {
        if (type == null) {
            return false;
        }
        int typeLength = type.size() - subType.length - 1;
        if (typeLength >= 0) {
            for (int i = typeLength, j = 0; j < subType.length; i++, j++) {
                if (!checkType(type.get(i), subType[j]))
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }



    public boolean isChildTypeOf(Set<String> parentTypes) {
        if (type == null) {
            return false;
        }
        if (type.size() >= 0) {
            for (TagType aType : type) {
                if (checkType(aType, parentTypes))
                    return true;
            }
            return false;
        } else {
            return false;
        }
    }


    public boolean isChildTypeOf(String parentType) {
        if (type == null) {
            return false;
        }
        if (type.size() >= 0) {
            for (TagType aType : type) {
                if (checkType(aType, parentType))
                    return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean checkType(TagType aType, String parentType) {
        return aType.typeInstance.equals(parentType);
    }

    private boolean checkType(TagType aType, Set<String> parentType) {
        return parentType.contains(aType.typeInstance);
    }

    public TagType clone() {
        return new TagType(this);
    }

    public String toString() {
        StringBuilder buf = new StringBuilder("[]");
        for (TagType aType : type) {
            buf.append("->");
            buf.append(aType.typeInstance);
        }
        return "TagType{" + buf + '}';
    }

    public String getTypeInstance() {
        return typeInstance;
    }

}
