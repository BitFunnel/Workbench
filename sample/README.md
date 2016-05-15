# Corpus Tools

The org.bitfunnel.corpus-tools package provides tools for converting Wikipedia 
database dump files into BitFunnel corpus files. We designed BitFunnel corpus
files with the goal of trivial and extremely low overhead parsing.

While the initial conversion from Wikipedia database dumps to BitFunnel corpus 
files may be slow and error-prone, subsequent experiments with BitFunnel corpus files should be fast and reliable. 

The expected workflow is to download a Wikipedia database dump and convert it
once and then use the resulting BitFunnel corpus files many time over.

## Obtaining a the Wikipedia Database Dump

1. Obtain a Wikipedia database dump file.  These files are available at 
[https://dumps.wikimedia.org/](https://dumps.wikimedia.org/).

1. Click on [Database backup dumps](https://dumps.wikimedia.org/backup-index.html) 
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

This data must be preprocessed with [wikiextractor](https://github.com/attardi/wikiextractor)
before converting to BitFunnel corpus format. The wikiextractor program
parses the XML dump file, extracts the title, url, curid, text for each
page, and then strips all of the wiki markup tags from the text.

The output of wikiextractor looks something like

~~~
<doc id="ID" url="https://en.wikipedia.org/wiki?curid=ID" title="TITLE">
  text for this document.
  ... more text ...
</doc>
... more documents ...
~~~

We were able to successfully process [enwiki-20160407-pages-meta-current1.xml-p000000010p000030303.bz2](https://dumps.wikimedia.org/enwiki/20160407/enwiki-20160407-pages-meta-current1.xml-p000000010p000030303.bz2) using wikiextractor [commit 60e40824](https://github.com/attardi/wikiextractor/commit/60e4082440b626465b2df30301ab00c3a04cd79b).

Note that this version of wikiextractor will not run on Windows
because of a bug.

## Generating the BitFunnel Corpus Files
The Java class org.bitfunnel.workbench.MakeCorpusFile will convert the wikiextractor output to BitFunnel corpus format. Type

Command line for OSX and Linux:
~~~
% java -cp target/corpus-converter-1.0-SNAPSHOT.jar \
       org.bitfunnel.workbench.MakeCorpusFile \
       input \
       output
~~~

where _input_ is the name of a directory containing wikiextractor
output and _output_ is the name of a directory to create for the
BitFunnel corpus files.

## Building org.bitfunnel.workbench.

This is a Java package that is built with [Maven](https://maven.apache.org/).
(version 3.3.9).
The unit tests are based on [JUnit](http://junit.org/).

### OSX Configuration and Build
~~~
% brew install maven
% mvn package 
~~~

### IntelliJ Configuration and Build

