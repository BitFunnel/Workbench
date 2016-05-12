package org.bitfunnel.workbench;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.TokenStream;  // Demo
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute; // Demo
//import org.apache.lucene.analysis.tokenattributes.BytesTermAttribute; // Demo

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        try {
            displayTokenUsingStandardAnalyzer();
        } catch (IOException e) {
            e.printStackTrace();
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
