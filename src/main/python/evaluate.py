
import re
import requests
import sys

URL = 'http://localhost:8088/suggestions/autocomplete'

def printf(format, *args):
    sys.stdout.write(format % args)


def normalize_query(query):
    query = query.lower()
    query = re.sub(r"[^0-9a-z ]", " ", query)
    query = re.sub(r" +", " ", query)
    return re.sub(r"^ | $", "", query)

def reciprocal_rank(query, json): 
    relevant = normalize_query(query)
    nr = 1
    #print relevant, " = ",
    for hit in json['hits']:
        title = normalize_query(hit['title'])
        #print title, ", "
        if (title == relevant):
            return 1.0 / nr;
        nr += 1
    return 0;


def returned_of_100(json):
    return len(json['hits']) / 100.0


if (len(sys.argv) != 2):
    sys.stderr.write ("Usage: python evaluate.py testdata.txt\n")
    sys.exit(1)

with open(sys.argv[1]) as testdata:
    recip_rank_total  = [0.0, 0.0, 0.0, 0.0, 0.0]
    returned100_total = [0.0, 0.0, 0.0, 0.0, 0.0] 
    query_total = 0;
    for line in testdata:
        query_total += 1
        (freq, query) = line.rstrip().split("\t")
        print query_total, query, ": ",
        query = re.sub(r"^[^0-9A-Za-z]", "", query) # start with letter
        for length in range(1, 6):
            prefix = query[:length]   
            param = { 'q': prefix }
            response = requests.get(URL, params=param)
            rr = reciprocal_rank(query, response.json())
            recip_rank_total[length - 1] += rr
            returned100_total[length - 1] += returned_of_100(response.json())
            print rr,
        print


print
print "Mean Reciprocal Rank for", query_total, "queries:"
print "Prefix  MRR       Returned of 100"
for length in range(1, 6):
    printf("   %d    %1.4f    %1.4f\n", length, recip_rank_total[length - 1] / query_total, returned100_total[length - 1] / query_total)
