package org.bitfunnel.workbench;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * Unit workbench for simple App.
 */
public class AppTest
    extends TestCase {
  /**
   * Create the workbench case
   *
   * @param testName name of the workbench case
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
   * Test converting Wikipedia dump to corpus file format.
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

      byte[] outputBytes = output.toByteArray();
      assertTrue(Arrays.equals(outputBytes, expected));
    }
    catch (Exception e){
      System.out.println("Exception");
      e.printStackTrace();
      fail();
    }
  }


  /**
   * Test reading then writing corpus file format.
   */
  public void testCorpusFile() {
    byte[] inputBytes =
        ("title\0one\0\0id\000123\0\0content\0body\0text\0\0\0" +
            "title\0two\0\0id\000456\0\0content\0some\0more\0body\0text\0\0\0" +
            "\0").getBytes(StandardCharsets.UTF_8);

    ByteArrayInputStream input = new ByteArrayInputStream(inputBytes);

    ByteArrayOutputStream output = new ByteArrayOutputStream();

    DocumentProcessor processor = new DocumentProcessor(output);

    CorpusFile corpus = new CorpusFile(input);
    corpus.process(processor);

    byte[] outputBytes = output.toByteArray();
    assertTrue(Arrays.equals(outputBytes, inputBytes));
  }


  //
  // Implementation of IDocumentProcessor that emits a string in corpus file
  // format. Used to workbench reading then writing corpus file.
  //
  public class DocumentProcessor implements IDocumentProcessor
  {
    ByteArrayOutputStream outputStream;

    public DocumentProcessor(ByteArrayOutputStream outputStream) {
      this.outputStream = outputStream;
    }

    @Override
    public void openDocumentSet() {
    }

    @Override
    public void openDocument() {
    }

    @Override
    public void openStream(String name) {
      try {
        outputStream.write(name.getBytes(StandardCharsets.UTF_8));
        outputStream.write((byte)0);
      }
      catch (IOException e) {
        e.printStackTrace();
        fail();
      }
    }

    @Override
    public void term(String term) {
      try {
        outputStream.write(term.getBytes(StandardCharsets.UTF_8));
        outputStream.write((byte)0);
      }
      catch (IOException e) {
        e.printStackTrace();
        fail();
      }
    }

    @Override
    public void closeStream() {
      outputStream.write((byte)0);
    }

    @Override
    public void closeDocument() {
      outputStream.write(0);
    }

    @Override
    public void closeDocumentSet() {
      outputStream.write(0);
    }
  }
}
