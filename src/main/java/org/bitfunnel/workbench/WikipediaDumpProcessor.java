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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class WikipediaDumpProcessor {
  private static final int titleStreamId = 0;
  private static final int bodyStreamId = 1;

  InputStream inputStream;
  OutputStream outputStream;
  Scanner scanner;
  String line;

  static Pattern pattern =
      Pattern.compile("<doc id=\"([^\"]*)\" url=\"[^\"]*\" title=\"([^\"]*)\">");

  static CharArraySet stopwords = new CharArraySet(0, true);
  static Analyzer analyzer = new StandardAnalyzer(stopwords);


  public WikipediaDumpProcessor(InputStream inputStream,
                                OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }


  public void ProcessFile() throws IOException, Exception {
    System.out.println("Process file.");
    scanner = new Scanner(inputStream);
    ProcessAllDocuments();
  }


  private void ProcessAllDocuments() throws Exception {
    try (FileScope scope = new FileScope()) {
      while (scanner.hasNextLine()) {
        ProcessOneDocument();
      }
    }
  }


  private void ProcessOneDocument() throws Exception {
    try (DocumentScope scope = new DocumentScope()) {
      ProcessDocumentHeader();
      ProcessAllContentLines();
      ProcessDocumentFooter();
    }
  }


  private void ProcessDocumentHeader() throws Exception {
    String line = GetLine();

    Matcher matcher = pattern.matcher(line);
    if (!matcher.find()) {
      throw new RuntimeException("Malformed document header.");
    }

    int documentId = Integer.parseUnsignedInt(matcher.group(1));
    emit(String.format("%016x", documentId));
    String title = matcher.group(2);

    try (StreamScope scope = new StreamScope(titleStreamId)) {
        try (TokenStream tokenStream
                = analyzer.tokenStream("contents", new StringReader(title))) {
            tokenStream.reset();
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                emit(term.toString());
            }
        }
    }
  }


  private void ProcessAllContentLines() throws Exception {
    try (StreamScope scope = new StreamScope(bodyStreamId)) {
      while (true) {
        String line = PeekLine();
        if (!IsDocumentEnd(line)) {
          ProcessOneContentLine();
        } else {
          break;
        }
      }
    }
  }


  private void ProcessOneContentLine() throws IOException {
    String line = GetLine();

    try (TokenStream tokenStream
             = analyzer.tokenStream("contents", new StringReader(line))) {
      tokenStream.reset();
      CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
      while (tokenStream.incrementToken()) {
        emit(term.toString());
      }
    }
  }


  private void emit(String text) {
    try {
      outputStream.write(text.getBytes(StandardCharsets.UTF_8));
      outputStream.write((byte)0);
    }
    catch (IOException e) {
      throw new RuntimeException("Error writing bytes.");
    }
  }


  private void ProcessDocumentFooter() {
    String line = GetLine();

    // TODO: REVIEW: Is this check really necessary?
    if (!IsDocumentEnd(line)) {
      throw new RuntimeException("Expected </doc>.");
    }
  }


  private boolean IsDocumentEnd(String line) {
    return line.startsWith("</doc>");
  }


  private String PeekLine() {
    if (line == null) {
      line = scanner.nextLine();
    }
    return line;
  }


  private String GetLine() {
    PeekLine();             // Force line to be initialized.
    String result = line;
    line = null;
    return result;
  }


  private class FileScope implements java.lang.AutoCloseable {
    public FileScope() {
    }

    @Override
    public void close() throws Exception {
      // Write trailing '\0'
      emit("");
    }
  }


  private class DocumentScope implements java.lang.AutoCloseable {
    public DocumentScope() {
    }

    @Override
    public void close() throws Exception {
      // Write trailing '\0'
      emit("");
    }
  }


  private class StreamScope implements java.lang.AutoCloseable {
    public StreamScope(int streamId) {
      emit(String.format("%02x", streamId));
    }

    @Override
    public void close() throws Exception {
      // Write trailing '\0'
      emit("");
    }
  }
}
