package org.bitfunnel.test;

import java.io.OutputStream;

public class StreamScope2 implements java.lang.AutoCloseable
{
    OutputStream outputStream;

    public StreamScope2(OutputStream outputStream, String name)
    {
        this.outputStream = outputStream;
        // Convert name to UTF-8
        // Write name and trailing '\0'
        System.out.println("      Open stream " + name);
    }

    @Override
    public void close() throws Exception {
        // Write trailing '\0'
        System.out.println("      Close stream");
    }
}
