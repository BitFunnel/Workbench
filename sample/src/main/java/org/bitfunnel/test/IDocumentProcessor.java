package org.bitfunnel.test;

/**
 * Created by michaelhopcroft on 5/14/16.
 */
public interface IDocumentProcessor {
  public void OpenDocumentSet();
  public void OpenDocument();
  public void OpenStream(String name);
  public void Term(String term);
  public void CloseStream();
  public void CloseDocument();
  public void CloseDocumentSet();
}
