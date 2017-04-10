Searsia Suggest
===============

Usage: 
+ Get a file `suggestions.txt` with suggested queries (see below)
+ Build with: `mvn package`
+ Run with: `java -jar target/searsiasuggest.jar -f suggestions.txt`
+ Done.

Basic implementation of query autocompletions, related queries, and spelling 
correction. The tool needs a list of query suggestions at startup. Check
out the provided poor-person's approach to [collecting suggestion data][1],
or the rich-person's approach to [collecting lots of suggestion data][2].
See also the provided [example suggestion file][3].

Related queries and spelling correction can be included as any other
external search engine. Autocompletions need some special configuration:
The [searsiaclient][4] will show autocompletions for engines that return a
value for "suggesttemplate". The autocompletions should use the format of
[OpenSearch suggestions][5], which searsiasuggest produces with the
`f=opensearch` parameter. An example value for "suggesttemplate" is:

    http://localhost:8088/suggestions/autocomplete?q={q}&f=opensearch

[1]: src/main/perl/ "Get some data without logging your users"
[2]: src/main/scala/ "Get more data without logging your users"
[3]: src/test/resources/exampleSuggestions.txt "Example data"
[4]: https://github.com/searsia/searsiaclient "Searsia Client"
[5]: http://www.opensearch.org/Specifications/OpenSearch/Extensions/Suggestions/1.0 "OpenSearch Extensions: Suggestions"
