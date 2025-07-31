package com.github.ivarref.capturesoutserr;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ReplayConsumePrintStreamTruncateTest {
    public static final PrintStream stdOutOriginal = System.out;

    public static void main(String[] args) {
        try (final ReplayConsumePrintStream newStdOut = new ReplayConsumePrintStream(null, true)) {
            System.out.println("Starting ReplayConsumePrintStreamTruncateTest");
            System.setOut(newStdOut);
            for (int i = 0; i< 100_000; i++) {
                System.out.println("" + i);
            }
            System.out.println("This message should appear in sout.log æøå");
            System.out.println("ሰላም አለም");
            System.out.println("ഹലോ വേൾഡ്");
            System.out.println("你好世界");
            stdOutOriginal.println("Original stdout still works!");
            newStdOut.setConsumer(ReplayConsumePrintStreamTruncateTest::logToFile);
        }
    }

    public static synchronized void logToFile(final String msg) {
        final String fileName = "sout_truncate.log";
        try (final FileWriter fw = new FileWriter(fileName, StandardCharsets.UTF_8, true);
             final PrintWriter pw = new PrintWriter(fw)) {
            pw.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
