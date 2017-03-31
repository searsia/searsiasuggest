Collecting Data for Query Suggestions
=====================================

> Check out the [poor-person's approach][1] if you do not have a Spark cluster.


## 1. Crawl the web

Use [Nutch 1.11][2] with the following command: (Kill the process once you have 
enough pages.)

    bin/crawl urls/ mycrawl/ 200 >logs/nutch.log 2>&1 

To crawl a domain, for instance `searsia.org`, the following settings are
recommended in `conf/regex-urlfilter.txt`:

    # skip file: ftp: and mailto: urls
    -^(file|ftp|mailto):

    # skip image and other suffixes we can't parse
    -\.(deb|ps|avi|xz|rar|doc|dmg|epub|bz2|mp4|gif|GIF|jpg|JPG|png|PNG|ico|ICO|css|CSS|sit
    |SIT|eps|EPS|wmf|WMF|zip|ZIP|ppt|PPT|mpg|MPG|xls|XLS|gz|GZ|rpm|RPM|tgz|TGZ|mov|MOV|exe
    |EXE|jpeg|JPEG|bmp|BMP|js|JS)$

    # skip URLs containing certain characters as probable queries, etc.
    -[?*!@=]

    # skip URLs with slash-delimited segment that repeats 3+ times, to break loops
    -.*(/[^/]+)/[^/]+\1/[^/]+\1/

    # accept anything else from searsia.org
    +^https?://([a-z0-9\-]*\.)*searsia.org


## 2. Create WARC data

WARC stands for [Web ARChive][3]. WARC files are generated as follows:

    for file in mycrawl/segments/*; do \
    bin/nutch commoncrawldump -outputDir MyCrawl -segment $file -warc -gzip; done


## 3. Get anchor texts, scored by number of occurrences

In the searsiasuggest directory, compile the Scala Spark script using `sbt`:

    sbt assembly

And run with:

    spark-submit target/scala-2.10/AnchorExtract-assembly-1.0.jar MyCrawl/* MyAnchors 

or likely something along the lines of (depending on your infrastructure):

    spark-submit --master yarn --deploy-mode cluster --num-executors 100 \
    --conf spark.yarn.executor.memoryOverhead=2048 --class org.searsia.AnchorExtract \
    target/scala-2.10/AnchorExtract-assembly-1.0.jar MyCrawl/* MyAnchors


## 4. Run the suggestions engine

Get your data into one single file called `anchors_count.txt`. Then run with:

    java -jar target/searsiasuggest.jar -f anchors_count.txt


[1]: ../perl/ "Collecting Data for Query Suggestions: a poor-person's approach"
[2]: https://nutch.apache.org/downloads.html "Apache Nuth Downloads"
[3]: https://en.wikipedia.org/wiki/Web_ARChive "Web ARChive - Wikipedia"
