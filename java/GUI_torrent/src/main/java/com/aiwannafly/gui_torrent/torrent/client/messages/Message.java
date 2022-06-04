package com.aiwannafly.gui_torrent.torrent.client.messages;

import com.aiwannafly.gui_torrent.torrent.client.exceptions.BadMessageException;
import com.aiwannafly.gui_torrent.torrent.client.util.ByteOperations;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
        public Piece piece;
    }

    public static class Piece {
        public byte[] data;
        public int idx;
        public int begin;
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
            return message;
        }
        if (message.length < 0) {
            throw new BadMessageException("=== Bad length");
        }
        ByteBuffer typeBuf = ByteBuffer.allocate(1);
        client.read(typeBuf);
        String typeStr = new String(typeBuf.array());
        message.type = Integer.parseInt(typeStr);
        message.length--;
        if (message.type != PIECE) {
            ByteBuffer messageBuf = ByteBuffer.allocate(message.length);
            int count = client.read(messageBuf);
            message.data = new String(messageBuf.array());
            if (count != message.length) {
                System.err.println("Read just " + count + " / " + message.length + " bytes.");
            }
            return message;
        }
        Piece piece = new Piece();
        ByteBuffer buf = ByteBuffer.allocate(4);
        client.read(buf);
        message.length -= 4;
        piece.idx = ByteOperations.convertFromBytes(new String(buf.array()));
        buf.clear();
        client.read(buf);
        message.length -= 4;
        piece.begin = ByteOperations.convertFromBytes(new String(buf.array()));
        InputStream in = client.socket().getInputStream();
        piece.data = new byte[message.length];
        int count = in.read(piece.data);
        while (count != message.length) {
            int return_value = in.read(piece.data, count, message.length - count);
            count += return_value;
        }
        message.piece = piece;
        return message;
    }
}
