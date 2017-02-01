
import re
import requests
import sys

URL = 'http://localhost:8088/suggestions/autocomplete'

def normalize_query(query):
    lower = query.lower()
    normal = re.sub(r"[^0-9a-z ]", " ", lower)
    return re.sub(r" +", " ", normal)


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


if (len(sys.argv) != 2):
    sys.stderr.write ("Usage: python evaluate.py testdata.txt\n")
    sys.exit(1)

with open(sys.argv[1]) as testdata:
    rr_total = [0.0, 0.0, 0.0, 0.0, 0.0]
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
            rr_total[length - 1] += rr
            print rr,
        print


print
print "Mean Reciprocal Rank for", query_total, "queries:"
print "Prefix  MRR"
for length in range(1, 6):
    print "  ", length, "  ", rr_total[length - 1] / query_total 
