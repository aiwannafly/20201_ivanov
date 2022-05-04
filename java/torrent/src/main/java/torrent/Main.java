package torrent;

public class Main {

    public static void main(String[] args) {
        TrackerServer trackerServer = new TrackerServer();
        trackerServer.run();
        TorrentClient firstClient = new TorrentClient();
        firstClient.sendMessageToServer("show peers");
        System.out.println(firstClient.receiveMessage());
        TorrentClient secondClient = new TorrentClient();
        secondClient.sendMessageToServer("show peers");
        String peersList = secondClient.receiveMessage();
        String[] peers = peersList.split(" ");
        System.out.println(peersList);
    }
}
