package torrent.client;

import java.io.IOException;

public interface FileManager extends AutoCloseable {

    /** Reads a piece of a file
     @param idx - index of a block where the piece is
     @param begin - offset in the block
     @param length - length of a piece in bytes
     @return a piece in this location presented as a byte array
     */
    byte[] readPiece(String fileName, int idx, int begin, int length) throws IOException;

    /** Writes a piece into a file
     @param idx - index of a block where the piece is
     @param begin - offset in the block
     @param piece -  a piece in this location presented as a byte array
     */
    void writePiece(String fileName, int idx, int begin, byte[] piece) throws IOException;

}
