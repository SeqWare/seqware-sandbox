# SeqWare Oozie Restart Script #

I found the retry mechanism built into SeqWare was problematic.  It tended to fail in the sense that restarted workflows would simply restart and then immedialtely fail again.  Denis indicated this, in some cases, was caused by an apparent bug in Oozie.

Regardless, I found that specifying "oozie.wf.rerun.skip.nodes=<list of job IDs to skip>" in the job.properties file and then re-running the Oozie workflow using the Oozie tools was much more effective.  So I developed the following script/process to restart SeqWare workflows:

First, find the workflow accession you're interested in:

    seqware workflow list
    -[ RECORD 0 ]------------+----------------------------------------------------------------------------------------------------------------------------
    Name                     | BWA                                                                                                                         
    Version                  | 2.6.0                                                                                                                       
    Creation Date            | Sat Aug 09 22:09:55 CDT 2014                                                                                                
    SeqWare Accession        | 967 

Next, find all the workflow-runs that failed and then prepare a call to my seqware-oozie-retry.pl script to attempt to fix the problem:

    seqware workflow report --accession 967 | grep -A 4 -B 1 failed | perl -e 'my $swid; my $dir; my $id; while(<>) { if (/Workflow Run SWID\s+\| (\d+)/) { $swid=$1 } if (/Workflow Run Working Dir\s+\| (\S+)/) { $dir=$1 } if (/Workflow Run Engine ID\s+\| (\S+)/) { $id=$1; print "perl seqware-oozie-retry.pl $dir $id $swid\n"; } }'

This will then print how to call the fixer script which will actually do the resubmission:

    seqware-oozie-retry.pl /glusterfs/data/ICGC2/seqware_results/scratch/oozie-4b93f651-9554-4a1f-87e0-b07fda6d0f8f 0000005-140809172848280-oozie-oozi-W 1326

You should run this as the user that originally submitted the workflow.  Also, it has a hardcoded Oozie server name in it, "master", you may need to change depending on your environment.

