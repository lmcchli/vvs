/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

public enum Converters {
    TO_STRING{
        Converter converter() {
            return toString;
        }
    },
    TO_URI{
        Converter converter() {
            return toURI;
        }

    },
    TO_FILEURI{
        Converter converter() {
            return toFileURI;
        }

    };

    private static Converter toString = new ToStringConverter();
    private static Converter toURI = new ToURIConverter();
    private static Converter toFileURI = new ToFileURIConverter();

    abstract Converter converter();

}
