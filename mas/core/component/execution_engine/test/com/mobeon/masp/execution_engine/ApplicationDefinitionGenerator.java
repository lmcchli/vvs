package com.mobeon.masp.execution_engine;

import com.mobeon.masp.util.url.ContentGenerator;

import java.io.*;
import java.net.URL;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-11
 * Time: 13:36:09
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationDefinitionGenerator extends ContentGenerator {
    StringBuilder sb = new StringBuilder();
    private static final String[] suffixes = {"ccxml", "vxml"};
    private static final Map<String, String> mimeTypes = new HashMap<String, String>(2);

    static {
        mimeTypes.put("ccxml", "application/xml+ccxml");
        mimeTypes.put("vxml", "application/xml+vxml");
    }

    public ApplicationDefinitionGenerator(URL url, File targetFile) {
        super(url);
        File directory = targetFile.getParentFile();

        AutoTestFilter filter = new AutoTestFilter(targetFile.getName());
        File[] files = directory.listFiles(filter);
        sb.append("<application>");

        URI base = getUri().resolve(".");
        String rootDoc = filter.prefix + ".ccxml";

        for (String suffix : suffixes) {
            boolean documentsTagWritten = false;
            String extension = "." + suffix;
            for (File file : files) {
                if (file.getName().endsWith(extension)) {
                    if (!documentsTagWritten) {
                        documentsTagWritten = true;
                        sb.append("<documents base=\"");
                        //test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/catchtag/
                        sb.append(base.toString());
                        sb.append("\" type=\"");
                        sb.append(mimeTypes.get(suffix));
                        sb.append("\">");
                    }
                    sb.append("<document src=\"");
                    //catch_11.ccxml
                    sb.append(file.getName());
                    sb.append("\"");
                    if (rootDoc.equals(file.getName()))
                        sb.append(" root=\"true\"");
                    sb.append(" />");
                }
            }
            if (documentsTagWritten)
                sb.append("</documents>");
        }
        sb.append("</application>");
    }

    private URI getUri() {
        try {
            return getUrl().toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL given for .xml file in test: " + getUrl(), e);
        }
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(sb.toString().getBytes());
    }
}
