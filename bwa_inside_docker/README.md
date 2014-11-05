This layers BWA on top of a base SeqWare docker image

Assumes that seqware inside docker has already been built. 

---------------------------------------------------------------

1. Set permissions on datastore which will hold results of workflows after they run

    chmod a+w datastore

2. Assuming docker is installed properly, build image with 

    sudo docker build  -t seqware_1.1.0-alpha.5_bwa .

3. Run container and login with the following (while persisting workflow run directories to datastore)
 
    sudo docker run --privileged -h master -t -v `pwd`/datastore:/datastore  -i seqware_1.1.0-alpha.5_bwa

4. Run workflow with 

    seqware bundle launch --dir ~/provisioned-bundles/Workflow_Bundle_BWA_2.6.1_SeqWare_1.1.0-alpha.2/ --no-metadata
