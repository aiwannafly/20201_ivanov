package torrent;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinaryOperations {
    private final static int INT_LEN = 32;

    public static String convertIntoBytes(int number) {
        String bin = Integer.toBinaryString(number);
        String fullBinary = String.valueOf('0').repeat(INT_LEN - bin.length()) + bin;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < fullBinary.length() / 8; i++) {
            int a = Integer.parseInt(fullBinary.substring(8 * i, (i + 1) * 8), 2);
            str.append((char) (a));
        }
        return str.toString();
    }

    public static int convertFromBytes(String bytes) {
        return ByteBuffer.wrap(bytes.substring(0, 4).getBytes(StandardCharsets.UTF_8)).getInt();
    }

    public static String getStringFromBytes(byte[] arr) {
        StringBuilder str = new StringBuilder();
        for (byte b: arr) {
            str.append((char) b);
        }
        return str.toString();
    }

    public static byte[] getBytesFromString(String str) {
        byte[] arr = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            arr[i] = (byte) str.charAt(i);
        }
        return arr;
    }
}
