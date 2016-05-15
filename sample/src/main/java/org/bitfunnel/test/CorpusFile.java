package org.bitfunnel.test;

import com.sun.xml.internal.bind.v2.model.core.ID;
import sun.print.DocumentPropertiesUI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class CorpusFile implements Iterable<CorpusFile.Document>,
                                   Iterator<CorpusFile.Document> {
  InputStream inputStream;
  int currentByte;
  Document current;


  CorpusFile(InputStream inputStream)
  {
    this.inputStream = inputStream;
    currentByte = -1;
//    current = parseNext();
  }


  void process(IDocumentProcessor processor) {
    processor.OpenDocumentSet();
    while (peek() != 0)
    {
      processDocument(processor);
    }
    Consume(0);
    processor.CloseDocumentSet();
  }


  void processDocument(IDocumentProcessor processor) {
    processor.OpenDocument();
    while (peek() != 0)
    {
      processStream(processor);
    }
    Consume(0);
    processor.CloseDocument();
  }


  void processStream(IDocumentProcessor processor) {
    processor.OpenStream(parseTerm());
    while (peek() != 0)
    {
      processor.Term(parseTerm());
    }
    Consume(0);
    processor.CloseStream();
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
    // Consume zero at end of term.
    Consume(0);

    return builder.toString();
  }


  @Override
  public Iterator<Document> iterator() {
    return this;
  }


  @Override
  public boolean hasNext() {
    return current != null;
  }


  @Override
  public Document next() {
    if (current == null)
    {
      throw new NoSuchElementException();
    }
    Document result = current;
    current = parseNext();
    if (current == null) {
      // Consume trailing 0 that marks end of the file.
      Consume(0);
    }
    return result;
  }


  private Document parseNext() {
    if (peek() != 0) {
      return new Document();
    }
    else {
      return null;
    }
  }


  private void Consume(int expected)
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


  public class Document implements Iterable<Stream>, Iterator<Stream> {
    Stream current;


    public Document() {
      current = parseNext();
    }


    @Override
    public Iterator<Stream> iterator() {
      return this;
    }


    @Override
    public boolean hasNext() {
      return current != null;
    }


    @Override
    public Stream next() {
      if (current == null) {
        throw new NoSuchElementException();
      }
      Stream result = current;
      current = parseNext();
      if (current == null) {
        // Consume trailing 0 that marks end of document.
        Consume(0);
      }
      return result;
    }


    private Stream parseNext() {
      if (peek() != 0) {
        return new Stream();
      }
      else {
        return null;
      }
    }
  }


  public class Stream implements Iterable<String>, Iterator<String> {
    String current;
    String name;


    public Stream()
    {
      name = parseNext();
      if (name == null) {
        throw new RuntimeException("Expected stream name.");
      }
      current = parseNext();
    }


    public String name()
    {
      return name;
    }


    @Override
    public Iterator<String> iterator() {
      return this;
    }


    @Override
    public boolean hasNext() {
      return current != null;
    }


    @Override
    public String next() {
      if (current == null) {
        throw new NoSuchElementException();
      }
      String result = current;
      current = parseNext();
      if (current == null)
      {
        // Consume trailing zero that marks end of stream.
        Consume(0);
      }
      return result;
    }


    private String parseNext() {
      if (peek() != 0) {
        ByteArrayOutputStream builder = new ByteArrayOutputStream();
        while (peek() != 0) {
          if (peek() == -1) {
            throw new RuntimeException("Attempted read pas end of stream.");
          }

          builder.write((byte)get());
        }
        // Consume zero at end of term.
        Consume(0);

        return builder.toString();
      }
      else {
        return null;
      }
    }
  }
}
