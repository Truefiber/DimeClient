import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Gennadiy on 27.07.2014.
 */
public class DimeClient extends JFrame{

    private JTextField inputField;
    //private JTextArea conversationArea;
    JTextPane conversationPane;
    StyledDocument conversationDoc;


    private ObjectOutputStream messageOutput;
    private ObjectInputStream messageInput;
    private String dimeServerIP;
    private Socket connection;
    static Logger log = Logger.getLogger("DimeClient");

    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    private Date dateobj;

    private static final int OWN_MESSAGE = 0;
    private static final int FOREIGN_MESSAGE = 1;
    private static final int SERVICE_MESSAGE = 2;


    public DimeClient(String serverIP) {
        super("Dio Messenger");
        dimeServerIP = serverIP;

        conversationPane = new JTextPane();
        conversationPane.setEditable(false);
        conversationDoc = conversationPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(conversationPane);

        inputField = new JTextField();
        inputField.setEditable(false);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());
                inputField.setText("");

            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, inputField);
        splitPane.setOneTouchExpandable(true);
        Dimension min = new Dimension(400,35);
        inputField.setMinimumSize(min);

        //add(inputField, BorderLayout.SOUTH);
        //conversationArea = new JTextArea();
        //add(new JScrollPane(conversationArea));
        getContentPane().add(splitPane, BorderLayout.CENTER);
        setSize(400, 400);
        setVisible(true);
        splitPane.setDividerLocation(0.9);


    }

    public void startClient() {

        try {
            establishConnection();
            raiseStreams();
            chat();

        } catch (EOFException e) {
            showMessage("Session is over", SERVICE_MESSAGE);

        } catch (IOException e) {
            e.printStackTrace();
        }

        finally {
            cleanUp();
        }

    }

    private void establishConnection() throws IOException{
        showMessage("Wait a few seconds", SERVICE_MESSAGE);
        connection = new Socket(InetAddress.getByName(dimeServerIP), 8080);
        showMessage("Connected to " + connection.getInetAddress().getHostName(), SERVICE_MESSAGE);

    }

    private void raiseStreams() throws IOException{
        messageOutput = new ObjectOutputStream(connection.getOutputStream());
        messageInput = new ObjectInputStream(connection.getInputStream());
        messageOutput.flush();
        showMessage("Streams are ready", SERVICE_MESSAGE);
    }

    private void chat() throws IOException{
        String message = "You can chat";
        showMessage(message, SERVICE_MESSAGE);
        inputField.setEditable(true);


        while (!message.equals("Exit")){
            try {
                message = (String) messageInput.readObject();
                if (message.length() > 8 && message.substring(0, 7).equals("Server:")) {
                    showMessage(message, SERVICE_MESSAGE);

                } else {
                    showMessage(message, FOREIGN_MESSAGE);
                }

            } catch (ClassNotFoundException e) {
                log.info("Wrong class");
            }

        }
    }

    private void cleanUp() {
        showMessage("Cleaning garbage", SERVICE_MESSAGE);
        inputField.setEditable(false);
        try {
            log.info("Cleaning");
            messageOutput.close();
            messageInput.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {

            messageOutput.writeObject(message);
            messageOutput.flush();
            showMessage(message, OWN_MESSAGE);
        } catch (IOException e) {
            showMessage("Error while sending message", SERVICE_MESSAGE);
        }
    }

    private void showMessage(final String message, final int typeOfMessage) {

        dateobj = new Date();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                try {
                    conversationDoc.insertString(conversationDoc.getLength(),
                            "\n" + dateFormat.format(dateobj) + ": " + message,
                            getAttributes(typeOfMessage));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
                getContentPane().validate();

            }
        });


    }

    private Style getAttributes(int typeOfMessage) {
        Style messageFontStyle = conversationPane.addStyle("Font color", null);
        switch (typeOfMessage) {
            case OWN_MESSAGE:
                StyleConstants.setForeground(messageFontStyle, Color.blue);
                break;
            case FOREIGN_MESSAGE:
                StyleConstants.setForeground(messageFontStyle, Color.black);
                break;
            case SERVICE_MESSAGE:
                StyleConstants.setForeground(messageFontStyle, Color.red);
                break;
        }

        return messageFontStyle;
    }

}
