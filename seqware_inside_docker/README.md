This is the base Dockerfile for seqware inside docker. 

1. Set permissions on datastore which will hold results of workflows after they run

        chmod a+w datastore

2. Assuming docker is installed properly, build image with 

        sudo docker build  -t seqware_1.1.0-alpha.6 .

3. Run container and login with the following (while persisting workflow run directories to datastore)
 
        sudo docker run --rm -h master -t -v `pwd`/datastore:/datastore  -i seqware_1.1.0-alpha.6

4. Run workflow with 

        seqware bundle launch --dir ~/provisioned-bundles/Workflow_Bundle_HelloWorld_1.0-SNAPSHOT_SeqWare_1.1.0-alpha.6/ --no-metadata
        
5. Exit the container and save the image

        exit
        sudo docker save -o seqware_1.1.0-alpha.6.tar seqware_1.1.0-alpha.6
