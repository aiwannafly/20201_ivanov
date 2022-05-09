package torrent;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i /*>> 0*/);

        return result;
    }

    public static String getStringFromBytes(byte[] arr) {
        StringBuilder str = new StringBuilder();
        for (byte b : arr) {
            str.append((char) b);
        }
        return str.toString();
    }

    public static void main(String[] args) throws IOException {

    }
}
