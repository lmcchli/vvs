/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.qualifier;

import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.IMediaQualifierFactory;
import com.mobeon.masp.mediacontentmanager.MediaQualifierException;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;


/**
 * Default implementation of the {@link IMediaQualifierFactory} interface.
 *
 * @author Mats Egland
 */
public class MediaQualifierFactory implements IMediaQualifierFactory {

    // javadoc in implemented interface
    public IMediaQualifier create(IMediaQualifier.QualiferType type,
                                  String name,
                                  String value,
                                  IMediaQualifier.Gender gender)
            throws MediaQualifierException {
        switch (type) {
            case Number:
                if (value == null) {
                    return new NumberQualifier(name, null, gender);
                }
                try {
                    return new NumberQualifier(name,
                            Integer.parseInt(value), gender);
                } catch (NumberFormatException e) {
                    throw new MediaQualifierException(
                            "The passed value-string:" + value +
                            " could not be parsed to an Integer when creating a NumberQualifier", e);
                }
            case String:
                return new StringQualifier(name, value, gender);
            case DateDM:
                if (value == null) {
                    return new DateDMQualifier(name, null, gender);
                }
                try {
                    // DateFormat is not thread safe, use one instance for each thread.
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return new DateDMQualifier(name, dateFormat.parse(value), gender);
                } catch (ParseException e) {
                    throw new MediaQualifierException(
                            "Failed to create a DateDMQualifier from string:" + value, e);
                }
            case CompleteDate:
                if (value == null) {
                    return new CompleteDateQualifier(name, null, gender);
                }
                try {
                    // DateFormat completeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                    // Time ignored for now
                    DateFormat completeDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return new CompleteDateQualifier(name,
                            completeDateFormat.parse(value), gender);
                } catch (ParseException e) {
                    throw new MediaQualifierException(
                            "Failed to create a CompleteDateQualifier from string:" + value, e);
                }
            case WeekDay:
                if (value == null) {
                    return new WeekdayQualifier(name, null, gender);
                }
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    return new WeekdayQualifier(name, dateFormat.parse(value), gender);
                } catch (ParseException e) {
                    throw new MediaQualifierException(
                            "Failed to create a WeekdayQualifier from string:" + value, e);
                }
            case Time12:
                if (value == null) {
                    return new Time12Qualifier(name, null, gender);
                }
                try {
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    return new Time12Qualifier(name, timeFormat.parse(value), gender);
                } catch (ParseException e) {
                    throw new MediaQualifierException(
                            "Failed to create a WeekdayQualifier from string:" + value, e);
                }
            case Time24:
                if (value == null) {
                    return new Time24Qualifier(name, null, gender);
                }
                try {
                    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    return new Time24Qualifier(name, timeFormat.parse(value), gender);
                } catch (ParseException e) {
                    throw new MediaQualifierException(
                            "Failed to create a WeekdayQualifier from string:" + value, e);
                }
            default:
                throw new UnsupportedOperationException(
                        "create for type:" + type + " is not yet implemented");
        }
    }
    // javadoc in implemented interface
    public IMediaQualifier create(String name,
                                  IMediaObject mediaObject,
                                  IMediaQualifier.Gender gender) {

        return new IMediaObjectQualifier(name, mediaObject, gender);
    }

}
