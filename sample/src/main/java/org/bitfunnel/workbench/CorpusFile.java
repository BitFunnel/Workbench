package org.bitfunnel.workbench;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class CorpusFile {
  InputStream inputStream;
  int currentByte;


  CorpusFile(InputStream inputStream)
  {
    this.inputStream = inputStream;
    currentByte = -1;
  }


  void process(IDocumentProcessor processor) {
    processor.openDocumentSet();
    while (peek() != 0)
    {
      processDocument(processor);
    }
    consume(0);
    processor.closeDocumentSet();
  }


  void processDocument(IDocumentProcessor processor) {
    processor.openDocument();
    while (peek() != 0)
    {
      processStream(processor);
    }
    consume(0);
    processor.closeDocument();
  }


  void processStream(IDocumentProcessor processor) {
    processor.openStream(parseTerm());
    while (peek() != 0)
    {
      processor.term(parseTerm());
    }
    consume(0);
    processor.closeStream();
  }


  String parseTerm()
  {
    ByteArrayOutputStream builder = new ByteArrayOutputStream();
    while (peek() != 0) {
      if (peek() == -1) {
        throw new RuntimeException("Attempted read past end of stream.");
      }

      builder.write((byte)get());
    }
    // consume zero at end of term.
    consume(0);

    return builder.toString();
  }


  private void consume(int expected)
  {
    if (expected != (int) get()) {
      throw new RuntimeException("Expected " + (int)expected);
    }
  }


  private int peek()
  {
    try {
      if (currentByte == -1) {
        currentByte = inputStream.read();
      }
    }
    catch (IOException e)
    {
      currentByte = -1;
    }
    return currentByte;
  }


  private int get()
  {
    int result = peek();
    if (result == -1) {
      throw new RuntimeException("Attempted read past end of stream.");
    }
    currentByte = -1;

    return (byte)result;
  }
}
