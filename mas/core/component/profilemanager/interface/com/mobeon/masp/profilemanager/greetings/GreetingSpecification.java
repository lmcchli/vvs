/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

import java.util.Set;
import java.util.EnumSet;

/**
 * Class for specifying a specific greeting to be stored or retrieved
 */
public class GreetingSpecification {
    private GreetingType type;
    private GreetingFormat format;
    private String subId;
    private String duration = null;
    private volatile int hashCode = 0;

    private static final Set<GreetingType> SUBID_TYPES = EnumSet.of(
            GreetingType.CDG,
            GreetingType.DIST_LIST_SPOKEN_NAME
    );

    public GreetingSpecification() {
        this("allcalls", GreetingFormat.VOICE, null);
    }

    public GreetingSpecification(String type, GreetingFormat format) {
        this(type, format, null);
    }

    public GreetingSpecification(String type, GreetingFormat format, String subId) {
        setType(type);
        setFormat(format);
        setSubId(subId);
    }

    protected GreetingSpecification(GreetingType type, GreetingFormat format) {
        this(type, format, null);
    }

    protected GreetingSpecification(GreetingType type, GreetingFormat format, String subId) {
        this.type = type;
        this.format = format;
        this.subId = subId;
    }

    protected GreetingSpecification(GreetingType type, GreetingFormat format, String subId, String duration) {
        this.type = type;
        this.format = format;
        this.subId = subId;
        this.duration = duration;
    }

    public GreetingType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = GreetingType.getValueOf(type);
    }

    public GreetingFormat getFormat() {
        return format;
    }

    public void setFormat(GreetingFormat format) {
        this.format = format;
    }

    /**
     * Retrieves the subid of the greeting specification
     * @return the subid of the greeting specification, if no subid has been specified, null is returned
     */
    public String getSubId() {
        return subId;
    }

    /**
     * Sets the subid of the greeting specification
     * @param subId the subid of the greeting specification
     */
    public void setSubId(String subId) {
        this.subId = subId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * Tests the validity of the greeting specification
     * @return true, if the greeting specification is valid, otherwise false
     */
    public boolean isValid() {
        if (SUBID_TYPES.contains(getType())) {
            if (getSubId() == null) {
                return false;
            }
        } else {
            // All other should not have a subid
            if (getSubId() != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    public String toString() {
        StringBuffer message = new StringBuffer();
        message.append(type);
        message.append("(").append(format).append(")");
        if (subId != null) {
            message.append("[").append(subId).append("]");
        }
        if (duration != null) {
            message.append("[").append(duration).append("]");
        }
        return message.toString();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof GreetingSpecification) {
            GreetingSpecification specification = (GreetingSpecification)obj;
            return (type == specification.type &&
                    format == specification.format &&
                    (subId == null ? specification.subId == null : subId.equals(specification.subId))
            );
        }
        return false;
    }

    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            result = 37 * result + type.hashCode();
            result = 37 * result + format.hashCode();
            result = 37 * result + (subId == null ? 0 : subId.hashCode());
            hashCode = result;
        }
        return hashCode;
    }
}
