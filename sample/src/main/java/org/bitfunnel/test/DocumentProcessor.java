package org.bitfunnel.test;

//import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

//import java.io.FileOutputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.OutputStream;
//import java.nio.file.Files;
import java.io.StringReader;
import java.util.Scanner;
import java.nio.file.Path;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DocumentProcessor {
    Path input;
    Path output;

    Scanner scanner;
    OutputStream outputStream;

    String line;

    static Pattern pattern =
            Pattern.compile("<doc id=\"([^\"]*)\" url=\"[^\"]*\" title=\"([^\"]*)\">");
//    <doc id="12" url="https://en.wikipedia.org/wiki?curid=12" title="Anarchism">

    static Analyzer analyzer = new StandardAnalyzer();



    public static void testRE(String s)
    {
        System.out.println(s);

//        Pattern pattern = Pattern.compile("(.*)");

        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            System.out.println(matcher.group(0));
            System.out.println(matcher.group(1));
            System.out.println(matcher.group(2));
        }

        System.out.println();
    }

    public DocumentProcessor(Path input, Path output)
    {
        this.input = input;
        this.output = output;
    }


    public void Process() throws IOException, Exception
    {
 //       System.out.println("createDirectories(" + output.getParent() + ")");
 //       Files.createDirectories(output.getParent());

        System.out.println("Open file " + output + " inside of try.");
//        try (OutputStream outputStream = Files.newOutputStream(output)) {
            this.outputStream = outputStream;

            scanner = new Scanner(input);
            ProcessAllDocuments();
//        }
    }


    public void ProcessAllDocuments() throws Exception
    {
        try (FileScope scope = new FileScope()) {
            while (scanner.hasNextLine()) {
                ProcessOneDocument();
            }
        }
    }


    public void ProcessOneDocument() throws Exception
    {
        try(DocumentScope scope = new DocumentScope()) {
            ProcessDocumentHeader();
            ProcessAllContentLines();
            ProcessDocumentFooter();
        }
    }


    public void ProcessDocumentHeader() throws Exception
    {
        String line = GetLine();
//        System.out.println("      HEADER: " + line);

        Matcher matcher = pattern.matcher(line);
        if (!matcher.find()) {
            throw new RuntimeException("Malformed document header.");
        }

        String id = matcher.group(1);
        String title = matcher.group(2);

        // Extract URL and write
        // Extract title, analyzed and write.
        try(StreamScope scope = new StreamScope("title"))
        {
            emit(title);
        }

        try(StreamScope scope = new StreamScope("id"))
        {
            emit(id);
        }
    }


    public void ProcessAllContentLines() throws Exception
    {
        try(StreamScope scope = new StreamScope("content"))
        {
            while (true)
            {
                String line = PeekLine();
                if (!IsDocumentEnd(line))
                {
                    ProcessOneContentLine();
                }
                else
                {
                    break;
                }
            }
        }
    }


    public void ProcessOneContentLine() throws IOException
    {
        String line = GetLine();
//        System.out.println("        LINE: " + line);
//        emit(line);
        // TODO: Do Lucene stuff here.

//        Analyzer analyzer = new StandardAnalyzer();
        try (TokenStream tokenStream
                = analyzer.tokenStream("contents", new StringReader(line))) {
            tokenStream.reset();
            CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()) {
                emit(term.toString());
//            System.out.print("[" + term + "] ");
//            byte[] s = term.toString().getBytes("UTF-8");
//            s.toString();
            }
        }
    }


    private void emit(String text)
    {
        System.out.println("\"" + text + "\\0\"");
    }


    public void ProcessDocumentFooter()
    {
        String line = GetLine();
//        System.out.println("      FOOTER: " + line);
        // Optionally verify line and throw.
    }


    public boolean IsDocumentEnd(String line)
    {
        return line.startsWith("</doc>");
        // TODO: Implement
    }


    public String PeekLine()
    {
        if (line == null)
        {
            line = scanner.nextLine();
        }
        return line;
    }


    public String GetLine()
    {
        PeekLine();             // Force line to be initialized.
        String result = line;
        line = null;
        return result;
    }


    public class FileScope implements java.lang.AutoCloseable
    {
        public FileScope()
        {
            System.out.println("  Enter file scope");
        }

        @Override
        public void close() throws Exception {
            // Write trailing '\0'
            emit("");
            System.out.println("  Exit file scope");
        }
    }


    public class DocumentScope implements java.lang.AutoCloseable
    {
        public DocumentScope()
        {
            System.out.println("    Enter document scope");
        }

        @Override
        public void close() throws Exception {
            // Write trailing '\0'
            emit("");
            System.out.println("    Exit document scope");
        }
    }


    private class StreamScope implements java.lang.AutoCloseable
    {
//        OutputStream outputStream;

        public StreamScope(String name)
        {
//            this.outputStream = outputStream;
            // Convert name to UTF-8
            // Write name and trailing '\0'
            System.out.println("      Open stream " + name);
            emit(name);
        }

        @Override
        public void close() throws Exception {
            // Write trailing '\0'
            emit("");
            System.out.println("      Close stream");
        }
    }
}
