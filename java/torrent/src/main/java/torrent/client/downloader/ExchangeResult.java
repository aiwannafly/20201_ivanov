package torrent.client.downloader;

import java.util.ArrayList;

public class ExchangeResult {
    public Status status;
    public int peerPort;
    public int pieceIdx;
    public boolean receivedKeepAlive = false;
    public long keepAliveTimeMillis = 0;
    public ArrayList<Integer> newAvailablePieces;
    public String message;

    public enum Status {
        RECEIVED, HAVE, GOT_KEEP_ALIVE, LOST, NOT_RESPONDS
    }

    public ExchangeResult() {
    }

    public ExchangeResult(Status status, int peerPort, int pieceIdx) {
        this.status = status;
        this.peerPort = peerPort;
        this.pieceIdx = pieceIdx;
    }
}
