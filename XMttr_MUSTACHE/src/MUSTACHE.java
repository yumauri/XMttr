import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrGenerator;
import name.yumaa.xmttr.XMttrModule;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * MUSTACHE generator
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 24.08.2015
 */
@SuppressWarnings("unused")
public class MUSTACHE extends XMttrGenerator implements XMttrModule {

    private Mustache.Compiler mustache;
    private Template tmpl;

    /**
     * Generate message
     * @param text     template text
     * @param scope    variables scope
     * @return message
     * @throws Exception
     */
    @Override
    public String generate(final String text, final Map<String,Object> scope) throws Exception {
        Template tmpl = mustache.compile(text);
        return tmpl.execute(scope);
    }

    /**
     * Generate message
     * @param scope    variables scope
     * @return message
     * @throws Exception
     */
    @Override
    public String generate(final Map<String,Object> scope) throws Exception {
        if (tmpl != null) {
            return tmpl.execute(scope);
        } else {
            throw new Exception("Mustache template is not defined");
        }
    }

    /**
     * Generate message
     * @param text    template
     * @return message
     * @throws Exception
     */
    @Override
    public String generate(final String text) throws Exception {
        return generate(text, null);
    }

    /**
     * Initialize module
     * @param properties    module properties
     * @throws Exception
     */
    @Override
    public void init(final Properties properties) throws Exception {
        Scribe.log(this, 3, "Initialize MUSTACHE generator module");

        // get compiler, with formats
        mustache = Mustache.compiler().withFormatter(new Mustache.Formatter() {
            protected final DateFormat fmt;

            // anonymous class "constructor"
            {
                if (properties.containsKey("date.format")) {
                    fmt = new SimpleDateFormat(properties.getProperty("date.format"));
                } else {
                    fmt = null;
                }
            }

            @Override
            public String format(final Object value) {
                if (value instanceof Date && fmt != null) {
                    return fmt.format((Date) value);
                }
                return String.valueOf(value);
            }
        });

        if (mustache == null) {
            throw new Exception("Can't create Mustache compiler...");
        }

        // get template file
        if (properties.containsKey("xmttr.template")) {
            String templateValue = properties.getProperty("xmttr.template");
            if (templateValue != null && !templateValue.isEmpty()) {
                Scribe.log(this, 3, "Compile Mustache template");
                final File tmplFile = new File(templateValue);

                // create compiler with partials loader
                mustache = mustache.withLoader(new Mustache.TemplateLoader() {
                    @Override
                    public Reader getTemplate(final String name) throws Exception {
                        return new FileReader(new File(tmplFile.getParent(), name));
                    }
                });

                // compile template
                tmpl = mustache.compile(new FileReader(tmplFile));
            }
        } else {
            Scribe.log(this, 3, "Mustache template (xmttr.template) is not defined");
        }
    }

    /**
     * Unload module
     * @throws Exception
     */
    @Override
    public void unload() throws Exception {}

    /**
     * Constructor
     */
    public MUSTACHE() {
        Scribe.log(this, 3, "Create MUSTACHE generator module");
    }
}
