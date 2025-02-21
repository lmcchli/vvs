/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

public enum Validators {
    SERVICE_VALIDATOR {
        Validator validator() {
            return nameValidator;
        }
    },
    URI_VALIDATOR {
        Validator validator() {
            return uriValidator;
        }
    },
    EXTENTSION_VALIDATOR {
        Validator validator() {
            return extensionValidator;
        }
    },
    MIME_VALIDATOR {
        Validator validator() {
            return mimeValidator;
        }
    },
    FILEURI_VALIDATOR {
        Validator validator() {
            return pathValidator;
        }
    },
    ANY_VALIDATOR {
        Validator validator() {
            return anyValidator;
        }
    };
    private static Validator mimeValidator = new MimeValidator();
    private static Validator extensionValidator = new ExtensionValidator();
    private static Validator nameValidator = new NameValidator();
    private static Validator uriValidator = new URIValidator();
    private static Validator pathValidator = new FileURIValidator();
    private static Validator anyValidator = new AnyValidator();

    abstract Validator validator();
}
