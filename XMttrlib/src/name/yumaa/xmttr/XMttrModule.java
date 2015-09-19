package name.yumaa.xmttr;

import java.util.Map;
import java.util.Properties;

/**
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 21.10.2014
 */
public interface XMttrModule {

    /**
     * Initialize module
     * @param properties    properties
     */
    public void init(final Properties properties) throws Exception;

    /**
     * Unload module
     */
    public void unload() throws Exception;
}
