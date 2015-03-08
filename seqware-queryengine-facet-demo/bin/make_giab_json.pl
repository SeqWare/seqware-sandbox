use strict;

my ($loc) = @ARGV;

foreach my $i in (`ls $loc | grep -v READ | grep -v report`) {
  print "$i";
}

