package org.bitfunnel.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//import org.bitfunnel.test.WikipediaDumpProcessor;

import java.io.*;
import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
import java.util.Arrays;

/**
 * Unit test for simple App.
 */
public class AppTest
    extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public AppTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(AppTest.class);
  }

  /**
   * Rigourous Test :-)
   */
  public void testWikipediaToCorpus() {
    String wikipedia =
        "<doc id=\"123\" url=\"http://www.bitfunnel.org/123\" title=\"one\">\n" +
            "This is the body text.\n" +
            "</doc>\n" +
            "<doc id=\"456\" url=\"http://www.bitfunnel.org/456\" title=\"two\">\n" +
            "Some more body text.\n" +
            "</doc>\n";

    byte[] expected =
        ("title\0one\0\0id\000123\0\0content\0body\0text\0\0\0" +
            "title\0two\0\0id\000456\0\0content\0some\0more\0body\0text\0\0\0" +
            "\0").getBytes(StandardCharsets.UTF_8);

    InputStream input = new ByteArrayInputStream(wikipedia.getBytes(StandardCharsets.UTF_8));
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    WikipediaDumpProcessor processor = new WikipediaDumpProcessor(input, output);
    try {
      processor.ProcessFile();
    }
    catch (Exception e){
      System.out.println("Exception");
      e.printStackTrace();
    }

    byte[] outputBytes = output.toByteArray();

//    for (int i = 0 ; i < outputBytes.length; ++i)   {
//      System.out.println(i + ": " + outputBytes[i] + "  " + expected[i]);
//    }

    assertTrue(Arrays.equals(outputBytes, expected));

    System.out.println("hello");
  }


  public class DocumentProcessor implements IDocumentProcessor
  {
    OutputStream outputStream;

    public DocumentProcessor(OutputStream outputStream) {
      this.outputStream = outputStream;
    }

    @Override
    public void OpenDocumentSet() {
      System.out.println("OpenDocumentSet");
    }

    @Override
    public void OpenDocument() {
      System.out.println("  OpenDocument");
    }

    @Override
    public void OpenStream(String name) {
      System.out.println("    OpenStream(" + name + ")");
    }

    @Override
    public void Term(String term) {
      System.out.println("      Term(" + term + ")");
    }

    @Override
    public void CloseStream() {
      System.out.println("    CloseStream");
    }

    @Override
    public void CloseDocument() {
      System.out.println("  CloseDocument");

    }

    @Override
    public void CloseDocumentSet() {
      System.out.println("CloseDocumentSet");
    }
  }


  public void testCorpusFile() {
    ByteArrayInputStream input =
        new ByteArrayInputStream(
            ("title\0one\0\0id\000123\0\0content\0body\0text\0\0\0" +
             "title\0two\0\0id\000456\0\0content\0some\0more\0body\0text\0\0\0" +
             "\0").getBytes(StandardCharsets.UTF_8));

    //StringWriter output = new StringWriter();
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
    PrintStream output = new PrintStream(outputBytes);

    DocumentProcessor processor = new DocumentProcessor(System.out);

    CorpusFile corpus = new CorpusFile(input);
    corpus.process(processor);

//    for (CorpusFile.Document document: corpus) {
//      for (CorpusFile.Stream stream: document) {
//        System.out.println("name = " + stream.name());
//        for (String s: stream) {
//          System.out.println("XXX: \"" + s + "\"");
//          //output.println(s);
//        }
//      }
//    }

    System.out.println(outputBytes.toString());

    assertTrue(true);
  }
}
