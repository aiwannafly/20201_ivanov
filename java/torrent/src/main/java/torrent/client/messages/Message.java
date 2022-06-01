package torrent.client.messages;

import torrent.Constants;
import torrent.client.exceptions.BadMessageException;
import torrent.client.util.ByteOperations;

import java.io.IOException;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Message {
    public static final int CHOKE = 0;
    public static final int UNCHOKE = 1;
    public static final int INTERESTED = 2;
    public static final int NOT_INTERESTED = 3;
    public static final int HAVE = 4;
    public static final int BITFIELD = 5;
    public static final int REQUEST = 6;
    public static final int PIECE = 7;
    public static final int CANCEL = 8;
    public static final int KEEP_ALIVE = 9;

    public static class MessageInfo {
        public int length;
        public int type;
        public String data;
    }

    public static MessageInfo getMessage(SocketChannel client) throws IOException, BadMessageException {
        ByteBuffer lengthBuf = ByteBuffer.allocate(4);
        try {
            client.read(lengthBuf);
        } catch (SocketException e) {
            throw new BadMessageException(e.getMessage());
        }
        MessageInfo message = new MessageInfo();
        String lengthStr = new String(lengthBuf.array());
        message.length = ByteOperations.convertFromBytes(lengthStr);
        if (message.length == 0) {
            message.type = KEEP_ALIVE;
            return message; // keep-alive
        }
        if (message.length < 0) {
            throw new BadMessageException("=== Bad length");
        }
        ByteBuffer messageBuf = ByteBuffer.allocate(message.length);
        int count = client.read(messageBuf);
        String data = new String(messageBuf.array());
        if (count != message.length) {
            System.err.println("Read just " + count + " / " + message.length + " bytes.");
        }
        message.type = Integer.parseInt(String.valueOf(data.charAt(0)));
        message.data = data.substring(1);
        return message;
    }
}
