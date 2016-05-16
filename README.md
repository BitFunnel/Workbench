# Corpus Tools

The **org.bitfunnel.workbench** package provides tools for converting
[Wikipedia](https://www.wikipedia.org/)
database dump files into BitFunnel corpus files. We designed BitFunnel corpus
files with the goal of trivial and extremely low overhead parsing.

The conversion process involves parsing the Wikipedia dump files, extracting
each document, removing wiki markup, performing [Lucene](https://lucene.apache.org/) analysis for
tokenization and stemming, and finally generating encoding and writing
the data in BitFunnel format.

While the initial conversion from Wikipedia database dumps to BitFunnel corpus 
files may be slow, subsequent experiments with BitFunnel corpus files should be fast and reliable.

The expected workflow is to download a Wikipedia database dump and convert it
once and then use the resulting BitFunnel corpus files many time over.

## Obtaining a the Wikipedia Database Dump

1. Obtain a Wikipedia database dump file.  These files are available at 
[https://dumps.wikimedia.org/](https://dumps.wikimedia.org/).

1. Click on [Database backup dumps](https://dumps.wikimedia.org/backup-index.html).
The dumps of the English language Wikipedia pages are under [enwiki]
(https://dumps.wikimedia.org/enwiki/). Each folder here corresponds to
a dump on a particular day.

1. The dump folder is organized into sections. Look for a section entitled,
**Articles, templates, media/file descriptions, and primary meta-pages.**
The links here should be of the form enwikie-DATE-parges-articlesN.xml-pXXXpYYY.bz2
where DATE is of the form YYYYMMDD, N is section number, and XXX and YYY
are number specifiying page ranges.

1. Download one of these files and use [7-Zip](http://www.7-zip.org/) or equivalent to decompress.

## Preprocessing the Wikipedia Database Dump

The wikipedia dump is XML data that looks something like

~~~
<mediawiki xmlns="http://www.mediawiki.org/xml/export-0.10/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.mediawiki.org/xml/export-0.10/ http://www.mediawiki.org/xml/export-0.10.xsd" version="0.10" xml:lang="en">
  <siteinfo>
    <sitename>Wikipedia</sitename>
    <dbname>enwiki</dbname>
    <base>https://en.wikipedia.org/wiki/Main_Page</base>
    <generator>MediaWiki 1.27.0-wmf.19</generator>
    <case>first-letter</case>
    <namespaces>
      <namespace key="-2" case="first-letter">Media</namespace>
      <namespace key="-1" case="first-letter">Special</namespace>
      ... many more namespace entries ...
      <namespace key="2600" case="first-letter">Topic</namespace>
    </namespaces>
  </siteinfo>
  <page>
    <revision>
      <title>TITLE</title>
      ... lots of other tags ...
      <text xml:space="preserve">
        ... the wiki markup for the page ...
      </text>
    </revision>
  </page>
  ... lots more pages ...
</mediawiki>
~~~

This data must be preprocessed with an open source program called
[WikiExtractor](https://github.com/attardi/wikiextractor)
before converting to BitFunnel corpus format. The WikiExtractor program
parses the XML dump file, extracts the title, url, curid, text for each
page, and then strips all of the wiki markup tags from the text.

WikiExtractor requires Python 2.7 (note that Python 3 and beyond are not compatable with version 2.7).
To install Python on the mac,
~~~
brew install python
~~~

To install Python on linux,
~~~
sudo apt install python
~~~

On windows, run the Python 2.7.11 [installer](https://www.python.org/downloads/).

You can run WikiExtractor from the command line as follows:
~~~
./WikiExtractor.py input
~~~
where **input** is an uncompressed Wikipedia database dump file.
If you don't supply the "-o" option the output will be written
to the directory ./text.

Note that some versions of WikiExtractor may fail on Windows because of a
bug related to process spawning.
You can work around this bug by using the "-a" flag, but the extraction will be
slower because it will be limited to a single thread.

The output of wikiextractor looks something like

~~~
<doc id="ID" url="https://en.wikipedia.org/wiki?curid=ID" title="TITLE">
  text for this document.
  ... more text ...
</doc>
... more documents ...
~~~

We were able to successfully process
[enwiki-20160407-pages-meta-current1.xml-p000000010p000030303.bz2](https://dumps.wikimedia.org/enwiki/20160407/enwiki-20160407-pages-meta-current1.xml-p000000010p000030303.bz2) using wikiextractor [commit 60e40824](https://github.com/attardi/wikiextractor/commit/60e4082440b626465b2df30301ab00c3a04cd79b).

Note that this version of WikiExtractor will not run on Windows
without the "-a" flag because of a bug.

## Generating the BitFunnel Corpus Files
The Java class **org.bitfunnel.workbench.MakeCorpusFile** converts
the WikiExtractor output to BitFunnel corpus format.
The converter uses the [Lucene](https://lucene.apache.org/) Standard Analyzer
to tokenize and stem each word in the extracted Wikipedia dump.
Type

Command line for OSX and Linux:
~~~
% java -cp target/corpus-converter-1.0-SNAPSHOT.jar \
       org.bitfunnel.workbench.MakeCorpusFile \
       input \
       output
~~~

where **input** is the name of a directory containing wikiextractor
output and **output** is the name of a directory to create the
BitFunnel corpus files.

## Building org.bitfunnel.workbench.

Java development requires a JDK ((we used jdk1.8.0_92).
Our package is built with [Maven](https://maven.apache.org/).
(version 3.3.9).
The unit tests are based on [JUnit](http://junit.org/).

### OSX Configuration and Build
Install a JDK. We used Oracle's **Java SE 8u92** which can be
found on their [downloads page](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

Use homebrew to install Maven:
~~~
% brew install maven
~~~

Build org.bitfunnel.workbench from the command line:
~~~
% mvn package
~~~

### Windows Configuration and Build

Install Maven.

1. Download the Maven [.zip file](https://maven.apache.org/download.cgi).
1. Extract to some location on the machine.
1. Add the extracted folder's bin directory to the PATH.
  1. Open the System Control panel by pressing (Windows + Pause).
  ![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/system-control-panel.png)
  1. Choose **Advanced System Settings** on the left.
  ![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/advanced-system-settings.png)
  1. Click **Environment Varables** at the bottom of the dialog.
  ![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/environment-variables.png)
  1. Select the variable called **PATH** and press **Edit...**
  ![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/system-control-panel.png)
  1. Add a semicolon (;) to the PATH and then the path to the extracted bin folder.
  ![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/edit-user-variable.png)
  1. **OK** out of all of the dialogs.
  1. Close and reopen any cmd.exe windows to get the new PATH.
  1. Tip. You can update the path in an open cmd.exe window, for example
     ~~~
     set PATH=%PATH%;C:\C:\Program Files\apache-maven-3.3.9\bin
     ~~~

     This change will only have effect in the current window and only until it is closed.

1. In a similar manner, set the JAVA_HOME to point to your JDK. For example,
   ~~~
   set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_92
   ~~~


### Linux Configuration and Build
TBD

### IntelliJ Configuration and Build
[IntelliJ Community Edition](https://www.jetbrains.com/idea/)
is a fairly lightweight, free IDE for Java.
It has a good debugger, support for Ant, Maven, Gradle, and JUnit, and
it provides a number of nice code browsing and refactoring features.
IntelliJ is available on Linux, OSX, and Windows.

Start IntelliJ. From the welcome screen, select **open**:

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/welcome-to-intellij-idea.png)

Select **pom.xml** and press **OK**.

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/intellij-open-file-or-project.png)

The project will be imported. Now set up the debug and run configurations by clicking on **Run => Edit Configurations ...**

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/intellij-edit-configurations.png)

The click the green **+** in the upper left corner to add a new configuration:

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/intellij-run-debug-configurations.png)

Select **Application**

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/intellij-add-new-configuration.png)

On the configuration tab, choose a **Name** for the configuration, set the **Main class** field to
org.bitfunnel.workbench.MakeCorpusFile, and set the **Program arguments** to reference your
input and output directories. **OK** out of all of the dialogs.

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/intellij-configuration-tab.png)

If you plan to edit the pom.xml file, say to add additional dependencies, it helps to configure
auto import. To do this, go to **File => Settings ...**

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/intellij-settings.png)

Expand the tree on the left to **Build, Execution, Deployment/Build Tools/Maven/Importing**. Select
**Import Maven projects automatically.** **OK** out of the dialog.

![alt text](https://github.com/MikeHopcroft/wbtest/blob/master/sample/README/intellij-maven-settings.png)

You are now good to go!
