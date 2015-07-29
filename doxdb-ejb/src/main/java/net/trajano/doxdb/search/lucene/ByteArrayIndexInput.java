package net.trajano.doxdb.search.lucene;

import java.io.IOException;

import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.IOContext;

public class ByteArrayIndexInput extends BufferedIndexInput {

    private final byte[] buffer;

    private int pos;

    public ByteArrayIndexInput(final String resourceDesc,
        final IOContext context,
        final byte[] buffer) {
        super(resourceDesc, context);
        this.buffer = buffer;
        pos = 0;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public long length() {

        return buffer.length;
    }

    @Override
    protected void readInternal(final byte[] b,
        final int offset,
        final int length) throws IOException {

        System.arraycopy(buffer, pos, b, offset, length);

    }

    @Override
    protected void seekInternal(final long pos) throws IOException {

        this.pos = (int) pos;
    }

}
