package torrent.client;

import torrent.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileManagerImpl implements FileManager {
    private final File file;
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;
    private final static ExecutorService executor = Executors.newFixedThreadPool(1);
    private final Mode mode;

    public enum Mode {
        READ, WRITE
    }

    public FileManagerImpl(String fileName, Mode mode) throws IOException {
        this.file = new File(Constants.PATH + fileName);
        this.mode = mode;
        if (mode == Mode.READ) {
            this.fileInputStream = new FileInputStream(file);
        } else {
            this.fileOutputStream = new FileOutputStream(file);
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public byte[] readPiece(int idx, int begin, int length) throws IOException {
        if (mode == Mode.WRITE) {
            throw new IOException("Only write mode");
        }
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
    public void writePiece(int idx, int begin, byte[] piece) throws IOException {
        if (mode == Mode.READ) {
            throw new IOException("Only read mode");
        }
        executor.execute((() -> {
            try {
                fileOutputStream.write(piece);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void close() throws IOException {
        executor.shutdown();
        if (mode == Mode.READ) {
            fileInputStream.close();
        } else {
            fileOutputStream.close();
        }
    }
}
