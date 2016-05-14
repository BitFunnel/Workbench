package org.bitfunnel.workbench;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bitfunnel.test.DocumentFileProcessor;


public class App {
  Path input;
  Path output;

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage");
    } else {
      try {
        App x = new App(Paths.get(args[0]), Paths.get(args[1]));
        x.ProcessAllFiles();
      } catch (IOException e) {
        System.out.println("IOException: " + e);
      }
    }
  }

  public App(Path input, Path output) {
    this.input = input;
    this.output = output;
  }


  public void ProcessAllFiles() throws IOException {
    if (!Files.exists(input)) {
      System.out.println("Error: input location " + input.getFileName() + " does not exist.");
    } else if (Files.exists(output)) {
      System.out.println("Error: output location " + output.getFileName() + " already exists.");
    } else {
      Files.walk(input).filter(Files::isRegularFile).forEach(this::ProcessOneFile);
    }
  }


  public void ProcessOneFile(Path path) {
    try {
      // Determine the path to the destination file.
      Path relativePath = input.relativize(path);
      Path destination = output.resolve(relativePath);

      DocumentFileProcessor processor =
          new DocumentFileProcessor(path, destination);
      processor.ProcessFile();

    } catch (Exception e) {
      System.out.println("EXCEPTION");
      e.printStackTrace();
    }
  }
}
