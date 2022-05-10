package torrent;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        Map<Class, Integer> m = new HashMap<>();
        Integer a = Integer.parseInt("10");
        m.put(a.getClass(), 2);
        System.out.println(m.get(a.getClass()));
    }
}
