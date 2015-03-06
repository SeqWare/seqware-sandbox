use strict;
use JSON;
use Data::Dumper;
use Getopt::Long;

# cat rmsk.txt | awk '{print $6 "\t" $7 "\t" $8 "\t" $12}' > rmsk.bed

my $trials_json_file;
my $output_file;

my $global_i = 0;
my $global_d = {};

GetOptions ("json-file=s" => \$trials_json_file, "output-json=s" => \$output_file) or die ("Error with args");

my $trial_data = `cat $trials_json_file`;
my $td = decode_json $trial_data;

print Dumper($td);

my @trial_arr;
foreach my $trial (keys %{$td->{data}}) {

  # params
  my $params_file = $td->{data}{$trial}{"params_file"};
  my $params = parse_params($params_file);

  # vcfs
  my $match_file = $td->{data}{$trial}{"vcf_file_match"};
  my $nomatch_file = $td->{data}{$trial}{"vcf_file_nomatch"};

  # annotations
  my $tstr = qq("feature_set": "$trial", "trial_id": "$trial" } );

  # call SNPEff
  my $match_file_annot = $match_file;
  $match_file_annot =~ s/\.vcf/\.annotated\.vcf/;
  my $nomatch_file_annot = $nomatch_file;
  $nomatch_file_annot =~ s/\.vcf/\.annotated\.vcf/;
  callSnpEff($match_file, $match_file_annot);
  callSnpEff($nomatch_file, $nomatch_file_annot);

  # output
  run("rm $output_file");
  generate_json($match_file_annot, 1, $output_file);
  generate_json($nomatch_file_annot, 0, $output_file);

}

print Dumper($global_d);

# {"id":"30", "title": "chrX:123-123:A->ACCA", "location": {"start":123, "stop":123, "chr":"chrX"}, "databases": ["omim","dbsnp"], "consequences": ["frameshift","coding"], "feature_set": "DO32839292", "variant_type": "Indel", "patient_id": 1002, "gender": "female", "age": 45, "race": "black", "diagnosis_code": ["555.1", "555.2"], "smoker": "former", "drugs": ["Azathioprine", "Infliximab"], "biorepo": ["small intestine FFPE"] }

#  {"id":"133", "title": "chr1:69511 A -> G", "location": {"start":69511, "stop":69511, "chr":"1"}, "feature_set": "Patient 1005", "variant_type": "SNP", "databases": [dbsnp,sift], "consequences": [nonsynonymous,coding],


sub generate_json {
  my ($input, $tp, $output) = @_;
  open IN, "<$input" or die "DIE: can't open file $input";
  open VCFOUT, ">>$output" or die "DIE: can't open file $output";
  while(<IN>) {
    my $d = {};
    chomp;
    if (!/^#/) {
      $global_i++;
      my @a = split /\s+/;
      my @annot = split /;/, $a[7];
      foreach my $annot_st (@annot) {
        my @annot_arr = split /=/, $annot_st;
        if ($annot_arr[0] eq 'ANN') {
          my @snp_annot = split /,/, $annot_arr[1];
          foreach my $snp_annot_st (@snp_annot) {
            my @snp_annot_tok_arr = split /\|/, $snp_annot_st;
            #print $snp_annot_tok_arr[1]."\n";
            my @final_tokens = split /\&/, $snp_annot_tok_arr[1];
            foreach my $final_token (@final_tokens) {
              if ($final_token =~ /custom/) {
                $final_token = "repeat_mask_".$snp_annot_tok_arr[6];
              } elsif ($final_token =~ /sequence_feature/) {
                next;
                # TODO: deal with this later
                #$final_token = "sequence_feature_".$snp_annot_tok_arr[5];
              }
              $d->{$final_token} = 1;
              $global_d->{$final_token} = 1;
            }
          }
        }
      }
      print VCFOUT print_vcf_line($global_i, $_, "}", $d, $tp);
    }
  }
  close IN;
  close VCFOUT;
}

sub callSnpEff {
  my ($input, $output) = @_;

  return 0 if (-e $output);

  die "problems with snpeff" if (run("java -Xmx8g -jar tools/snpEff/snpEff.jar -interval data/tracks/rmsk.bed GRCh37.75  $input > $output"));

}

sub run {
  my ($cmd) = @_;
  print "CMD: $cmd\n";
  return(system($cmd));
}

sub generate_json_tmp {
#  open IN, "<$match_file" or die "DIE: can't open file";
#  while(<IN>) {
#    if (/^#/) {
#      print VCFOUT $_;
#    } else {
#      my $random = rand();
#      next if ($random > $random_max);
#      $curr_i++;
#      $i++;
#      print VCFOUT $_;
#      chomp;
#      print (print_vcf_line($i, $pat, $_, $pstr)."\n");
#      last if ($curr_i>$max_vars);
#    }
#  }
#  close IN;
#  close VCFOUT;
#  system("bgzip $pat.vcf; tabix -p vcf $pat.vcf.gz");
}

sub parse_params {

}

sub print_vcf_line {
  my ($i, $vln, $pln, $d, $tp) = @_;
  $vln =~ /^chr(\S+)\s(\d+)\s(\S+)\s(\S+)\s(\S+)/;
  my $ln = qq({"index":{"_index":"queryengine","_type":"features","_id":"$i"}}
{"id":"$i", "title": "chr$1:$2 $4 -> $5", "location": {"start":$2, "stop":$2, "chr":"$1"}, );

  my @dbs;
  if ($vln =~ /CUSTOM.rmsk/) { push @dbs, "RepeatMask"; }
  if ($vln =~ /hasSift/) { push @dbs, "sift"; }
  if ($vln =~ /inOmimGene/) { push @dbs, "omim"; }

  my $variant_type = '';
  if ($vln =~ /TYPE=([^;]+)/) { $variant_type = "$1"; }

  my @cons;
  foreach my $cons_str (keys %{$d}) {
    if ($cons_str !~ /repeat_mask_/) { push @cons, $cons_str; }
  }
  #if ($vln =~ /nonsynonymous/) { push @cons, "nonsynonymous"; }
  #elsif ($vln =~ /synonymous/) { push @cons, "synonymous"; }
  #if ($vln =~ /coding/) { push @cons, "coding"; }
  #if ($vln =~ /nonframeshift/) { push @cons, "nonframeshift"; }
  #elsif ($vln =~ /frameshift/) { push @cons, "frameshift"; }

  my @repeat;
  foreach my $repeat_str (keys %{$d}) {
    if ($repeat_str =~ /repeat_mask_(\S+)/) { push @repeat, $1; }
  }

  # now print
  $ln .= qq("variant_type": "$variant_type", );

  if (scalar(@dbs) == 0) {
    $ln .= qq("databases": [ "none" ], );
  } else {
    $ln .= qq("databases": [ ").join("\", \"", @dbs).qq(" ], );
  }

  if (scalar(@cons) == 0) {
    $ln .= qq("consequences": [ "none" ], );
  } else {
    $ln .= qq("consequences": [ ").join("\", \"", @cons).qq(" ], );
  }

  if (scalar(@repeat) == 0) {
    $ln .= qq("repeat": [ "none" ], );
  } else {
    $ln .= qq("repeat": [ ").join("\", \"", @repeat).qq(" ], );
  }

  if ($tp) {
    $ln .= qq("accuracy": [ "truePositive" ] }\n);
  } else {
    $ln .= qq("accuracy": [ "falsePositive" ] }\n);
  }

  return($ln);
}

sub printArr {
  my ($arr) = @_;
  my $r;
  if (ref($arr) eq 'ARRAY') {
    if (scalar(@{$arr}) > 0 ) {
      $r = qq([ ").join("\", \"", @{$arr}).qq(" ]);
    }
    else {
      $r = qq([ "none" ]);
    }
  } else {
    $r = qq([ "$arr" ]);
  }
  return($r);
}
