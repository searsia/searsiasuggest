#!/usr/bin/perl -w

$prev = "";
$prev_orig = "";
$count = 0;

sub line_out {
  my $prev = shift;
  my $prev_orig = shift;
  my $count = shift;
  if ($prev ne "") {
    $length = () = ($prev =~ / /g);
    if ($prev_orig =~ /[^A-Za-z]/) {
      $length += 1;
    }
    if ($prev =~ /[a-z0-9]{16,}/i) {
      $score /= 2;
    }
    $score = $count / ($length + 1);
    if ($count > 1 and $score > 0.7) {
      printf "%1.1f\t%s\n", $score, $prev_orig;
    }
  }
}

while(<STDIN>) {
  chop;
  $this = $_;
  $this_orig = $_;
  $this =~ tr/A-Z/a-z/;
  $this =~ s/[^a-z0-9 ]//g;
  if ($this ne $prev) {
    line_out($prev, $prev_orig, $count);
    $prev = $this;
    $prev_orig = $this_orig;
    $count = 0;
  } else {
    $count++; 
  }
}

line_out($prev, $prev_orig, $count);

