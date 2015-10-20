package net.unit8.jmeter.protocol.socket_io.control.gui;

import net.unit8.jmeter.protocol.socket_io.sampler.SocketIOSampler;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * GUI for SocketIOSampler
 *
 * @author jiaxiluo
 */
public class SocketIOSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    private JTextField domain;
    private JTextField port;
    private JTextField protocol;
    private JTextField path;
    private JTextField room;
    private JTextField connectTimeout;
    private JTextField ackTimeout;
    private JTextField sendEvent;
    private JTextArea  sendMessage;
    private JTextArea  ackMessage;

    private boolean displayName = true;
    private static final ResourceBundle resources;

    static {
        Locale loc = JMeterUtils.getLocale();
        resources = ResourceBundle.getBundle(SocketIOSampler.class.getName() + "Resources", loc);
        log.info("Resource " + SocketIOSampler.class.getName() +
                " is loaded for locale " + loc);
    }

    public SocketIOSamplerGui() {
        this(true);
    }

    public SocketIOSamplerGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called");
    }

    @Override
    public String getStaticLabel() {
        return getResString("socket_io_testing_title");
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        domain.setText(element.getPropertyAsString(SocketIOSampler.DOMAIN));
        port.setText(element.getPropertyAsString(SocketIOSampler.PORT));
        protocol.setText(element.getPropertyAsString(SocketIOSampler.PROTOCOL));
        path.setText(element.getPropertyAsString(SocketIOSampler.PATH));
        room.setText(element.getPropertyAsString(SocketIOSampler.ROOM));
        connectTimeout.setText(element.getPropertyAsString(SocketIOSampler.CONNECT_TIMEOUT));
        ackTimeout.setText(element.getPropertyAsString(SocketIOSampler.ACK_TIMEOUT));
        sendEvent.setText(element.getPropertyAsString(SocketIOSampler.SEND_EVENT));
        sendMessage.setText(element.getPropertyAsString(SocketIOSampler.SEND_MESSAGE));
        ackMessage.setText(element.getPropertyAsString(SocketIOSampler.ACK_MESSAGE));
    }

    public TestElement createTestElement() {
        SocketIOSampler element = new SocketIOSampler();

        element.setName(getName());
        element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
        element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());

        modifyTestElement(element);
        return element;
    }

    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        element.setProperty(SocketIOSampler.DOMAIN, domain.getText());
        element.setProperty(SocketIOSampler.PATH, path.getText());
        element.setProperty(SocketIOSampler.ROOM, room.getText());
        element.setProperty(SocketIOSampler.PORT, port.getText());
        element.setProperty(SocketIOSampler.PROTOCOL, protocol.getText());
        element.setProperty(SocketIOSampler.CONNECT_TIMEOUT, connectTimeout.getText());
        element.setProperty(SocketIOSampler.ACK_TIMEOUT, ackTimeout.getText());
        element.setProperty(SocketIOSampler.SEND_EVENT, sendEvent.getText());
        element.setProperty(SocketIOSampler.SEND_MESSAGE, sendMessage.getText());
        element.setProperty(SocketIOSampler.ACK_MESSAGE, ackMessage.getText());
    }

    private JPanel getDomainPanel() {
        domain = new JTextField(20);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain"));
        label.setLabelFor(domain);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(domain, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getPortPanel() {
        port = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port"));
        label.setLabelFor(port);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(port, BorderLayout.CENTER);

        return panel;
    }

    protected Component getProtocolAndPathPanel() {
        // PATH
        path = new JTextField(15);
        JLabel pathLabel = new JLabel(JMeterUtils.getResString("path"));
        pathLabel.setLabelFor(path);

        // PROTOCOL
        protocol = new JTextField(4);
        JLabel protocolLabel = new JLabel(JMeterUtils.getResString("protocol"));
        protocolLabel.setLabelFor(protocol);


        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(pathLabel);
        panel.add(path);
        panel.add(Box.createHorizontalStrut(5));

        panel.add(protocolLabel);
        panel.add(protocol);
        panel.add(Box.createHorizontalStrut(5));

        panel.setMinimumSize(panel.getPreferredSize());

        return panel;
    }
    
    private JPanel getConnectTimeoutPanel() {
        connectTimeout = new JTextField(10);

        JLabel label = new JLabel(getResString("socket_io_connect_timeout"));
        label.setLabelFor(connectTimeout);
  
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(connectTimeout, BorderLayout.CENTER);
  
        return panel;
    }
    
    private JPanel getAckTimeoutPanel() {
        ackTimeout = new JTextField(10);
  
        JLabel label = new JLabel(getResString("socket_io_ack_timeout"));
        label.setLabelFor(ackTimeout);
  
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(ackTimeout, BorderLayout.CENTER);
  
        return panel;
    }
    
    private JPanel getSendEventPanel() {
        sendEvent = new JTextField(30);
        JLabel label = new JLabel(getResString("socket_io_send_event"));
        label.setLabelFor(sendEvent);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(sendEvent, BorderLayout.CENTER);
  
        return panel;
  }

    private JPanel getRoomPanel() {
        room = new JTextField(30);
        JLabel label = new JLabel(getResString("socket_io_room"));
        label.setLabelFor(room);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(room, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getSendMessagePanel() {
        JLabel sendMessageLabel = new JLabel(getResString("socket_io_send_message"));
        sendMessage = new JTextArea(3, 0);
        sendMessage.setLineWrap(true);
        sendMessageLabel.setLabelFor(sendMessage);

        JPanel sendMessagePanel = new JPanel(new BorderLayout(5, 0));
        sendMessagePanel.add(sendMessageLabel, BorderLayout.WEST);
        sendMessagePanel.add(sendMessage, BorderLayout.CENTER);
        return sendMessagePanel;
    }

    private JPanel getAckMessagePanel() {
        JLabel ackMessageLabel = new JLabel(getResString("socket_io_ack_message"));
        ackMessage = new JTextArea(3, 0);
        ackMessage.setLineWrap(true);
        ackMessageLabel.setLabelFor(ackMessage);

        JPanel ackMessagePanel = new JPanel(new BorderLayout(5, 0));
        ackMessagePanel.add(ackMessageLabel, BorderLayout.WEST);
        ackMessagePanel.add(ackMessage, BorderLayout.CENTER);
        return ackMessagePanel;
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        JPanel webRequestPanel = new HorizontalPanel();
        JPanel serverPanel = new JPanel();
        serverPanel.setLayout(new BoxLayout(serverPanel, BoxLayout.X_AXIS));
        serverPanel.add(getDomainPanel());
        serverPanel.add(getPortPanel());
        webRequestPanel.add(serverPanel, BorderLayout.NORTH);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(getProtocolAndPathPanel());
        northPanel.add(getConnectTimeoutPanel());
        webRequestPanel.add(northPanel, BorderLayout.SOUTH);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.add(getRoomPanel());
        southPanel.add(getSendEventPanel());
        southPanel.add(getAckTimeoutPanel());

        mainPanel.add(webRequestPanel);
        mainPanel.add(southPanel);
        mainPanel.add(getSendMessagePanel());
        mainPanel.add(getAckMessagePanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Gets the resource string for this key.
     *
     * If the resource is not found, a warning is logged
     *
     * @param key
     *            the key in the resource file
     * @return the resource string if the key is found; otherwise, return
     *         "[res_key="+key+"]"
     */
    public static String getResString(String key) {
        return getResStringDefault(key, RES_KEY_PFX + key + "]");
    }

    public static final String RES_KEY_PFX = "[res_key=";

    /*
     * Helper method to do the actual work of fetching resources; allows
     * getResString(S,S) to be deprecated without affecting getResString(S);
     */
    private static String getResStringDefault(String key, String defaultValue) {
        if (key == null) {
            return null;
        }
        // Resource keys cannot contain spaces
        key = key.replace(' ', '_');
        key = key.toLowerCase(java.util.Locale.ENGLISH);
        String resString = null;
        try {
            resString = resources.getString(key);
        } catch (MissingResourceException mre) {
            log.warn("ERROR! Resource string not found: [" +
                    key + "]", mre);
            resString = defaultValue;
        }
        return resString;
    }
}
