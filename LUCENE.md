## WIP: steps to get parallel Lucene pipeline running (Mac):

Install JDK 1.8, ant 1.8.2+, Ivy 2.2.0:

~~~
brew update
brew cask install java
brew install ant
ant ivy-bootstrap
~~~

Build Lucene:

~~~
ant
~~~

TODO: figure out what property settings we want

    ./lucene.build.properties
    ./build.properties
    ./lucene-x.y/build.properties

To try to run benchmark:
Get wikipedia .bz2 file.
Put it in `lucene-solr/lucene/benchmark/temp/`

The file will need to be renamed to what Lucene expects. Run

~~~
ant expand-enwiki
~~~

to find what the filename should be, then rename and expand.

Note that this keeps the compressed file, so we have a 12GB compressed file and a 50GB uncompressed file in `temp`.

[java] line.file.out = work/enwiki.txt
[java] work.dir = work
[java] -------------------------------
[java] java.lang.Exception: Error: cannot understand algorithm!
[java]     at org.apache.lucene.benchmark.byTask.Benchmark.<init>(Benchmark.java:64)
[java]     at org.apache.lucene.benchmark.byTask.Benchmark.exec(Benchmark.java:110)
[java]     at org.apache.lucene.benchmark.byTask.Benchmark.main(Benchmark.java:85)


Maybe you have to do this first? Hard to say.

# This alg will process the Wikipedia documents feed to produce a
# single file that contains all documents, one per line.
#
# To use this, first cd to benchmark and then run:
#
#   ant run-task -Dtask.alg=conf/extractWikipedia.alg
#
# Then, to index the documents in the line file, see
# indexLineFile.alg.