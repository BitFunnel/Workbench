package org.bitfunnel.test;

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
    current = parseNext();
  }


  @Override
  public Iterator<Document> iterator() {
    return this;
  }


  @Override
  public boolean hasNext() {
    return peek() != -1;
  }


  @Override
  public Document next() {
    if (peek() == -1)
    {
      throw new NoSuchElementException();
    }
    return new Document();
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
        StringBuilder builder = new StringBuilder();
        while (peek() != 0) {
          if (peek() == -1) {
            throw new RuntimeException("Attempted read pas end of stream.");
          }

          builder.append(get());
        }
        // Consume trailing zero.
        Consume(0);

        return builder.toString();
      }
      else {
        return null;
      }
    }
  }
}
