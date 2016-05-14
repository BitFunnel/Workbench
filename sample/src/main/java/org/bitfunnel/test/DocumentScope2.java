package org.bitfunnel.test;

import java.io.OutputStream;

public class DocumentScope2 implements java.lang.AutoCloseable
{
    OutputStream outputStream;

    public DocumentScope2(OutputStream outputStream)
    {
        this.outputStream = outputStream;
        System.out.println("    Enter document scope");
    }

    @Override
    public void close() throws Exception {
        // Write trailing '\0'
        System.out.println("    Exit document scope");
    }
}
