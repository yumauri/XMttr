import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrEmitter;
import name.yumaa.xmttr.XMttrModule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple emitter, print text to files
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 17.08.2015
 */
@SuppressWarnings("unused")
public class FILES extends XMttrEmitter implements XMttrModule {

    private String template = null;
    private String folderName = null;
    private String fileName = null;
    private String encoding = null;
    private final AtomicInteger i = new AtomicInteger();

    /**
     * Emit text
     * @param text    text
     * @throws Exception
     */
    public void emit(final String text) throws Exception {
        PrintWriter file = null;
        try {
            // create file and write text
            String fName = String.format(fileName, i.getAndIncrement(), Calendar.getInstance()); // new file name
            file = new PrintWriter(new OutputStreamWriter(new FileOutputStream(folderName + File.separator + fName), encoding), true);
            file.print(text != null ? text : "");
        } finally {
            if (file != null) {
                file.flush();
                file.close();
            }
        }
    }

    /**
     * Emit text
     * @param scope    scope
     * @throws Exception
     */
    public void emit(final Map<String, Object> scope) throws Exception {
        emit(template);
    }

    /**
     * Emit text
     * @param text     text
     * @param scope    scope
     * @throws Exception
     */
    public void emit(final String text, final Map<String, Object> scope) throws Exception {
        emit(text);
    }

    /**
     * Unload module (not used)
     */
    @Override
    public void unload() throws Exception {}

    /**
     * Initialize module
     * @param properties    properties
     */
    @Override
    public void init(Properties properties) throws Exception {
        Scribe.log(this, 3, "Initialize FILES emitter module");

        // get template
        if (properties.containsKey("xmttr.template")) {
            String templateValue = properties.getProperty("xmttr.template");
            if (templateValue != null && !templateValue.isEmpty()) {
                template = new String(Files.readAllBytes(Paths.get(templateValue)));
            }
        }

        // get save folder
        if (properties.containsKey("xmttr.emitter.folder")) {
            folderName = properties.getProperty("xmttr.emitter.folder");
            if (folderName == null || folderName.isEmpty()) {
                folderName = ".";
            }
        }
        Scribe.log(this, 3, "Using folder \"" + folderName + "\"");

        // create save folder
        File folder = new File(folderName);
        if (!folder.exists()) {
            Scribe.log(this, 3, "Creating folder \"" + folderName + "\"");
            if (!folder.mkdirs()) {
                throw new Exception("Error in creating save folder...");
            }
        } else
        if (!folder.isDirectory()) {
            throw new Exception("\"" + folderName + "\" is not a folder...");
        }

        // get filename
        if (properties.containsKey("xmttr.emitter.file")) {
            fileName = properties.getProperty("xmttr.emitter.file");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new Exception("Define file for output (xmttr.emitter.file)");
        }
        Scribe.log(this, 3, "Using file name \"" + fileName + "\"");

        // get encoding
        encoding = properties.getProperty("xmttr.emitter.encoding", "UTF-8");
        Scribe.log(this, 3, "Using encoding " + encoding);

        // get initial number
        String num = properties.getProperty("xmttr.emitter.start");
        i.set(num != null && !num.isEmpty() ? Integer.parseInt(num) : 0);
        Scribe.log(this, 3, "Start number = " + i.get());
    }

    /**
     * Emitter module constructor
     */
    public FILES() {
        Scribe.log(this, 3, "Create FILES emitter module");
    }
}
