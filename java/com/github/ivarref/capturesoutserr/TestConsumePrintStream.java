package com.github.ivarref.capturesoutserr;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class TestConsumePrintStream {

    public static final PrintStream stdOutOriginal = System.out;

    public static void main(final String[] ignoredArgs) {
        try (final PrintStream newStdOut = new ConsumePrintStream(TestConsumePrintStream::logToFile)) {
            System.setOut(newStdOut);
            System.out.println("This message should appear in sout.log æøå");
            System.out.println("ሰላም አለም");
            System.out.println("ഹലോ വേൾഡ്");
            System.out.println("你好世界");
            stdOutOriginal.println("Original stdout still works!");
        }
    }

    public static synchronized void logToFile(final String msg) {
        final String fileName = "sout.log";
        try (final FileWriter fw = new FileWriter(fileName, StandardCharsets.UTF_8, true);
             final PrintWriter pw = new PrintWriter(fw)) {
            pw.println(msg);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
