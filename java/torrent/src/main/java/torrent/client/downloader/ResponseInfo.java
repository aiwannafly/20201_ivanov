package torrent.client.downloader;

import torrent.client.messages.Message;

import java.util.ArrayList;

public class ResponseInfo {
    public Status status;
    public int peerPort;
    public int pieceIdx;
    public boolean receivedKeepAlive = false;
    public long keepAliveTimeMillis = 0;
    public ArrayList<Integer> newAvailablePieces;
    public Message.MessageInfo messageInfo;

    public enum Status {
        RECEIVED, HAVE, GOT_KEEP_ALIVE, LOST, NOT_RESPONDS
    }

    public ResponseInfo() {
    }

    public ResponseInfo(Status status, int peerPort, int pieceIdx) {
        this.status = status;
        this.peerPort = peerPort;
        this.pieceIdx = pieceIdx;
    }
}
