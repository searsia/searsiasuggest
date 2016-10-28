#!/usr/bin/perl -w

while(<STDIN>) {
  $prnt = 1;
  if (/\x70\x6F\x72\x6E|\x70\x75\x73\x73\x79|\x66\x75\x63\x6B/i) { $prnt = 0; }

  if ($prnt) {
    print;
  }
}

