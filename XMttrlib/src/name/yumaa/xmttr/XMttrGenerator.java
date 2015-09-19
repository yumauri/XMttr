package name.yumaa.xmttr;

import java.util.Map;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 21.10.2014
 */
@SuppressWarnings("unused")
public abstract class XMttrGenerator implements XMttrModule {

    /**
     * Generate message from scope
     * @param text    template text
     * @return generated text
     */
    public String generate(final String text) throws Exception {
        return null;
    }

    /**
     * Generate message from scope
     * @param scope    template scope
     * @return generated text
     */
    public String generate(final Map<String,Object> scope) throws Exception {
        return null;
    }

    /**
     * Generate message from scope
     * @param text     template text
     * @param scope    template scope
     * @return generated text
     */
    public String generate(final String text, final Map<String,Object> scope) throws Exception {
        return null;
    }

    /**
     * Unload module
     */
    @Override
    public void unload() throws Exception {}
}
