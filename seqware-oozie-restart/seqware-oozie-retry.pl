use strict;

my ($local_dir, $jobid, $swid) = @ARGV;

my $text = `oozie job -oozie http://master:11000/oozie -info $jobid | grep SUCCESS | awk '{print \$1}' | awk -F '\@' '{print \$2}'`;

my @lines = split /\n/, $text;
my $first = 1;
run("cat $local_dir/job.properties | grep -v oozie.wf.rerun.skip.nodes > $local_dir/job.properties.2; cp $local_dir/job.properties.2 $local_dir/job.properties");
open OUT, ">>$local_dir/job.properties" or die;
print OUT "\noozie.wf.rerun.skip.nodes=";
foreach my $line(@lines) {
  chomp $line;
  if (!$first) { print OUT ","; }
  $first = 0;
  print OUT $line;
}
print OUT "\n";
close OUT;
$local_dir =~ /(oozie-\S+)/;
my $hdfspath = "seqware_workflow/$1";
run("hadoop fs -rm $hdfspath/job.properties");
run("hadoop fs -put $local_dir/job.properties $hdfspath/job.properties");
run("oozie job -oozie http://master:11000/oozie -config $local_dir/job.properties -rerun $jobid");
sleep 5;
# this sometimes doesn't work if the job.properties ends up having both retry variables:
# E0404: Only one of the properties are allowed [oozie.wf.rerun.skip.nodes OR oozie.wf.rerun.failnodes]
run("seqware workflow-run retry --accession $swid");
sleep 5;
run("oozie job -oozie http://master:11000/oozie -config $local_dir/job.properties -info $jobid");
run("seqware workflow-run report --accession $swid");

#system("cd $local_dir; oozie job -oozie http://master:11000/oozie -info $jobid | grep SUCCESS | awk '{print \$1}' | awk -F '\@' '{print \$2}' | perl -e 'print \"\\noozie.wf.rerun.skip.nodes=\"; my \$first=1; while(<>) { chomp; if(!\$first) { print ','; } print \"\$_\"; \$first=0; }' >> job.properties");

sub run {
  my $cmd = shift;
  print "$cmd\n";
  system($cmd);
}

