package org.bitfunnel.workbench;

/**
 * Created by michaelhopcroft on 5/14/16.
 */
public interface IDocumentProcessor {
  public void openDocumentSet();
  public void openDocument();
  public void openStream(String name);
  public void term(String term);
  public void closeStream();
  public void closeDocument();
  public void closeDocumentSet();
}
