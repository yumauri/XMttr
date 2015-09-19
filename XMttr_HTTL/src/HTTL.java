import httl.Engine;
import httl.Template;
import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrGenerator;
import name.yumaa.xmttr.XMttrModule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * HTTL generator
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 21.10.2014
 */
@SuppressWarnings("unused")
public class HTTL extends XMttrGenerator implements XMttrModule {

    private Engine httl;
    private Template xml;

    public HTTL() {
        Scribe.log(this, 3, "Create HTTL generator module");
    }

    @Override
    public void init(final Properties properties) throws Exception {
        Scribe.log(this, 3, "Initialize HTTL generator module");

        httl = Engine.getEngine(properties);
        if (httl == null) {
            throw new Exception("Can't create HTTL engine...");
        }

        // get template file
        if (properties.containsKey("xmttr.template")) {
            String templateValue = properties.getProperty("xmttr.template");
            if (templateValue != null && !templateValue.isEmpty()) {
                Scribe.log(this, 3, "Parse HTTL template");
                //xml = httl.getTemplate(properties.getProperty("xmttr.template"));
                xml = httl.parseTemplate(new String(Files.readAllBytes(Paths.get(templateValue))));
            }
        } else {
            Scribe.log(this, 3, "HTTL template (xmttr.template) is not defined");
        }
    }

    @Override
    public void unload() throws Exception {}

    @Override
    public String generate(final Map<String,Object> scope) throws Exception {
        if (xml != null) {
            return xml.evaluate(scope).toString();
        } else {
            throw new Exception("HTTL template is not defined");
        }
    }

    @Override
    public String generate(final String text) throws Exception {
        return httl.parseTemplate(text).evaluate().toString();
    }

    @Override
    public String generate(final String text, final Map<String,Object> scope) throws Exception {
        Scribe.log(this, 3, "Parse HTTL template");
        Template xml = httl.parseTemplate(text);
        if (xml != null) {
            return xml.evaluate(scope).toString();
        } else {
            throw new Exception("Template is not defined");
        }
    }
}
