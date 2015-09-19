import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrEmitter;
import name.yumaa.xmttr.XMttrModule;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Simple emitter, print text to file
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 23.10.2014
 */
@SuppressWarnings("unused")
public class FILE extends XMttrEmitter implements XMttrModule {

    private String template = null;

    /**
     * File to write
     */
    private static volatile PrintWriter file = null;

    public FILE() {
        Scribe.log(this, 3, "Create FILE emitter module");
    }

    @Override
    public void init(final Properties properties) throws Exception {
        Scribe.log(this, 3, "Initialize FILE emitter module");

        if (properties.containsKey("xmttr.template")) {
            String templateValue = properties.getProperty("xmttr.template");
            if (templateValue != null && !templateValue.isEmpty()) {
                template = new String(Files.readAllBytes(Paths.get(templateValue)));
            }
        }

        if (properties.containsKey("xmttr.emitter.file")) {
            String fileName = properties.getProperty("xmttr.emitter.file");
            if (fileName != null && !fileName.isEmpty()) {
                String append = properties.getProperty("xmttr.emitter.append", "false").toLowerCase();
                file = new PrintWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(fileName, "true".equals(append) || "yes".equals(append) || "1".equals(append)),
                                properties.getProperty("xmttr.emitter.encoding", "UTF-8")
                        ),
                        true
                );
            }
        }

        if (file == null) {
            throw new Exception("Define file for output (xmttr.emitter.file)");
        }
    }

    @Override
    public void unload() throws Exception {
        if (file != null) {
            file.flush();
            file.close();
        }
    }

    @Override
    public void emit(final String text) {
        if (text != null && file != null) {
            file.println(text);
        }
    }

    @Override
    public void emit(final Map<String, Object> scope) {
        emit(template);
    }

    @Override
    public void emit(final String text, final Map<String, Object> scope) {
        emit(text);
    }
}
