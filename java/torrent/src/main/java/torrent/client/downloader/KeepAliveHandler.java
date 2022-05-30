package torrent.client.downloader;

import torrent.Constants;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class KeepAliveHandler {
    private final Map<Integer, DownloadManager.SeedInfo> seedsInfo;
    private final Timer keepAliveSendTimer = new Timer();
    private final Timer keepAliveReceiveTimer = new Timer();

    public KeepAliveHandler(Map<Integer, DownloadManager.SeedInfo> seedsInfo) {
        this.seedsInfo = seedsInfo;
    }

    public void start() {
        TimerTask sendKeepAliveTask = new KeepAliveHandler.SendKeepAliveTask();
        TimerTask receiveKeepAliveTask = new KeepAliveHandler.ReceiveKeepAliveTask();
        keepAliveSendTimer.schedule(sendKeepAliveTask, 0, Constants.KEEP_ALIVE_SEND_INTERVAL);
        keepAliveReceiveTimer.schedule(receiveKeepAliveTask, 0, Constants.MAX_KEEP_ALIVE_INTERVAL);
    }

    public void stop() {
        keepAliveReceiveTimer.cancel();
        keepAliveSendTimer.cancel();
    }

    private class SendKeepAliveTask extends TimerTask {
        @Override
        public void run() {
            for (DownloadManager.SeedInfo info: seedsInfo.values()) {
                System.out.println("=== Send keep-alive");
                String keepAliveMsg = "\0\0\0\0";
                info.out.print(keepAliveMsg);
                info.out.flush();
            }
        }
    }

    private class ReceiveKeepAliveTask extends TimerTask {
        @Override
        public void run() {
            for (Integer peerPort: seedsInfo.keySet()) {
                if (getTimeFromLastKeepAlive(peerPort) > Constants.MAX_KEEP_ALIVE_INTERVAL) {
                    System.out.println("=== Close connection");
                    // close connection
                    DownloadManager.SeedInfo info = seedsInfo.get(peerPort);
                    try {
                        info.in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    info.out.close();
                    seedsInfo.remove(peerPort);
                }
            }
        }
    }

    private long getTimeFromLastKeepAlive(Integer peerPort) {
        return System.currentTimeMillis() - seedsInfo.get(peerPort).lastKeepAliveTimeMillis;
    }
}