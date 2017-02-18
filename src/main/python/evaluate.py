
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
        if (nr > 10): 
            print "Warning: More than 10 results"
        nr += 1
    return 0;


def returned_of_10(json):
    return len(json['hits']) / 10.0


if (len(sys.argv) != 2):
    sys.stderr.write ("Usage: python evaluate.py testdata.txt\n")
    sys.exit(1)

with open(sys.argv[1]) as testdata:
    recip_rank_total_char = [0.0, 0.0, 0.0, 0.0, 0.0]
    returned10_total_char = [0.0, 0.0, 0.0, 0.0, 0.0]
    recip_rank_total_word = [0.0, 0.0, 0.0, 0.0, 0.0]
    returned10_total_word = [0.0, 0.0, 0.0, 0.0, 0.0]
    query_total = 0;
    for line in testdata:
        query_total += 1
        (freq, query) = line.rstrip().split("\t")
        print query_total, query, ": ",
        query = re.sub(r"^[^0-9A-Za-z]", "", query) # start with letter
        old_prefix = ""
        old_rr = 0.0
        for length in range(1, 6):
            prefix = query[:length]   
            if (prefix != old_prefix):
                param = { 'q': prefix }
                response = requests.get(URL, params=param)
                rr = reciprocal_rank(query, response.json())
                old_prefix = prefix
                old_rr = rr 
            recip_rank_total_char[length - 1] += rr
            returned10_total_char[length - 1] += returned_of_10(response.json())
            print rr,
        old_position = 1
        old_prefix = ""
        old_rr = 0.0
        for length in range(1, 6):
            try:
                position = query[old_position:].index(" ") + old_position + 1
                prefix = query[:position]
            except:
                position = len(query)
                prefix = query
            if (prefix != old_prefix):
                param = { 'q': prefix }
                response = requests.get(URL, params=param)
                rr = reciprocal_rank(query, response.json())
                old_prefix = prefix
                old_rr = rr
            recip_rank_total_word[length - 1] += rr
            returned10_total_word[length - 1] += returned_of_10(response.json())
            print rr,
            old_position = position
        print
            

print
print "Mean Reciprocal Rank for", query_total, "queries:"
print "Prefix    MRR       Returned of 10"
for length in range(1, 6):
    printf("%d char    %1.4f    %1.4f\n", length, recip_rank_total_char[length - 1] / query_total, returned10_total_char[length - 1] / query_total)
for length in range(1, 6):
    printf("%d word    %1.4f    %1.4f\n", length, recip_rank_total_word[length - 1] / query_total, returned10_total_word[length - 1] / query_total)

