import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrEmitter;
import name.yumaa.xmttr.XMttrModule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Simple emitter, just print text to stdout
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 21.10.2014
 */
@SuppressWarnings("unused")
public class STDOUT extends XMttrEmitter implements XMttrModule {

    private String template = null;

    @Override
    public void init(final Properties properties) throws Exception {
        Scribe.log(this, 3, "Initialize STDOUT emitter module");
        if (properties.containsKey("xmttr.template")) {
            String templateValue = properties.getProperty("xmttr.template");
            if (templateValue != null && !templateValue.isEmpty()) {
                template = new String(Files.readAllBytes(Paths.get(templateValue)));
            }
        }
    }

    @Override
    public void unload() throws Exception {
        System.out.flush();
    }

    @Override
    public void emit(final String text) {
        if (text != null) {
            System.out.println(text);
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
