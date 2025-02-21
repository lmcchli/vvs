package com.mobeon.masp.execution_engine;

import com.mobeon.masp.util.url.TestStreamHandler;
import com.mobeon.masp.util.url.GeneratedContentURLConnection;
import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.ApplicationDefinitionGenerator;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.File;

/**
 * Automatically generates a suitable testcase_<n>.xml given a url pointing to the
 * files contained in the test cases.
 * The convention is as follows. For a testcase name of catch_11.xml. All files named
 * catch_11* is considered as part of the set of files for that test. If a catch_11.xml
 * exists. That file is used as is.
 * <pre>
 * &lt;application&gt;
 *   &lt;documents base="test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/catchtag/" type="application/xml+ccxml"&gt;
 *       &lt;document src="catch_11.ccxml" root="true"/&gt;
 *   &lt;/documents&gt;
 *   &lt;documents base="test:/test/com/mobeon/masp/execution_engine/runapp/applications/vxml/catchtag/" type="application/xml+vxml"&gt;
 *       &lt;document src="catch_11.vxml"/&gt;
 *   &lt;/documents&gt;
 * &lt;/application&gt;
 * </pre>
 *
 * @author Mikael Andersson
 */
public class AutoTestStreamHandler extends TestStreamHandler {


    protected URLConnection openConnection(URL u) throws IOException {
        String file = u.getPath();
        File f = MASTestSwitches.currentMasDir();
        String path = f.getAbsoluteFile().getPath() + File.separator;
        if ("\\".equals(File.separator)) {
            path = path.replaceAll("\\\\", "/");
        }
        File targetFile = new File(path + file);
        if (!targetFile.exists() && targetFile.getName().endsWith(".xml")) {
            return new GeneratedContentURLConnection(new ApplicationDefinitionGenerator(u,targetFile));
        } else {
            URL newURL = new URL("file:///" + path + file);
            return newURL.openConnection();
        }
    }
}
