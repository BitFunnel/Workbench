/*
The MIT License (MIT)

Copyright (c) 2016 Microsoft

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
