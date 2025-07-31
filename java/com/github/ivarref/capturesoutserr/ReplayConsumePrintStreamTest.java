package com.github.ivarref.capturesoutserr;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ReplayConsumePrintStreamTest {

    public static final PrintStream stdOutOriginal = System.out;

    public static void main(String[] args) {
        try (final ReplayConsumePrintStream newStdOut = new ReplayConsumePrintStream(null, true)) {
            System.out.println("Starting ReplayConsumePrintStreamTest");
            System.setOut(newStdOut);
            System.out.println("This message should appear in sout.log æøå");
            System.out.println("ሰላም አለም");
            System.out.println("ഹലോ വേൾഡ്");
            System.out.println("你好世界");
            stdOutOriginal.println("Original stdout still works!");
            newStdOut.setConsumer(ReplayConsumePrintStreamTest::logToFile);
        }
    }

    public static synchronized void logToFile(final String msg) {
        final String fileName = "sout_replay.log";
        try (final FileWriter fw = new FileWriter(fileName, StandardCharsets.UTF_8, true);
             final PrintWriter pw = new PrintWriter(fw)) {
            pw.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
