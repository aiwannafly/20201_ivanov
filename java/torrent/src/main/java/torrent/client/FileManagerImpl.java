package torrent.client;

import torrent.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileManagerImpl implements FileManager {
    private final Map<String, FileOutputStream> outputStreams = new HashMap<>();
    private final Map<String, FileInputStream> inputStreams = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    @Override
    public synchronized byte[] readPiece(String fileName, int idx, int begin, int length) throws IOException {
        if (!inputStreams.containsKey(fileName)) {
            File file = new File(Constants.PATH + fileName);
            inputStreams.put(fileName, new FileInputStream(file));
        }
        FileInputStream fileInputStream = inputStreams.get(fileName);
        byte[] piece = new byte[length];
        try {
            int readBytes = fileInputStream.read(piece);
            if (readBytes != length) {
                System.err.println("Read " + readBytes + " / " + length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return piece;
    }

    @Override
    public synchronized void writePiece(String fileName, int idx, int begin, byte[] piece) throws IOException {
        if (!outputStreams.containsKey(fileName)) {
            File file = new File(Constants.PATH + fileName);
            outputStreams.put(fileName, new FileOutputStream(file));
        }
        FileOutputStream fileOutputStream = outputStreams.get(fileName);
        executor.execute((() -> {
            try {
                fileOutputStream.write(piece);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public synchronized void close() throws IOException {
        executor.shutdown();
        for (FileInputStream stream: inputStreams.values()) {
            stream.close();
        }
        for (FileOutputStream stream: outputStreams.values()) {
            stream.close();
        }
    }
}
