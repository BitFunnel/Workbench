package org.bitfunnel.workbench;

import java.io.IOException;
import java.io.StringReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;  // Demo
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute; // Demo
import org.bitfunnel.test.DocumentProcessor;

/**
 * Hello world!
 *
 */
public class App
{
    Path input;
    Path output;
//    Path base;

    public static void main3(String[] args)
    {
        DocumentProcessor.testRE("<doc id=\"12\" url=\"https://en.wikipedia.org/wiki?curid=12\" title=\"Anarchism\">");
    }

    public static void main(String[] args)
    {
        if (args.length != 2) {
            System.out.println("Usage");
        }
        else {
            try {
                App x = new App(Paths.get(args[0]), Paths.get(args[1]));
                x.ProcessAllFiles();
                //            Files.walk(Paths.get(path)).filter(Files::isRegularFile).forEach(x::ProcessOneFile);
            } catch (IOException e) {
                System.out.println("IOException: " + e);
            }
        }
    }

    public App(Path input, Path output)
    {
        this.input = input;
        this.output = output;
//        this.base = input.toAbsolutePath().getParent();
    }

    public void ProcessAllFiles() throws IOException
    {
        if (!Files.exists(input))
        {
            System.out.println("Error: input location " + input.getFileName() + " does not exist.");
        }
        else if (Files.exists(output))
        {
            System.out.println("Error: output location " + output.getFileName() + " already exists.");
        }
        else
        {
            Files.walk(input).filter(Files::isRegularFile).forEach(this::ProcessOneFile);
        }
    }


    public void ProcessOneFile(Path path)
    {
        try {
            // Determine the path to the destination file.
            Path relativePath = input.relativize(path);
            Path destination = output.resolve(relativePath);

            // Create directories on the path to the destination file.
//            System.out.println("createDirectories(" + destination.getParent() + ")");

            // Create destination file.
//            System.out.println("Create file " + destination);

            try {
                DocumentProcessor processor = new DocumentProcessor(path, destination);
                processor.Process();
            }
            catch (Exception e) {
                System.out.println("EXCEPTION");
                e.printStackTrace();
            }

            System.out.println();
        }
        catch (RuntimeException e)
        {
            System.out.println("xxx");
        }

        /*
Document: (Head\n(Line\n)*Tail)

         */

//        try {
//            Scanner scanner = new Scanner(path);
//            int lines = 0;
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                ++lines;
//            }
//            System.out.println(path.getFileName() + ": " + lines);
//        }
//        catch (IOException e)
//        {
//            System.out.println("IOException reading " + path.getFileName());
//        }
    }
    //////////////////////////////////////////////////////
    // Junk Below
    //////////////////////////////////////////////////////

    public static void main2( String[] args )
    {
//        process();
        System.out.println( "Hello World!" );
        try {
            process2(".");
//            displayTokenUsingStandardAnalyzer();
//            readText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void indent(int depth)
    {
        System.out.print(String.format("%" + (depth * 2 + 1) + "s", ""));
    }


    public static class AnotherProcessor implements Processor {
        public void process(File file, int depth) {
            indent(depth);
            System.out.println("== " + file.getName());
        }
    }

    public static void process()
    {
        File root = new File("dirs");
//        processDirectory(root, 0, (file, depth) -> { indent(depth); System.out.println(file.getName());});
        processDirectory(root, 0, new AnotherProcessor());
    }

    public interface Processor {
        public void process(File file, int depth);
    }

    public static void yetAnother(Path path)
    {
        try {
            Scanner scanner = new Scanner(path);
            int lines = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                ++lines;
            }
            System.out.println(path.getFileName() + ": " + lines);
        }
        catch (IOException e)
        {
            System.out.println("IOException reading " + path.getFileName());
        }
    }

    public static void process2(String path) throws java.io.IOException
    {
//        Files.walk(Paths.get(path)).filter(Files::isRegularFile).forEach(System.out::println);
        Files.walk(Paths.get(path)).filter(Files::isRegularFile).forEach(App::yetAnother);
    }

    public static void processDirectory(File root, int depth, Processor processor)
    {
        processor.process(root, depth);
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                for (File child : files) {
                    processDirectory(child, depth + 1, processor);
                }
            }
        }
    }


    public static void readText() throws FileNotFoundException {
        Scanner scan = new Scanner(new File("wiki_00"));
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            //Here you can manipulate the string the way you want
            System.out.println(line);
        }
    }

    // http://www.tutorialspoint.com/lucene/lucene_standardanalyzer.htm
    private static void displayTokenUsingStandardAnalyzer() throws IOException{
        String text
                = "xxx hello ѐѐѐ Lucene ? \" the dog is running simple yet powerful java based search library.";
//        Analyzer analyzer = new EnglishAnalyzer();
        Analyzer analyzer = new StandardAnalyzer();
        TokenStream tokenStream
                = analyzer.tokenStream("contents",
                new StringReader(text));
        tokenStream.reset();
        CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
        while(tokenStream.incrementToken()) {
            System.out.print("[" + term + "] ");
            byte[] s = term.toString().getBytes("UTF-8");
            s.toString();
        }
    }
}
