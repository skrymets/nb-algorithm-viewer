package org.netbeans.module.sandbox.dot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

public class DotBridge implements Cloneable {

    public static final String DOT_BINARY = "C:\\opt\\graphviz\\2.38\\bin\\dot.exe";

    private Process dotProcess;

    public String convertDOT2SVG(String dotInput) throws DotBridgeException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                DOT_BINARY,
                "-Tsvg"
        );

        OutputStreamWriter dotStandardInput;
        try {
            dotProcess = processBuilder.start();
            dotStandardInput = new OutputStreamWriter(dotProcess.getOutputStream());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try (InputStream dot_stderr = dotProcess.getErrorStream();
                InputStream dot_stdout = dotProcess.getInputStream();) {

            ThreadedStream errorStream = new ThreadedStream(dot_stderr);
            ThreadedStream outputStream = new ThreadedStream(dot_stdout);
            errorStream.start();
            outputStream.start();

            dotStandardInput.write(dotInput);
            dotStandardInput.close();
            dotProcess.waitFor();

            errorStream.join(4000);
            outputStream.join(4000);

            String error = errorStream.getData();
            if (error != null && !error.isEmpty()) {
                throw new DotBridgeException(error);
            }

            String data = outputStream.getData();

            return data;

        } catch (IOException | InterruptedException ex) {
            throw new DotBridgeException(ex);
        }

    }

    class ThreadedStream extends Thread {

        private final InputStream streamToRead;
        private final StringBuilder sb = new StringBuilder();

        ThreadedStream(InputStream streamToRead) {
            this.streamToRead = streamToRead;
        }

        @Override
        public void run() {
            int read;
            try {
                while ((read = streamToRead.read()) != -1) {
                    sb.append((char) read);
                }
            } catch (IOException e) {
                sb.append('\n');
                sb.append(e.toString());
            }
        }

        public String getData() {
            return sb.toString();
        }
    }
}
