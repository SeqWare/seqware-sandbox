use strict;
use JSON;
use Data::Dumper;

my ($patient) = @ARGV;

my $patient_data = `cat $patient`;
my $pd = decode_json $patient_data;

#print Dumper($pd);

my $i=100;

my @pat_arr;
foreach my $pat (keys %{$pd}) {

  open VCFOUT, ">$pat.vcf" or die;

  my $curr_i=0;
  my $gender = $pd->{$pat}{"gender"};
  my $age = $pd->{$pat}{"age_at_diagnosis"};
  my $race = $pd->{$pat}{"race"};
  my $diagnosis_code = $pd->{$pat}{"diagnosis_code"};
  my $diagnosis = $pd->{$pat}{"diagnosis"};
  my $location_of_crohns = $pd->{$pat}{"location_of_Crohns"};
  my $ibd_surgery = $pd->{$pat}{"IBD_surgery"};
  my $ibd_family_history = $pd->{$pat}{"IBD_family_history"};
  my $ibd_2nd_degree_relative = $pd->{$pat}{"IBD_2nd_degree_relative"};
  my $jewish_paternal_grandparents = $pd->{$pat}{"jewish_paternal_grandparents"};
  my $jewish_maternal_grandparents = $pd->{$pat}{"jewish_maternal_grandparents"};
  my $smoker = $pd->{$pat}{"smoker"};
  my $drugs = $pd->{$pat}{"drugs"};
  my $aliquot_type = $pd->{$pat}{"aliquot_type"};
  my $inventory_status = $pd->{$pat}{"inventory_status"};
  my $vcf = $pd->{$pat}{"vcf_file"};
  my $max_vars = $pd->{$pat}{"max_variants"};
  my $random_max = $pd->{$pat}{"prob_of_inclusion"};
  #print "MAX: $max_vars Curr $curr_i $vcf\n";
  #print "HERE1\n";
  #my $pstr = qq("feature_set": "$pat", "patient_id": "$pat", "gender": "$gender", "age": "$age", "race": "$race", "diagnosis_code": ).printArr($diagnosis_code).qq(, "smoker": "$smoker", "drugs": ).printArr($drugs).qq(, "biorepo": ).qq($biorepo).qq( });
  my $pstr = qq("feature_set": "$pat", "patient_id": "$pat", "gender": "$gender", "age_at_diagnosis": "$age", "race": "$race", "diagnosis_code": ).printArr($diagnosis_code).qq(, "smoker": "$smoker", )
.qq("diagnosis": ).printArr($diagnosis).qq(, )
.qq("location_of_crohns": ).printArr($location_of_crohns).qq(, )
.qq("ibd_surgery": "$ibd_surgery", )
.qq("ibd_family_history": ).printArr($ibd_family_history).qq(, )
.qq("ibd_2nd_degree_relative": "$ibd_2nd_degree_relative", )
.qq("jewish_paternal_grandparents": "$jewish_paternal_grandparents", )
.qq("jewish_maternal_grandparents": "$jewish_maternal_grandparents", )
.qq("inventory_status": ).printArr($inventory_status).qq(, )
.qq("drugs": ).printArr($drugs).qq(, "aliquot_type": ).printArr($aliquot_type).qq(, "properties" : { "age" : {"type" : "string" } } } );
  #my $pstr ="";
  #print "HERE2\n";
  open IN, "<$vcf" or die "DIE: can't open file";
  #print "HERE\n";
  while(<IN>) {
    if (/^#/) {
      print VCFOUT $_;
    } else {
      my $random = rand();
      next if ($random > $random_max);
      $curr_i++;
      $i++;
      print VCFOUT $_;
      chomp;
      print (print_vcf_line($i, $pat, $_, $pstr)."\n");
      last if ($curr_i>$max_vars);
    }
  }
  close IN;
  close VCFOUT;
  # {"id":"30", "title": "chrX:123-123:A->ACCA", "location": {"start":123, "stop":123, "chr":"chrX"}, "databases": ["omim","dbsnp"], "consequences": ["frameshift","coding"], "feature_set": "DO32839292", "variant_type": "Indel", "patient_id": 1002, "gender": "female", "age": 45, "race": "black", "diagnosis_code": ["555.1", "555.2"], "smoker": "former", "drugs": ["Azathioprine", "Infliximab"], "biorepo": ["small intestine FFPE"] }

 #  {"id":"133", "title": "chr1:69511 A -> G", "location": {"start":69511, "stop":69511, "chr":"1"}, "feature_set": "Patient 1005", "variant_type": "SNP", "databases": [dbsnp,sift], "consequences": [nonsynonymous,coding],
}

sub print_vcf_line {
  my ($i, $patientId, $vln, $pln) = @_;
  $vln =~ /^(\S+)\s(\d+)\s(\S+)\s(\S+)\s(\S+)/;
  my $ln = qq({"index":{"_index":"queryengine","_type":"features","_id":"$i"}}
{"id":"$i", "title": "chr$1:$2 $4 -> $5", "location": {"start":$2, "stop":$2, "chr":"$1"}, );

  my @dbs;
  if ($vln =~ /isDbSNP/) { push @dbs, "dbsnp"; }
  if ($vln =~ /hasSift/) { push @dbs, "sift"; }
  if ($vln =~ /inOmimGene/) { push @dbs, "omim"; }

  my $variant_type = 'SNP';
  if ($vln =~ /IndelType/) { $variant_type = "Indel"; }

  my @cons;
  if ($vln =~ /nonsynonymous/) { push @cons, "nonsynonymous"; }
  elsif ($vln =~ /synonymous/) { push @cons, "synonymous"; }
  if ($vln =~ /coding/) { push @cons, "coding"; }
  if ($vln =~ /nonframeshift/) { push @cons, "nonframeshift"; }
  elsif ($vln =~ /frameshift/) { push @cons, "frameshift"; }

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

  return($ln.$pln);
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
