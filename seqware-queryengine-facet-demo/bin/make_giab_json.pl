use strict;

my ($loc) = @ARGV;

print qq({
  "settings" : {
    "snpEff_path" : "tools/snpEff/snpEff.jar",
    "snpEff_db" : "GRCh37.75 -interval data/tracks/rmsk.bed"
  },
  "data" : {);

my $cnt = 0;
foreach my $i (`ls $loc | grep -v READ | grep -v report`) {
  chomp $i;
  if ($cnt>0) { print ","; }
  $cnt++; 
  print qq(
    "trial_$i" : {
      "vcf_file_match": "$loc/$i/all/match_All_variants_key.subregions.vcf.gz",
      "vcf_file_nomatch": "$loc/$i/all/noMatch_All_variants_key.subregions.vcf.gz",
      "param_file": "$loc/$i/tvc.params"
    });
}

print "\n}";


