package com.tf.search.lucene.store;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link DataOutput} wrapping a plain {@link OutputStream}.
 */
public class OutputStreamDataOutput extends DataOutput implements Closeable {
    private final OutputStream os;

    public OutputStreamDataOutput(OutputStream os) {
        this.os = os;
    }

    @Override
    public void writeByte(byte b) throws IOException {
        os.write(b);
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        os.write(b, offset, length);
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
