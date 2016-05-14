package org.bitfunnel.test;

import java.io.OutputStream;

public class FileScope2 implements java.lang.AutoCloseable
{
    OutputStream outputStream;

    public FileScope2(OutputStream outputStream)
    {
        this.outputStream = outputStream;
        System.out.println("  Enter file scope");
    }

    @Override
    public void close() throws Exception {
        // Write trailing '\0'
        System.out.println("  Exit file scope");
    }
}
