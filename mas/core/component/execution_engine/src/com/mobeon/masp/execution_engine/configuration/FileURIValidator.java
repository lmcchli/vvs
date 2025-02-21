package com.mobeon.masp.execution_engine.configuration;

import com.mobeon.masp.util.Tools;

import java.io.File;
import java.net.URI;

/**
 * @author Mikael Andersson
 */
public class FileURIValidator extends URIValidator {

    public boolean isValid(Object value) {
        if (value == null)
            return false;
        String str = value.toString();
        URI uri = Tools.toUri(str);
        if (ToFileURIConverter.isFileURI(uri, requireDir())) {
            return true;
        } else {
            uri = new File("").toURI().resolve(str);
            return ToFileURIConverter.isFileURI(uri, requireDir());
        }
    }

    public boolean requireDir() {
        return false;
    }

}
