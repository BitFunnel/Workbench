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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
Make the Maven script use Shade to packup up dependencies.
Document IntelliJ/Maven setup.
  Maven
    Download zip file
    Unzip to ...
    Add bin dir to path
    Set JAVA_PATH

Microsoft Windows [Version 6.1.7601]
Copyright (c) 2009 Microsoft Corporation.  All rights reserved.

C:\Users\Mike>where mvn
C:\Program Files\apache-maven-3.3.9\bin\mvn
C:\Program Files\apache-maven-3.3.9\bin\mvn.cmd

C:\Users\Mike>mvn --version

Error: JAVA_HOME not found in your environment.
Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.


C:\Users\Mike>set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_92

C:\Users\Mike>mvn --version
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T08:41:47-08:00)
Maven home: C:\Program Files\apache-maven-3.3.9\bin\..
Java version: 1.8.0_92, vendor: Oracle Corporation
Java home: C:\Program Files\Java\jdk1.8.0_92\jre
Default locale: en_US, platform encoding: Cp1252
OS name: "windows 7", version: "6.1", arch: "amd64", family: "dos"


x Is DocumentFile a good name? Seems given DocumentFileProcessor.
x   What is name of input file type? WikipediaDump?
x   What is name of output file type? BitFunnelCorpus?
x Write out corpus file as UTF8 bytes.
Rename AppTest
Rename iml file - decide whether to commit
Remove references to sample from pom.xml
Figure out artifact id in pom.xml
IntelliJ walkthrough
Test command line build
Fix package name. Should be org.bitfunnel.workbench.
README.md
Release vs debug builds.
Test running on Windows and Linux.
Put code into real repository.
Sample data files.
  Perhaps public domain poems.
  Very short Wikipedia articles.
x Rename App to CorpusConverter.
Test converting large corpus files.
Measure conversion time.
Progress indicator.
x JUnit workbench function to convert and then read back one file.
Corpus statistics class
  Counts of files, documents, streams per document
  Byte size of files.
Java documentation comments.

commons-cli:commons-cli:1.3.1
org.apache.lucene:lucene-analyzers-common:6.0.0
org.apache.lucene:lucene-core:6.0.0
Maven:junit:junit:3.8.1
 */

public class MakeCorpusFile {
  Path input;
  Path output;

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage");
    } else {
      try {
        MakeCorpusFile processor = new MakeCorpusFile(Paths.get(args[0]), Paths.get(args[1]));
        processor.ProcessAllFiles();
      } catch (IOException e) {
        System.out.println("IOException: " + e);
      }
    }
  }


  public MakeCorpusFile(Path input, Path output) {
    this.input = input;
    this.output = output;
  }


  public void ProcessAllFiles() throws IOException {
    if (!Files.exists(input)) {
      System.out.println("Error: input location " + input.getFileName() + " does not exist.");
    } else if (Files.exists(output)) {
      System.out.println("Error: output location " + output.getFileName() + " already exists.");
    } else {
      System.out.println("Making corpus file.");
      System.out.println("Input (Wikipedia dump): " + input.toAbsolutePath());
      System.out.println("Output (corpus file: " + output.toAbsolutePath());

      Files.walk(input).filter(Files::isRegularFile).forEach(this::ProcessOneFile);
    }
  }


  public void ProcessOneFile(Path path) {
    try {
      // Determine the path to the destination file.
      Path relativePath = input.relativize(path);
      Path destination = output.resolve(relativePath);

      System.out.println("source = " + path);

      System.out.println("createDirectories(" + destination.getParent() + ")");
      Files.createDirectories(destination.getParent());

      System.out.println("Open file " + destination + " inside of try.");

      try (InputStream inputStream = Files.newInputStream(path);
           OutputStream outputStream = Files.newOutputStream(destination)) {
        WikipediaDumpProcessor processor =
            new WikipediaDumpProcessor(inputStream, outputStream);
        processor.ProcessFile();
      }

    } catch (Exception e) {
      System.out.println("EXCEPTION");
      e.printStackTrace();
    }
  }
}
