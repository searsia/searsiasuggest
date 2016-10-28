#!/usr/bin/perl -w

use utf8;
use HTML::Entities;

while(<STDIN>) {
  #$_ = decode_entities($_); # this introduces problems later on
  s/\([^\)]+\)//g;  # remove things between () {} [] weg
  s/\[[^\]]+\]//g;
  s/\{[^\}]+\}//g;
  s/[\(\{\[\)\]\}]/ /g;
  s/[0-9A-Za-z]+\.\.\.//;  # beginning or ending with ...
  s/\.\.\.[0-9A-Za-z]*//;
  s/\&amp;/\&/g;
  s/\&quot;/\"/g;
  s/\&\#38;/\&/g;
  s/\&\#39;/\'/g;
  s/\&\#8217;/\'/g;
  s/\&[^\; ]+;/ /g; # wrong entities?
  s/\t/ /g;
  s/[\x00-\x09]//g;
  s/[^\n -ü]/ /g;
  s/^[^A-Za-z0-9]+//;  # trailing nonsense
  s/[^A-Za-z0-9\?]+\n$/\n/; # end with character or question mark
  s/[^A-Za-z0-9]+\?\n/\?\n/;
  s/ +/ /g;
  if (/[a-zA-Z]/) {
    print;
  }
}
