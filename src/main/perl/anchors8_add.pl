#!/usr/bin/perl -w

# add these queries (must be lower case)

%important = (
  "searsia", 10.0,
  "search for noobs", 1.0
);


while(<STDIN>) {
  chop; 
  ($score, $query) = split /\t/;
  $query_string = $query;
  $query_string =~ tr/A-Z/a-z/;
  if (defined($important{$query_string})) {
    $score += $important{$query_string};
    delete($important{$query_string});
  }
  printf "%1.1f\t%s\n", $score, $query;
}

foreach $query (keys %important) {
  printf "%1.1f\t%s\n", $important{$query}, $query;
}
