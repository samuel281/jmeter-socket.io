package net.unit8.jmeter.protocol.socket_io.sampler;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.*;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.eclipse.jetty.util.ConcurrentHashSet;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

/**
 * The sampler for SocketIO.

 * @author jiaxiluo
 */
public class SocketIOSampler extends AbstractSampler implements TestStateListener {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<String>(
            Arrays.asList(new String[]{
                    "net.unit8.jmeter.protocol.socket_io.control.gui.SocketIOSamplerGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui"}));

    private static final String DEFAULT_PROTOCOL = "ws";
    private static final int UNSPECIFIED_PORT = 0;
    private static final String UNSPECIFIED_PORT_AS_STRING = "0";
    private static final int URL_UNSPECIFIED_PORT = -1;

    private SocketIO socket = null;
    private IOAcknowledge ack = null;
    private static final ConcurrentHashSet<SocketIO> samplerConnections
            = new ConcurrentHashSet<SocketIO>();

    private boolean initialized = false;
    private String responseMessage;

    public static final String DOMAIN = "SocketIOSampler.domain";
    public static final String PORT = "SocketIOSampler.port";
    public static final String PATH = "SocketIOSampler.path";
    public static final String PROTOCOL = "SocketIOSampler.protocol";
    public static final String CONNECT_TIMEOUT = "SocketIOSampler.connectTimeout";
    public static final String SEND_EVENT = "SocketIOSampler.sendEvent";
    public static final String SEND_MESSAGE = "SocketIOSampler.sendMessage";
    public static final String ACK_MESSAGE = "SocketIOSampler.ackMessage";
    public static final String ACK_TIMEOUT = "SocketIOSampler.ackTimeout";

    public SocketIOSampler() {}

    public void initialize() throws Exception {
        final SocketIOSampler parent = this;
        final String threadName = JMeterContextService.getContext().getThread().getThreadName();
        final Pattern regex = (getAckMessage() != null) ? Pattern.compile(getAckMessage()) : null;

        if (!initialized) {
            URI uri = getUri();
            socket = new SocketIO(uri.toURL());

            socket.connect(new IOCallback() {
                public void onMessage(JsonElement json, IOAcknowledge ack) {
                    try {
                        onMessage(json.toString(), ack);
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }

                public void onMessage(String data, IOAcknowledge ack) {
                    log.info("Connect " + threadName);
                }

                public void onError(SocketIOException socketIOException) {
                    log.info("an Error occured" + threadName);
                    socketIOException.printStackTrace();
                }

                public void onDisconnect() {
                    log.info("Disconnect " + threadName);
                    synchronized (parent) {
                        initialized = false;
                        parent.notify();
                    }
                }

                public void onConnect() {
                    log.info("Connect " + threadName);
                    synchronized (parent) {
                        initialized = true;
                        parent.notify();
                    }
                }

                public void on(String event, IOAcknowledge ack, JsonElement... args) {
                    log.info("Server triggered event '" + event + "'");
                }
            });

            ack = new IOAcknowledge() {
                public void ack(JsonElement... args) {
                    String data = args.toString();
                    synchronized (parent) {
                        if (regex == null || regex.matcher(data).find()) {
                            responseMessage = data;
                            parent.notify();
                        }
                    }
                }
            };

            synchronized (parent) {
                if (initialized == false)
                    wait(getConnectTimeout());
            }
            samplerConnections.add(socket);
        }
    }

    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());

        boolean isOK = false;
        if (!initialized) {
            try {
                initialize();
            } catch (Exception e) {
                res.setResponseMessage(e.getMessage());
                res.setSuccessful(false);
                return res;
            }
        }
        String message = getPropertyAsString(SEND_MESSAGE, "");
        String event = getPropertyAsString(SEND_EVENT, "");
        res.setSamplerData(message);
        res.sampleStart();
        try {
            if (socket.isConnected()) {
                socket.emit(event, ack, message);
                log.info("Send " + event + " event.");
            } else {
                initialize();
            }
            synchronized (this) {
                wait(getAckTimeout());
            }
            if (responseMessage == null) {
                res.setResponseCode("204");
                throw new TimeoutException("No content (probably timeout).");
            }
            res.setResponseCodeOK();
            isOK = true;
        } catch (Exception e) {
            log.info(e.getMessage());
            res.setResponseMessage(e.getMessage());
        }
        res.sampleEnd();
        res.setSuccessful(isOK);

        return res;
    }

    @Override
    public void setName(String name) {
        if (name != null)
            setProperty(TestElement.NAME, name);
    }

    @Override
    public String getName() {
        return getPropertyAsString(TestElement.NAME);
    }

    @Override
    public void setComment(String comment){
        setProperty(new StringProperty(TestElement.COMMENTS, comment));
    }

    @Override
    public String getComment(){
        return getProperty(TestElement.COMMENTS).getStringValue();
    }

    public URI getUri() throws URISyntaxException {
        String path = this.getPath();
        String domain = this.getDomain();
        String protocol = this.getProtocol();

        // HTTP URLs must be absolute, allow file to be relative
        if (!path.startsWith("/")){
            path = "/" + path;
        }

        if (isProtocolDefaultPort()) {
            return new URI(protocol, null, domain, -1, path, null, null);
        }
        return new URI(protocol, null, domain, getPort(), path, null, null);
    }

    public void setPath(String path) {
        setProperty(PATH, path);
    }

    public String getPath() {
        String p = getPropertyAsString(PATH);
        return encodeSpaces(p);
    }

    public void setPort(int value) {
        setProperty(new IntegerProperty(PORT, value));
    }

    public static int getDefaultPort(String protocol,int port){
        if (port==URL_UNSPECIFIED_PORT){
            return
                    protocol.equalsIgnoreCase(HTTPConstants.PROTOCOL_HTTP)  ? HTTPConstants.DEFAULT_HTTP_PORT :
                            protocol.equalsIgnoreCase(HTTPConstants.PROTOCOL_HTTPS) ? HTTPConstants.DEFAULT_HTTPS_PORT :
                                    port;
        }
        return port;
    }

    /**
     * Get the port number from the port string, allowing for trailing blanks.
     *
     * @return port number or UNSPECIFIED_PORT (== 0)
     */
    public int getPortIfSpecified() {
        String port_s = getPropertyAsString(PORT, UNSPECIFIED_PORT_AS_STRING);
        try {
            return Integer.parseInt(port_s.trim());
        } catch (NumberFormatException e) {
            return UNSPECIFIED_PORT;
        }
    }

    /**
     * Tell whether the default port for the specified protocol is used
     *
     * @return true if the default port number for the protocol is used, false otherwise
     */
    public boolean isProtocolDefaultPort() {
        final int port = getPortIfSpecified();
        final String protocol = getProtocol();
        return port == UNSPECIFIED_PORT ||
                ("ws".equalsIgnoreCase(protocol) && port == HTTPConstants.DEFAULT_HTTP_PORT) ||
                ("wss".equalsIgnoreCase(protocol) && port == HTTPConstants.DEFAULT_HTTPS_PORT);
    }

    public int getPort() {
        final int port = getPortIfSpecified();
        if (port == UNSPECIFIED_PORT) {
            String prot = getProtocol();
            if ("wss".equalsIgnoreCase(prot)) {
                return HTTPConstants.DEFAULT_HTTPS_PORT;
            }
            if (!"ws".equalsIgnoreCase(prot)) {
                log.warn("Unexpected protocol: "+prot);
                // TODO - should this return something else?
            }
            return HTTPConstants.DEFAULT_HTTP_PORT;
        }
        return port;
    }


    public void setDomain(String value) {
        setProperty(DOMAIN, value);
    }

    public String getDomain() {
        return getPropertyAsString(DOMAIN);
    }

    public void setProtocol(String value) {
        setProperty(PROTOCOL, value.toLowerCase(java.util.Locale.ENGLISH));
    }

    public String getProtocol() {
        String protocol = getPropertyAsString(PROTOCOL);
        if (protocol == null || protocol.length() == 0 ) {
            return DEFAULT_PROTOCOL;
        }
        return protocol;
    }
    
    public void setSendEvent(String value) {
        setProperty(SEND_EVENT, value);
    }
  
    public String getSendEvent() {
        return getPropertyAsString(SEND_EVENT);
    }

    public void setSendMessage(String value) {
        setProperty(SEND_MESSAGE, value);
    }

    public String getSendMessage() {
        return getPropertyAsString(SEND_MESSAGE);
    }

    public void setAckMessage(String value) {
        setProperty(ACK_MESSAGE, value);
    }

    public String getAckMessage() {
        return getPropertyAsString(ACK_MESSAGE);
    }

    public void setAckTimeout(long value) {
        setProperty(new LongProperty(ACK_TIMEOUT, value));
    }

    public long getAckTimeout() {
        return getPropertyAsLong(ACK_TIMEOUT, 20000L);
    }
    
    public void setConnectTimeout(long value) {
        setProperty(new LongProperty(CONNECT_TIMEOUT, value));
    }
  
    public long getConnectTimeout() {
        return getPropertyAsLong(CONNECT_TIMEOUT, 20000L);
    }

    protected String encodeSpaces(String path) {
        return JOrphanUtils.replaceAllChars(path, ' ', "%20");
    }

    public void testStarted() {
        testStarted("");
    }

    public void testStarted(String host) {
    }

    public void testEnded() {
        testEnded("");
    }

    public void testEnded(String host) {
        try {
            for(SocketIO socket : samplerConnections) {
                socket.disconnect();
            }
        } catch (Exception e) {
            log.error("sampler error when close.", e);
        }
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

}
