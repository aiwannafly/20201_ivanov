package torrent.client.downloader;

import torrent.Constants;
import torrent.client.uploader.UploadHandler;
import torrent.client.util.ByteOperations;
import torrent.client.messages.Message;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

public class CollectPiecesInfoTask implements Callable<ExchangeResult> {
    private final DownloadManager.PeerInfo peerInfo;
    private final int peerPort;


    CollectPiecesInfoTask(DownloadManager.PeerInfo peerInfo, int peerPort) {
        this.peerInfo = peerInfo;
        this.peerPort = peerPort;
    }

    @Override
    public ExchangeResult call() throws Exception {
        ExchangeResult result = new ExchangeResult();
        result.status = ExchangeResult.Status.NOT_RESPONDS;
        result.peerPort = peerPort;
        Selector selector = Selector.open();
        peerInfo.channel.configureBlocking(false);
        peerInfo.channel.register(selector, SelectionKey.OP_READ);
        long timeout = 5000;
        int type;
        long startTime = System.currentTimeMillis();
        do {
            int returnValue = selector.select(timeout);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
            keysIterator.next().cancel();
            if (returnValue == 0) {
                peerInfo.channel.configureBlocking(true);
                return result;
            }
            Message.MessageInfo message = Message.getMessage(peerInfo.channel);
            if (message.type == Message.KEEP_ALIVE) {
                type = Message.KEEP_ALIVE;
                continue;
            }
            type = Integer.parseInt(String.valueOf(result.message.charAt(4)));
            timeout = 5000 - (System.currentTimeMillis() - startTime);
        } while (type == Message.KEEP_ALIVE && timeout > 0);
        if (type == Message.HAVE) {
            result.status = ExchangeResult.Status.HAVE;
            result.pieceIdx = ByteOperations.convertFromBytes(result.message.substring(5, 9));
            result.newAvailablePieces = new ArrayList<>();
            result.newAvailablePieces.add(result.pieceIdx);
        }
        peerInfo.channel.configureBlocking(true);
        return result;
    }
}
