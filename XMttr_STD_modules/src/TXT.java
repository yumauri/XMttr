import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrGenerator;
import name.yumaa.xmttr.XMttrModule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * Simple TXT generator, just replace variables within template
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 21.10.2014
 */
@SuppressWarnings("unused")
public class TXT extends XMttrGenerator implements XMttrModule {

    private String template = null;

    @Override
    public void init(final Properties properties) throws Exception {
        Scribe.log(this, 3, "Initialize TXT generator module");
        if (properties.containsKey("xmttr.template")) {
            String templateValue = properties.getProperty("xmttr.template");
            if (templateValue != null && !templateValue.isEmpty()) {
                template = new String(Files.readAllBytes(Paths.get(templateValue)));
            }
        } else {
            Scribe.log(this, 3, "TXT template (xmttr.template) is not defined");
        }
    }

    @Override
    public String generate(final String text) throws Exception {
        return generate(text, null);
    }

    @Override
    public String generate(final String text, final Map<String, Object> scope) throws Exception {
        if (text != null) {
            String txt = text;
            if (!txt.isEmpty() && scope != null && !scope.isEmpty()) {
                for (Map.Entry<String, Object> e : scope.entrySet()) {
                    if (e.getValue() != null) {
                        txt = txt.replace(e.getKey(), e.getValue().toString());
                    }
                }
            }
            return txt;
        } else {
            throw new Exception("TXT template is not defined");
        }
    }

    @Override
    public String generate(final Map<String, Object> scope) throws Exception {
        return generate(template, scope);
    }

}
