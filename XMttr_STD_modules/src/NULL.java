import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrEmitter;
import name.yumaa.xmttr.XMttrModule;

import java.util.Properties;

/**
 * Simple emitter, send text to /dev/null :) Just do nothing
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 21.10.2014
 */
@SuppressWarnings("unused")
public class NULL extends XMttrEmitter implements XMttrModule {

    @Override
    public void init(final Properties properties) {
        Scribe.log(this, 3, "Initialize NULL emitter module");
    }
}
