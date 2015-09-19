package name.yumaa.xmttr;

import java.util.Map;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 21.10.2014
 */
@SuppressWarnings("unused")
public abstract class XMttrEmitter implements XMttrModule {

    /**
     * Emit text
     * @param text    text
     * @throws Exception
     */
    public void emit(final String text) throws Exception {}

    /**
     * Emit text
     * @param scope    scope
     * @throws Exception
     */
    public void emit(final Map<String, Object> scope) throws Exception {}

    /**
     * Emit text
     * @param text     text
     * @param scope    scope
     * @throws Exception
     */
    public void emit(final String text, final Map<String, Object> scope) throws Exception {}

    /**
     * Unload module
     */
    @Override
    public void unload() throws Exception {}
}
