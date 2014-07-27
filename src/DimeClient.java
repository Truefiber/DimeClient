import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Gennadiy on 27.07.2014.
 */
public class DimeClient extends JFrame{

    private JTextField inputField;
    private JTextArea conversationArea;
    private ObjectOutputStream messageOutput;
    private ObjectInputStream messageInput;
    private String message = "";
    private String dimeServerIP;
    private Socket connection;

    public DimeClient(String serverIP) {
        super("Dio Messenger");
        dimeServerIP = serverIP;
        inputField = new JTextField();
        inputField.setEditable(false);
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(e.getActionCommand());
                inputField.setText("");

            }
        });
        add(inputField, BorderLayout.SOUTH);
        conversationArea = new JTextArea();
        add(new JScrollPane(conversationArea));
        setSize(400, 400);
        setVisible(true);
    }

    public void startClient() {

        try {
            establishConnection();
            raiseStreams();
            chat();
        } catch (EOFException e) {
            showMessage("Session is over");

        } catch (IOException e) {
            e.printStackTrace();
        }

        finally {
            cleanUp();
        }

    }

    private void establishConnection() throws IOException{
        showMessage("Wait a few seconds");
        connection = new Socket(InetAddress.getByName(dimeServerIP), 6789);
        showMessage("Connected to " + connection.getInetAddress().getHostName());

    }

    private void raiseStreams() throws IOException{
        messageOutput = new ObjectOutputStream(connection.getOutputStream());
        messageInput = new ObjectInputStream(connection.getInputStream());
        messageOutput.flush();
        showMessage("Streams are ready");
    }

    private void chat() throws IOException{
        String message = "You can chat";
        sendMessage(message);
        inputField.setEditable(true);

        while (!message.equals("Exit")){
            try {
                message = (String) messageInput.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException e) {
                showMessage("Wrong class");
            }

        }
    }

    private void cleanUp() {
        showMessage("Cleaning garbage");
        inputField.setEditable(false);
        try {
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
            showMessage("\n" + message);
        } catch (IOException e) {
            showMessage("Error while sending message");
        }
    }

    private void showMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                conversationArea.append(message);

            }
        });


    }

}
