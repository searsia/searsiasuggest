#!/usr/bin/perl -w

while(<STDIN>) {
  s/\s+/ /g;
  s/<\/a>/<\/a>\n/gi;
  print;
}
