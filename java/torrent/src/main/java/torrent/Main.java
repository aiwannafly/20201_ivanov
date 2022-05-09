package torrent;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;

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
        File f1 = new File(Settings.PATH + "wallpaper.jpg");
        File f2 = new File(Settings.PATH + "wallpaper.jpg.txt");
        FileInputStream i1 = new FileInputStream(f1);
        FileInputStream i2 = new FileInputStream(f2);
        byte[] b1 = i1.readNBytes(8);
        byte[] b2 = i2.readNBytes(8);
        byte a = -128;
        byte b = 127;
        char c = 0;
        for (int i = 0; i < 8; i++) {
            System.out.println("b1: " + b1[i]);
            System.out.println("b2: " + b2[i]);
        }
//        File file = new File(Settings.PATH + "wallpaper.jpg");
//        File test = new File(Settings.PATH + "test.txt");
//        FileInputStream inputStream = new FileInputStream(file);
//        FileOutputStream outputStream = new FileOutputStream(test);
//        byte[] piece = inputStream.readAllBytes();
//        String str = BinaryOperations.getStringFromBytes(piece);
//        System.out.println(str);
//        byte[] piece2 = BinaryOperations.getBytesFromString(str);
//        outputStream.write(piece2);
//        System.out.println(str);
//        outputStream.write(BinaryOperations.getBytesFromString(str));
//        int a = 13;
//        String bytes = BinaryOperations.convertIntoBytes(a);
//        System.out.println(bytes);
//        System.out.println(BinaryOperations.convertFromBytes(bytes));
    }
}
