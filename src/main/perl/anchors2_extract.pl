#!/usr/bin/perl -w

sub clean {
  $text = shift;
  $text =~ s/<!--.*?-->/ /g;
  $text =~ s/<script.*?<\/script>/ /g;
  $text =~ s/<style.*?<\/style>/ /g;
  $text =~ s/<[^>]+>/ /g;
  $text =~ s/: /\n/g;  # split on ': '
  $text =~ s/\? /\?\n/g;
  $text =~ s/ [\-\|]+ /\n/g;
  return $text;
}

while(<STDIN>) {
  if (/<a[^>]*>(.*?)<\/a>/i) {
    $anchor = clean($1);
    print "$anchor\n";
  }
  if (/<title>(.*?)<\/title>/i) {
    $title = clean($1);
    print "$title\n";
  }
}
