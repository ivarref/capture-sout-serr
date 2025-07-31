package com.github.ivarref.capturesoutserr;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

// Some code copied from sun.rmi.runtime.Log.LoggerPrintStream
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
    private final List<String> buffer;
    private final PrintStream copyStream;
    private final boolean buffering;

    public ReplayConsumePrintStream(final PrintStream copyToStream, boolean doBuffer) {
        super(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8);
        this.bufOut = (ByteArrayOutputStream) super.out;
        this.id = instanceNumber.getAndIncrement();
        this.copyStream = copyToStream;
        this.buffering = doBuffer;
        if (doBuffer) {
            buffer = new ArrayList<>(BUFFER_SIZE);
        } else {
            buffer = null;
        }
    }

    private synchronized void debug(String msg) {
        if ("TRUE".equalsIgnoreCase(System.getenv("ReplayConsumePrintStreamDebug"))) {
            final String fileName = "./debug.log";
            try (final FileWriter fw = new FileWriter(fileName, StandardCharsets.UTF_8, true);
                 final PrintWriter pw = new PrintWriter(fw)) {
                pw.println(msg);
                if (copyStream != null) {
                    copyStream.println("DEBUG " + msg);
                }
            } catch (IOException e) {
                if (copyStream != null) {
                    copyStream.println("Error during writing debug log: " + e.getMessage());
                    e.printStackTrace(copyStream);
                }
                throw new RuntimeException(e);
            }
        }
    }

    public synchronized void setConsumer(Consumer<String> lineConsumer) {
        consumer = lineConsumer;
        if (buffering) {
            for (String line : buffer) {
                debug("UNBUFFERING: " + line);
                try {
                    consumer.accept(line);
                } catch (Throwable t) {
                    debug("UNBUFFERING FAILED: " + t.getMessage());
                }
            }
            buffer.clear();
        } else {
            debug("NO UNBUFFERING");
        }
    }

    private synchronized void consumeLine(final String line) {
        if (consumer == null) {
            if (buffering) {
                if (buffer.size() == BUFFER_SIZE) {
                    buffer.clear();
                    buffer.add("(Stream truncated ...)");
                    buffer.add(line);
                } else {
                    buffer.add(line);
                }
                debug("BUFFERING: " + line);
            } else {
                debug("NOT BUFFERING: " + line);
            }
        } else {
            debug("DIRECT SEND: " + line);
            try {
                consumer.accept(line);
            } catch (Throwable t) {
                String message = t.getMessage();
                debug("DIRECT SEND FAILED: " + message);
                if (copyStream != null) {
                    t.printStackTrace(copyStream);
                }
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
