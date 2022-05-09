package torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicationHandler implements Runnable {
    private PrintWriter out;
    private BufferedReader in;
    private Socket partnerSocket = null;

    enum ConnectionState {
        CHOKED, UNCHOKED
    }

    public CommunicationHandler(Socket partnerSocket) {
        this.partnerSocket = partnerSocket;
    }

    private String getResponse(String message) {
        return "YOUR MESSAGE: " + message;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(partnerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(partnerSocket.getInputStream()));
            while (true) {
                String command = in.readLine();
                if (null == command) {
                    break;
                }
                System.out.printf("Sent from the client: %s\n", command);
                String message = getResponse(command);
                out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    partnerSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
