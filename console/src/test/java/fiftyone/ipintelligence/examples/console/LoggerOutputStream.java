package fiftyone.ipintelligence.examples.console;

import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LoggerOutputStream extends OutputStream {
    private final ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
    private final Logger logger;

    public LoggerOutputStream(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void write(int b) {
        memoryStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        memoryStream.write(b, off, len);
    }

    @Override
    public void flush() {
        logger.info(memoryStream.toString());
        memoryStream.reset();
    }

    @Override
    public void close() throws IOException {
        flush();
        memoryStream.close();
    }
}
