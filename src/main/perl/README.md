Collecting Data for Query Suggestions
=====================================

> "a poor person's approach"

## 1. Crawl the web

Below, replace `SITE.TLD` (twice) by the site you will crawl. Replace
`NAME` with your name to announce your crawler properly. Only if you are
evil, add `-e robots=off`. Kill the process once you have enough pages.

    wget --timeout=9 --wait=2 --random-wait --level=inf --html-extension \
    --recursive --span-hosts --domains=SITE.TLD --no-clobber --tries=2 \
    --user-agent='NAME' --html-extension --restrict -file-names=windows \
    --reject=jpg,js,css,png,gif,doc,docx,jpeg,pdf,mp3,avi,mpeg,txt,ico \
    http://SITE.TLD

## 2. Get anchor text

    find . -name "*.htm*" -exec cat \{\} \; \
    | ./anchors1_linebreaks.pl | ./anchors2_extract.pl \
    | ./anchors3_replace.pl  | sort -f >anchors.txt

## 3. Score texts (count and normalize score by length)
 
    cat anchors.txt | ./anchors5_count.pl | ./anchors6_clean.pl \
    | ./anchors7_adultfilter.pl | ./anchors8_add.pl | sort -r -n \
    >anchors_count.txt

## 4. Test locally

    grep -i -P '\ts' anchors_count.txt | more

## 5. Run the suggestions engine

    java -jar target/searsiasuggest.jar -f anchors_count.txt

