package name.yumaa.xmttr.modules.jms;

import name.yumaa.xmttr.Scribe;

import javax.jms.*;
import javax.naming.InitialContext;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by Victor Didenko
 * yumaa.verdin@gmail.com
 * 28.10.2014
 */
public class Context implements ExceptionListener {

    private final String contextFactory;
    private final String providerUrl;
    private final String securityPrincipal;
    private final String securityCredentials;
    private final String queueJNDI;
    private final String connectionFactoryJNDI;

    // InitialContext
    private final InitialContext ctx;

    /**
     * Send message to queue
     * @param messageText    message
     * @throws Exception
     */
    public void sendMessage(final String messageText) throws Exception {
        InitialContext ctx = getCtx(); // get the initial context

        Scribe.log(this, 3, "Get queue");
        Queue q = (Queue) ctx.lookup(queueJNDI); // lookup the queue object
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(connectionFactoryJNDI); // lookup the queue connection factory
        QueueConnection queueConn = connFactory.createQueueConnection(); // create a queue connection
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE); // create a queue session
        queueConn.setExceptionListener(this); // set an asynchronous exception listener on the connection

        QueueSender queueSender = queueSession.createSender(q); // create a queue sender
        queueSender.setDeliveryMode(DeliveryMode.PERSISTENT);

        TextMessage message = queueSession.createTextMessage(messageText); // create message

        Scribe.log(this, 3, "Send message to queue");
        queueSender.send(message); // send the message
        Scribe.log(this, 3, "Close connection");
        queueConn.close(); // close the queue connection
    }

    /**
     * Receive messages from queue
     * @param messageListener      message listener
     * @throws Exception
     */
    public void receiveMessages(final MessageListener messageListener) throws Exception {
        InitialContext ctx = getCtx(); // get the initial context

        Scribe.log(this, 3, "Get queue");
        Queue q = (Queue) ctx.lookup(queueJNDI); // lookup the queue object
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(connectionFactoryJNDI); // lookup the queue connection factory
        QueueConnection queueConn = connFactory.createQueueConnection(); // create a queue connection
        QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); // create a queue session
        queueConn.setExceptionListener(this); // set an asynchronous exception listener on the connection

        QueueReceiver queueReceiver = queueSession.createReceiver(q); // create a queue receiver
        queueReceiver.setMessageListener(messageListener); // set an asynchronous message listener

        queueConn.start(); // start the connection

        Scribe.log(this, 3, "Wait for messages in queue");
        // wait for messages
        //for (int i = 0; i < waitTime; i++) {
            Thread.sleep(5000);
        //}

        Scribe.log(this, 3, "Close connection");
        queueConn.close(); // close the queue connection
    }

    /**
     * Count messages in queue
     * @param getAll    flag, is count all messages or just check if messages exist
     * @return messages count (or 1, if getAll=false)
     * @throws Exception
     */
    private int getMessagesCount(final boolean getAll) throws Exception {
        InitialContext ctx = getCtx(); // get the initial context

        Scribe.log(this, 3, "Get queue");
        Queue q = (Queue) ctx.lookup(queueJNDI); // lookup the queue object
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(connectionFactoryJNDI); // lookup the queue connection factory
        QueueConnection queueConn = connFactory.createQueueConnection(); // create a queue connection
        QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE); // create a queue session
        queueConn.setExceptionListener(this); // set an asynchronous exception listener on the connection

        QueueBrowser queueBrowser = queueSession.createBrowser(q); // create a queue browser

        queueConn.start(); // start the connection

        Scribe.log(this, 3, "Get queue messages count");
        Enumeration e = queueBrowser.getEnumeration(); // browse the messages

        // count number of messages
        int numMsg = 0;
        if (getAll) {
            while (e.hasMoreElements()) {
                e.nextElement();
                numMsg++;
            }
        } else { // if getAll is false -> we don't need to get all messages, just check, if messages exists
            if (e.hasMoreElements()) {
                numMsg++;
            }
        }

        Scribe.log(this, 3, "Close connection");
        queueConn.close(); // close the queue connection

        return numMsg;
    }

    /**
     * Count messages in queue
     * @return messages count
     * @throws Exception
     */
    public int getMessagesCount() throws Exception {
        return getMessagesCount(true);
    }

    /**
     * Check if messages exist in queue
     * @return is queue is empty or not
     * @throws Exception
     */
    public boolean isQueueEmpty() throws Exception {
        return (getMessagesCount(false) == 0);
    }

    /**
     * ExceptionListener callback
     * @param e    queue exception
     */
    @Override
    public void onException(JMSException e) {
        Scribe.log(this, 3, "Error occurred: " + e.getMessage());
    }

    /**
     * Constructor
     * @param factory        context factory
     * @param url            provider url
     * @param principal      login
     * @param credentials    password
     * @param queue          queue name
     * @param connection     connection name
     * @throws Exception
     */
    public Context(final String factory, final String url, final String principal, final String credentials, final String queue, final String connection) throws Exception {
        contextFactory = factory;
        providerUrl = url;
        securityPrincipal = principal;
        securityCredentials = credentials;
        queueJNDI = queue;
        connectionFactoryJNDI = connection;

        // create InitialContext
        Scribe.log(this, 3, "Set InitialContext properties");
        Properties ctxProperties = new Properties();
        ctxProperties.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        if (providerUrl != null) {
            ctxProperties.setProperty(javax.naming.Context.PROVIDER_URL, providerUrl);
        }
        if (securityPrincipal != null) {
            ctxProperties.setProperty(javax.naming.Context.SECURITY_PRINCIPAL, securityPrincipal);
        }
        if (securityCredentials != null) {
            ctxProperties.setProperty(javax.naming.Context.SECURITY_CREDENTIALS, securityCredentials);
        }

        // get the initial context
        Scribe.log(this, 3, "Create InitialContext");
        ctx = new InitialContext(ctxProperties);
    }

    public InitialContext getCtx() {
        return ctx;
    }

}
