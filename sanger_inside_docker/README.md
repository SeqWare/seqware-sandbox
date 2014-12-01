This layers Sanger on top of a base SeqWare docker image

Assumes that seqware inside docker has already been built. 

---------------------------------------------------------------

1. Set permissions on datastore which will hold results of workflows after they run

    chmod a+w datastore

2. Run the tabix server as a named container if you have not already (see tabix\_inside\_docker) 

3. Assuming docker is installed properly, build image with 

    sudo docker build  -t seqware_1.1.0-alpha.5_sanger .

4. Run container and login with the following (while persisting workflow run directories to datastore, and opening a secure link to the tabix server)
 
    sudo docker run --rm -h master -t --link pancancer_tabix_server:pancancer_tabix_server -v `pwd`/datastore:/datastore  -i seqware_1.1.0-alpha.5_sanger

5. Run workflow with 

    seqware bundle launch --dir ~/provisioned-bundles/Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.0_SeqWare_1.1.0-alpha.5/ --no-metadata --ini workflow.ini
