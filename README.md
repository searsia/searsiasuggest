Searsia Suggest
===============

Usage: 
+ Get a file `suggestions.txt` with suggested queries (see below)
+ Build with: `mvn package`
+ Run with: `java -jar target/searsiasuggest.jar -f suggestions.txt`
+ Done.

Basic implementation of query autocompletions, related queries, and spelling 
correction. The tool needs a list of query suggestions at startup. Check
out the provided poor-person's approach to [collecting suggestion data][1].
See also the provided [example suggestion file][2].

[1]: src/main/perl/ "Get some data without logging your users"
[2]: src/test/resources/exampleSuggestions.txt "Example data"

