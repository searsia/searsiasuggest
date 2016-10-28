#!/usr/bin/perl -w

while(<STDIN>) {
  $prnt = 1;
  if (/https?:\/\//) { $prnt = 0; }
  if (/\@[a-z]+\.[a-z]+/i) { $prnt = 0; }
  if (/[\x00-\x08]/) { $prnt = 0; }
  if (/[\x11-\x1F]/) { $prnt = 0; }
  if (/[^ \t]{26,}/) { $prnt = 0; }

  if ($prnt) {
    s/^(([^ ]+ ){10}).+$/$1/; # at most 10 terms
    s/[^A-Za-z0-9]+\n$/\n/;
    print;
  }
}

