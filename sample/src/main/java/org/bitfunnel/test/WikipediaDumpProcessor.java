package org.bitfunnel.test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class WikipediaDumpProcessor {
  InputStream inputStream;
  OutputStream outputStream;
  Scanner scanner;
  String line;

  static Pattern pattern =
      Pattern.compile("<doc id=\"([^\"]*)\" url=\"[^\"]*\" title=\"([^\"]*)\">");

  static Analyzer analyzer = new StandardAnalyzer();


  public WikipediaDumpProcessor(InputStream inputStream,
                                OutputStream outputStream) {
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }


  public void ProcessFile() throws IOException, Exception {
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

    String id = matcher.group(1);
    String title = matcher.group(2);

    try (StreamScope scope = new StreamScope("title")) {
      emit(title);
    }

    // TODO: Should id be an integer in the file?
    try (StreamScope scope = new StreamScope("id")) {
      emit(id);
    }
  }


  private void ProcessAllContentLines() throws Exception {
    try (StreamScope scope = new StreamScope("content")) {
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
    System.out.println("\"" + text + "\\0\"");
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
    public StreamScope(String name) {
      emit(name);
    }

    @Override
    public void close() throws Exception {
      // Write trailing '\0'
      emit("");
    }
  }
}
