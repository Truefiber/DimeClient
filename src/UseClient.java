import javax.swing.*;

/**
 * Created by Gennadiy on 27.07.2014.
 */
public class UseClient {

    public static void main(String[] args) {

        DimeClient dimeClient = new DimeClient("127.0.0.1");
        dimeClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dimeClient.startClient();

    }
}
