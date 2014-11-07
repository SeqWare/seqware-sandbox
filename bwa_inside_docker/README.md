This layers BWA on top of a base SeqWare docker image

Assumes that seqware inside docker (image named seqware_1.1.0-alpha.5 ) has already been built. 

---------------------------------------------------------------

1. Set permissions on datastore which will hold results of workflows after they run

        chmod a+w datastore

2. Assuming docker is installed properly, build image with 

        sudo docker build  -t seqware_1.1.0-alpha.5_bwa .

3. Run container and login with the following (while persisting workflow run directories to datastore)
 
        sudo docker run --rm -h master -t -v `pwd`/datastore:/datastore  -i seqware_1.1.0-alpha.5_bwa

4. Run workflow with 

        seqware bundle launch --dir ~/provisioned-bundles/Workflow_Bundle_BWA_2.6.1_SeqWare_1.1.0-alpha.2/ --no-metadata
        
5. Exit the container and save the image

        exit
        sudo docker save -o seqware_1.1.0-alpha.5_bwa.tar seqware_1.1.0-alpha.5_bwa

6. Load the image on a new machine 

        sudo docker load -i seqware_1.1.0-alpha.5_bwa.tar
   
Note that to actually run real data, you will need to install a pem file for upload and download to GNOS. 
