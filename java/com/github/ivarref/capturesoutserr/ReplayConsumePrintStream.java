package com.github.ivarref.capturesoutserr;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class ReplayConsumePrintStream extends PrintStream {

    public static final int BUFFER_SIZE = 100_000;
    private int last = -1;

    public static final AtomicLong instanceNumber = new AtomicLong(1L);

    private final long id;
    /**
     * stream used for buffering lines
     */
    private final ByteArrayOutputStream bufOut;
    private Consumer<String> consumer;
    private final List<String> buffer = new ArrayList<>(BUFFER_SIZE);

    public ReplayConsumePrintStream() {
        super(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        this.bufOut = (ByteArrayOutputStream) super.out;
        this.id = instanceNumber.getAndIncrement();
    }

    public static synchronized void debug(String msg) {
        if ("TRUE".equalsIgnoreCase(System.getenv("ReplayConsumePrintStreamDebug"))) {
            final String fileName = "./debug.log";
            try (final FileWriter fw = new FileWriter(fileName, StandardCharsets.UTF_8, true);
                 final PrintWriter pw = new PrintWriter(fw)) {
                pw.println(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void setConsumer(Consumer<String> lineConsumer) {
        consumer = lineConsumer;
        for (String line : buffer) {
            debug("UNBUFFERING: " + line);
            try {
                consumer.accept(line);
            } catch (Throwable t) {
                debug("UNBUFFERING FAILED: " + t.getMessage());
            }
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
            debug("BUFFERING: " + line);
        } else {
            debug("DIRECT SEND: " + line);
            try {
                consumer.accept(line);
            } catch (Throwable t) {
                debug("DIRECT SEND FAILED: " + t.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return "com.github.ivarref.capturesoutserr.ReplayConsumePrintStream@" + id + ",consumer=" + consumer;
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
