package com.github.ivarref.capturesoutserr;

import java.io.*;
import java.util.function.Consumer;

/** Based on sun.rmi.runtime.Log.LoggerPrintStream */
public class ConsumePrintStream extends PrintStream {

    private int last = -1;

    /**
     * stream used for buffering lines
     */
    private final ByteArrayOutputStream bufOut;
    private final Consumer<String> consumer;


    public ConsumePrintStream(final Consumer<String> stringLineConsumer) {
        super(new ByteArrayOutputStream());
        this.consumer = stringLineConsumer;
        this.bufOut = (ByteArrayOutputStream) super.out;
    }

    public void write(final int b) {
        if ((last == '\r') && (b == '\n')) {
            last = -1;
            return;
        } else if ((b == '\n') || (b == '\r')) {
            try {
                /* Consume a single line */
                String line = bufOut.toString();
                consumer.accept(line);
            } finally {
                bufOut.reset();
            }
        } else {
            super.write(b);
        }
        last = b;
    }

    public void write(final byte[] b, final int off, final int len) {
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException(len);
        }
        for (int i = 0; i < len; i++) {
            write(b[off + i]);
        }
    }
}
