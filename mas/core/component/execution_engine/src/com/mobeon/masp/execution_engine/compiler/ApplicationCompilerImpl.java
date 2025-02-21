/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.masp.execution_engine.ApplicationImpl;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.components.ApplicationCompilerComponent;
import com.mobeon.masp.execution_engine.configuration.*;
import com.mobeon.masp.execution_engine.runtime.RuntimeConstants;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.xml.XPP3CompilerReader;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.Ignore;
import com.mobeon.masp.util.Tools;
import static com.mobeon.masp.util.Tools.relativize;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.XPP3Reader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compile one or more VoiceXML/CCXML documents info an {@link ApplicationImpl}.
 * Each {@link ApplicationImpl} consists of one or more {@link Module}s.
 * It is possible to serialize/deserialize a compiled application
 *
 * @author David Looberger
 */
@ConfigurationParameters({ParameterId.ApplicationCompiler_MapExtensionToMimeType,
        ParameterId.ApplicationCompiler_GenerateOps,
        ParameterId.ApplicationCompiler_OpsPath})
public class ApplicationCompilerImpl extends Configurable implements ApplicationCompilerComponent {

    static ILogger log = ILoggerFactory.getILogger(ApplicationCompilerImpl.class);
    private static final Map<String, CompilerConfiguration> compilerConfigurations = new HashMap<String, CompilerConfiguration>();
    private Map<String, String> extensionToMimeType = new HashMap<String, String>();
    private ParameterBlock parameterBlock = new ParameterBlock();
    private boolean generateOps;
    private URI opsPathURI;
    private IConfigurationManager configurationManager;

    public ParameterBlock getParameterBlock() {
        return parameterBlock;
    }

    static {
        compilerConfigurations.put(Constants.MimeType.CCXML_MIMETYPE, Compiler.CCXML_CONFIG);
        compilerConfigurations.put(Constants.MimeType.VOICEXML_MIMETYPE, Compiler.VXML_CONFIG);
    }

    public ApplicationCompilerImpl() {
        extensionToMimeType = defaultMap(ParameterId.ApplicationCompiler_MapExtensionToMimeType);
        generateOps = defaultBoolean(ParameterId.ApplicationCompiler_GenerateOps);
        opsPathURI = convert(ParameterId.ApplicationCompiler_OpsPath, URI.class, defaultString(ParameterId.ApplicationCompiler_OpsPath));
    }

    /**
     * Compile an VoiceXML/CCXML application, by specifying the root URI of the application.
     *
     * @param applicationURI
     * @return the compiled application or null if failure
     * @logs.error "Invalid URI <applicationURI> couldn't be converted to an URL <message>" - The URI <applicationURI> is invalid and could not be compiled. Check if the URI exists and its format. <message> should give more information about the problem.
     * @logs.error "Error while reading application description file <applicationURI>, it's missing or invalid <message>" - The <applicationURI> is missing or has invalid format. <message> should give more information about the problem.
     * @logs.error "I/O error while reading application description file <applicationURI> <message>" - the <applicationURI> points to files that can not be read, either because they have wrong format, are missing, or due to bad file permissions. <message> should give more information about the problem.
     */
    public ApplicationImpl compileApplication(URI applicationURI) {
        if (log.isDebugEnabled()) log.debug("Compiling application:" + applicationURI.getPath());

        XPP3Reader r = new XPP3CompilerReader();
        ApplicationImpl app = new ApplicationImpl(applicationURI);
        try {
            URL docURL = applicationURI.toURL();
            Document doc = r.read(docURL);
            if (! compileApplication(app, doc, applicationURI)) {
                return null;
            }
        } catch (MalformedURLException e) {
            log.error("Invalid URI " + applicationURI + " couldn't be converted to an URL", e);
            return null;
        } catch (DocumentException e) {
            log.error("Error while reading application description file " + applicationURI + ", it's missing or invalid", e);
            return null;
        } catch (IOException e) {
            log.error("I/O error while reading application description file " + applicationURI, e);
            return null;
        } catch (XmlPullParserException e) {
            log.error("XML parser error while reading application description file " + applicationURI +
                    " is the XML file well-formed ?", e);
            return null;
        }
        return app;
    }

    /**
     * @param app
     * @param applicationDescriptionDocument
     * @param applicationURI
     * @return
     * @logs.error "Expected <documents> but got <message> in <applicationURI>, unable to continue loading!" - The format of the application description file is not correct. Especially, check the <documents> tags
     * @logs.error "Encountered <documents> without type attribute, in <applicationURI>" - The format of the application description file is not correct. Especially, check that the type attribute is defined for all documents.
     */
    private boolean compileApplication(ApplicationImpl app, Document applicationDescriptionDocument, URI applicationURI) {

        List documentsList = applicationDescriptionDocument.getRootElement().elements();


        for (Object documentsObj : documentsList) {
            Element documentsElement = (Element) documentsObj;
            if (! "documents".equals(documentsElement.getName())) {
                log.error("Expected <documents> but got" + documentsElement.getName() +
                        " in " + applicationURI + " unable to continue loading!");
                return false;
            }
            String type = documentsElement.attributeValue("type");
            if (type == null) {
                log.error("Encountered <documents> without type attribute, in " + applicationURI);
                return false;
            }
            String base = documentsElement.attributeValue("base");
            if (base != null) {
                base = relativize(base);
            }
            ModuleCollection collection = compileModuleCollection(applicationURI, base, type, documentsElement);
            if (collection == null) {
                return false;
            }
            app.add(collection);
        }
        return true;
    }


    /**
     * @param applicationURI
     * @param base
     * @param type
     * @param documentsElement
     * @return
     * @logs.error "Invalid base uri specified in application description, URI <base> is unparsable" - The URI <base> in the application description file has invalid format.
     * @logs.warning "Base URI doesn't end with a '/', this may lead to unexpected behaviour in applications. Attempting to add a '/" - The base URI was not specifid with a trailing '/'
     */
    private ModuleCollection compileModuleCollection(URI applicationURI, String base, String type, Element documentsElement) {
        URI baseURI = applicationURI;
        ModuleCollection collection = null;
        if (base != null) {
            try {
                baseURI = new URI(base);
                collection = new ModuleCollection(applicationURI, baseURI, type);
                if (!baseURI.getPath().endsWith("/")) {
                    log.warn("Base URI doesn't end with a '/', this may lead to " +
                            "unexpected behaviour in applications. Attempting to add a '/'");
                    baseURI = new URI(baseURI.toString() + '/');
                }
            } catch (URISyntaxException e) {
                log.error("Invalid base uri specified in application description, URI " + base + " is unparsable");
                return null;
            }
        }
        if (collection == null)
            collection = new ModuleCollection(applicationURI, baseURI, type);

        List documentList = documentsElement.elements();
        for (Object elem : documentList) {
            if (! compileModule(elem, collection, baseURI)) {
                return null;
            }
        }
        return collection;
    }

    private boolean compileModule(Object elem, ModuleCollection collection, URI baseURI) {
        Element element = (Element) elem;
        String value = element.attributeValue("src");
        boolean isRoot = false;
        if (Boolean.parseBoolean(element.attributeValue("root"))) {
            collection.setIsRoot();
            isRoot = true;
        }
        URI documentURI = baseURI.resolve(value);
        Module module = compileDocument(documentURI, collection);
        if (module == null) {
            return false;
        }

        module.setParent(collection);
        boolean generateOps = readBoolean(configurationManager, ParameterId.ApplicationCompiler_GenerateOps, RuntimeConstants.CONFIG.GENERATE_OPS, log);

        if (TestEventGenerator.isActive()) {
            TestEventGenerator.generateEvent(TestEvent.APPLICATION_COMPILER_GENERATEOPS, generateOps);
        }
        if (generateOps) {
            dumpCompiledDocument(documentURI, module);
        }
        if (isRoot || collection.getEntry() == null)
            collection.setEntry(module);
        collection.put(documentURI, module);
        return true;
    }

    /**
     * @param documentURI
     * @param module
     * @logs.warn "Unable to generate .ops files to <opsPathURI> because it is a file ! It should be a directory." - Failed to generate .ops files to the configured directory <opsPathURI>, since <opsPathURI> is actually a file
     * @logs.warn "The directory <opsPathURI> didn't exist, and we failed creating it, .ops dumping will not work !" - Failed to create the directory <opsPathURI>, probably due to wrong file permissions.
     */
    public void dumpCompiledDocument(URI documentURI, Module module) {
        String crashInfo = null;
        String compiledName;
        if (documentURI.getHost() == null) {
            compiledName = "localhost";
        } else {
            compiledName = documentURI.getHost();
        }
        String path = readString(configurationManager, ParameterId.ApplicationCompiler_OpsPath, RuntimeConstants.CONFIG.OPSPATH, log);
        if (TestEventGenerator.isActive()) {
            // A test case relies on this log:
            TestEventGenerator.generateEvent(TestEvent.APPLICATION_COMPILER_PATH, path);
        }
        setOpsPathURI(path);
        File opsDir = new File(opsPathURI);
        if (opsDir.isFile()) {
            log.warn("Unable to generate .ops files to " + opsPathURI + " because it is a file ! It should be a directory.");
            return;
        }
        if (!opsDir.isDirectory()) {
            if (!opsDir.mkdirs()) {
                log.warn("The directory " + opsPathURI + " didn't exist, and we failed creating it, .ops dumping will not work !");
                return;
            }
        }

        compiledName = opsPathURI.getPath() + "/" + compiledName + "-" + documentURI.getPath().replaceAll("[\\/:]", "_") + ".ops";
        File compiled = new File(opsPathURI.resolve(compiledName));

        if (compiled.exists()) {
            compiled.delete();
            compiled = new File(compiledName);
        }
        ExecutableBase.StringAccumulator sa = new ExecutableBase.StringAccumulator();
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(compiled));
            module.getProduct().appendMnemonic(sa, 0);
        } catch (FileNotFoundException e) {
            if (log.isDebugEnabled())
                log.debug("Unable to write compiled document to " + compiled + " the exceptions was: " + e.getMessage());
        } catch (IOException e) {
            if (log.isDebugEnabled())
                log.debug("Problem while writing compiled document to " + compiled + " the exception was: " + e.getMessage());
        } catch (Throwable t) {
            if (log.isDebugEnabled()) log.debug("Unexpected error while dumping compiled document !", t);
            crashInfo = Tools.readStackDump(t);
        }
        finally {
            if (writer != null)
                try {
                    writer.write(sa.toString().replaceAll("\\\\n", "\n"));
                    if (crashInfo != null) {
                        writer.write("----------------- Crash -----------------");
                        writer.write(crashInfo);
                    }
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    Ignore.ioException(e);
                }
        }
    }

    /**
     * Compile one document into its compiled representation, {@link Module}.
     *
     * @param documentURI
     * @return the compiled document
     * @logs.warn "Unknown document extension, assuming VoiceXML" - It was not possible to recognize the file extension of the file being compiled, and VoiceXML contents is assumed.
     * @logs.error "No suitable mime-type found for <docURL>" - The file extension of the file pointed out by <docURL> is not recognized. ".vxml" or ".ccxml" is recommended.  <message> should give further information about the problem.
     * @logs.error "Unsupported mime-type <mimeType> needed for <docURL>" - The document pointed out by docURL requires the unknown mime type <mimeType>.  <message> should give further information about the problem.
     * @logs.error "When compiling <documentURI> : Invalid URI requested, this caused an error, <message>" - The URI <documentURI> encountered during compilation is invalid. <message> should give further information about the problem
     * @logs.error "When compiling <documentURI>: Compilation failed, the XML document is unparsable, <message>" - The document pointed out by <documentURI> does not contain valid XML.  <message> should give further information about the problem.
     * @logs.error "When compiling <documentURI>: Compilation failed due to an I/O error, <message>" - It was not possiblle to retrieve or read <documentURI>, due to wrong permissions or similar. <message> should give further information about the problem
     * @logs.error "XML parser error while reading document <documentURI>, is the XML file well-formed ?" - The document pointed out by <documentURI> does not contain valid XML.  <message> should give further information about the problem.
     */
    public Module compileDocument(URI documentURI, ModuleCollection collection) {

        Module module = null;
        XPP3Reader r = new XPP3CompilerReader();
        try {
            URL docURL = documentURI.toURL();
            String mimeType;
            String file = docURL.getFile();
            int index = file.lastIndexOf('.');
            if (index != 0) {
                String ext = file.substring(index + 1);
                mimeType = extensionToMimeType.get(ext);
            } else {
                log.warn("Unknown document extension, assuming VoiceXML");
                mimeType = Constants.MimeType.VOICEXML_MIMETYPE;
            }
            Document doc = r.read(docURL);
            if (!compilerConfigurations.containsKey(mimeType)) {
                log.error("Unsupported mime-type " + mimeType + " needed for " + docURL);
                return null;
            }
            long start = System.nanoTime();
            CompilerConfiguration compilerConfig = compilerConfigurations.get(mimeType);
            module = Compiler.compileDocument(compilerConfig, doc, documentURI, collection);
            if (log.isDebugEnabled())
                log.debug("Compilation of " + documentURI + " took " + ((double) System.nanoTime() - start) / 1000 / 1000 + " ms");

        } catch (MalformedURLException e) {
            log.error("When compiling " + documentURI + ": Invalid URI requested, this caused an error", e);
        } catch (DocumentException e) {
            log.error("When compiling " + documentURI + ": Compilation failed, the XML document is unparsable", e);
        } catch (IOException e) {
            log.error("When compiling " + documentURI + ": Compilation failed due to an I/O error", e);
        } catch (XmlPullParserException e) {
            log.error("XML parser error while reading document " + documentURI +
                    " is the XML file well-formed ?", e);
        }
        return module;
    }

    public Module compileDocument(Document doc, URI documentURI, ModuleCollection collection) {
        Module module = null;
        try {
            URL docURL = documentURI.toURL();
            String mimeType;
            String file = docURL.getFile();
            int index = file.lastIndexOf('.');
            if (index != 0) {
                String ext = file.substring(index + 1);
                mimeType = extensionToMimeType.get(ext);
            } else {
                log.warn("Unknown document extension, assuming VoiceXML");
                mimeType = Constants.MimeType.VOICEXML_MIMETYPE;
            }
            if (mimeType == null) {
                log.error("No suitable mime-type found for " + docURL);
                return null;
            }
            if (!compilerConfigurations.containsKey(mimeType)) {
                log.error("Unsupported mime-type " + mimeType + " needed for " + docURL);
                return null;
            }
            long start = System.nanoTime();
            CompilerConfiguration compilerConfig = compilerConfigurations.get(mimeType);
            module = Compiler.compileDocument(compilerConfig, doc, documentURI, collection);
            if (log.isDebugEnabled())
                log.debug("Compilation of " + documentURI + " took " + ((double) System.nanoTime() - start) / 1000 / 1000 + " ms");

        } catch (MalformedURLException e) {
            log.error("When compiling " + documentURI + ": Invalid URI requested, this caused an error", e);
        } catch (IOException e) {
            log.error("When compiling " + documentURI + ": Compilation failed due to an I/O error", e);
        }
        return module;
    }

    /**
     * Compiles and serializes the application identified by the root document
     *
     * @param rootDocument
     * @return InputStream representing the serialized application
     */
    public InputStream serialize(URI rootDocument) //TODO: should this be an URI?
    {
        ApplicationImpl app = compileApplication(rootDocument);
        return app.serialize();
    }

    /**
     * Convert a serialized application back to its non-serialized, compiled representation, {@link ApplicationImpl}.
     *
     * @param application, {@link File} representation the serialized application
     * @return the de-serialized {@link ApplicationImpl}
     */
    public ApplicationImpl deserialize(File application) {
        return ApplicationImpl.deserialize(application);
    }

    public Map<String, String> getExtensionToMimeType() {
        return extensionToMimeType;
    }

    @StringParameter(
            description = "Absolute or relative path or URI describing where to put .ops files",
            displayName = ".ops path",
            configName = "opspath",
            parameter = ParameterId.ApplicationCompiler_OpsPath,
            defaultValue = "ops/",
            validator = Validators.FILEURI_VALIDATOR,
            converter = Converters.TO_FILEURI
    )
    /**
     * @logs.warn "Not setting config parameter <path> as .ops path, it is not valid !" - The configured directory for .ops files is not valid
     * @logs.warn "Tried configuration with invalid file uri : <uri>" - The configured directory for .ops files, <uri> is not valid
     */
    public void setOpsPathURI(String path) {
        ParameterId thisParam = ParameterId.ApplicationCompiler_OpsPath;
        if (validate(thisParam, path)) {
            URI newURI = convert(thisParam, URI.class, path);
            if (newURI != null) {
                if (log.isInfoEnabled())
                    log.info("Config parameter " + displayName(thisParam) + " was " + path + " and got interpreted as " + newURI);
                opsPathURI = newURI;
            } else {
                log.warn("Not setting config parameter " + path + " as " + displayName(thisParam) + ", it is not valid !");
            }
        } else {
            if (log.isDebugEnabled())
                log.warn("Tried configuration with invalid file uri :" + path);
        }
    }

    @BooleanParameter(
            description = "Enabled or disabled status of .ops file generation",
            displayName = "Generate .ops files",
            configName = "generateops",
            parameter = ParameterId.ApplicationCompiler_GenerateOps,
            defaultValue = false
    )

    @MapParameter(
            description = "Mapping of extensions to mime-types",
            displayName = "Extension mapping",
            configName = "mapextensiontomimetype",
            parameter = ParameterId.ApplicationCompiler_MapExtensionToMimeType,
            keyValidator = Validators.EXTENTSION_VALIDATOR,
            valueValidator = Validators.MIME_VALIDATOR,
            defaultValue = {"ccxml", Constants.MimeType.CCXML_MIMETYPE,
                    "vxml", Constants.MimeType.VOICEXML_MIMETYPE}
    )

    /**
     * @logs.warn "Invalid configuration for extensions-to-mimetype detected" - The configuration regarding mapping from file extensions to mimetype is not valid. <message> should give more information about the problem.
     */
    public void setExtensionToMimeType(Map<String, String> extensionToMimeType) {
        try {
            this.extensionToMimeType = convert(ParameterId.ApplicationCompiler_MapExtensionToMimeType, extensionToMimeType);
        } catch (InvalidConfigurationException e) {
            if (log.isDebugEnabled())
                log.warn("Invalid configuration for extensions-to-mimetype detected:" + e.getMessage());
        }
    }

    public static ILogger getLog() {
        return log;
    }

    public static void setLog(ILogger log) {
        ApplicationCompilerImpl.log = log;
    }

    public void setConfigurationManager(IConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }
}
