package com.github.ivarref.capturesoutserr;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ReplayConsumePrintStream extends PrintStream {

    public static final int BUFFER_SIZE = 100_000;
    private int last = -1;

    /**
     * stream used for buffering lines
     */
    private final ByteArrayOutputStream bufOut;
    private Consumer<String> consumer;
    private final List<String> buffer = new ArrayList<>(BUFFER_SIZE);

    public ReplayConsumePrintStream() {
        super(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        this.bufOut = (ByteArrayOutputStream) super.out;
    }

    public synchronized void setConsumer(Consumer<String> lineConsumer) {
        consumer = lineConsumer;
        for (String line : buffer) {
            consumer.accept(line);
        }
        buffer.clear();
    }

    private synchronized void consumeLine(final String line) {
        if (consumer == null) {
            if (buffer.size() == BUFFER_SIZE) {
                buffer.clear();
                buffer.add("(Stream truncated ...)");
                buffer.add(line);
            } else {
                buffer.add(line);
            }
        } else {
            consumer.accept(line);
        }
    }

    @Override
    public synchronized final void write(final int b) {
        if ((last == (int) '\r') && (b == (int) '\n')) {
            last = -1;
            return;
        } else if ((b == (int) '\n') || (b == (int) '\r')) {
            try {
                /* Consume a single line */
                String line = bufOut.toString();
                consumeLine(line);
            } finally {
                bufOut.reset();
            }
        } else {
            super.write(b);
        }
        last = b;
    }

    @Override
    public synchronized final void write(final byte[] b, final int off, final int len) {
        if (len < 0) {
            throw new ArrayIndexOutOfBoundsException(len);
        }
        for (int i = 0; i < len; i++) {
            write((int) b[off + i]);
        }
    }
}
