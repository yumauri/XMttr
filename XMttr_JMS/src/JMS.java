import name.yumaa.xmttr.Scribe;
import name.yumaa.xmttr.XMttrEmitter;
import name.yumaa.xmttr.XMttrModule;
import name.yumaa.xmttr.modules.jms.Client;

import java.util.Map;
import java.util.Properties;

/**
 * JMS emitter
 * @author Victor Didenko
 * yumaa.verdin@gmail.com
 * 27.10.2014
 */
@SuppressWarnings("unused")
public class JMS extends XMttrEmitter implements XMttrModule {

    private volatile Client client;
    private volatile boolean skipsend = false;

    public JMS() {
        Scribe.log(this, 3, "Create JMS emitter module");
    }

    @Override
    public void init(final Properties properties) throws Exception {
        Scribe.log(this, 3, "Initialize JMS emitter module");

        String skipsendProp = properties.getProperty("xmttr.emitter.skipsend", "no");
        skipsend = "yes".equalsIgnoreCase(skipsendProp) || "true".equalsIgnoreCase(skipsendProp) || "1".equals(skipsendProp);
        if (!skipsend) {
            client = new Client(properties);
        }
    }

    @Override
    public void emit(final String text) {
        Scribe.log(this, 2, "Emitting message:\n" + text);
        if (!skipsend) {
            try {
                client.get().sendMessage(text);
            } catch (Exception e) {
                Scribe.log(this, 0, "Cannot emit message: " + e);
            }
        } else {
            Scribe.log(this, 3, "Message didn't send, skipsend is true");
        }
    }

    @Override
    public void emit(final Map<String,Object> scope) throws Exception {
        throw new Exception("This method is not implemented, use emit('...') instead");
    }

    @Override
    public void emit(final String text, final Map<String,Object> scope) throws Exception {
        throw new Exception("This method is not implemented, use emit('...') instead");
    }

    /**
     * Check, if queue is empty
     * Rhino uses reflection, so we can define any method here and it will be accessible from plugin
     * @return true or false
     */
    public boolean isQueueEmpty() throws Exception {
        return skipsend || client.get().isQueueEmpty();
    }

}
