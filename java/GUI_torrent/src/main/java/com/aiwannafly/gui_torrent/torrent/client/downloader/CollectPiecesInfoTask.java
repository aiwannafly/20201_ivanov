package com.aiwannafly.gui_torrent.torrent.client.downloader;

import com.aiwannafly.gui_torrent.torrent.client.util.ByteOperations;
import com.aiwannafly.gui_torrent.torrent.client.messages.Message;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;

public class CollectPiecesInfoTask implements Callable<ResponseInfo> {
    private final DownloadManager.PeerInfo peerInfo;
    private final int peerPort;


    CollectPiecesInfoTask(DownloadManager.PeerInfo peerInfo, int peerPort) {
        this.peerInfo = peerInfo;
        this.peerPort = peerPort;
    }

    @Override
    public ResponseInfo call() throws Exception {
        ResponseInfo result = new ResponseInfo();
        result.status = ResponseInfo.Status.NOT_RESPONDS;
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
            if (keysIterator.hasNext()) {
                keysIterator.next().cancel();
            }
            if (returnValue == 0) {
                peerInfo.channel.configureBlocking(true);
                return result;
            }
            result.messageInfo = Message.getMessage(peerInfo.channel);
            if (result.messageInfo.type == Message.KEEP_ALIVE) {
                type = Message.KEEP_ALIVE;
                continue;
            }
            type = result.messageInfo.type;
            timeout = 5000 - (System.currentTimeMillis() - startTime);
        } while (type == Message.KEEP_ALIVE && timeout > 0);
        if (type == Message.HAVE) {
            result.status = ResponseInfo.Status.HAVE;
            result.pieceIdx = ByteOperations.convertFromBytes(
                    result.messageInfo.data.substring(0, 4));
            result.newAvailablePieces = new ArrayList<>();
            result.newAvailablePieces.add(result.pieceIdx);
        }
        peerInfo.channel.configureBlocking(true);
        return result;
    }
}
