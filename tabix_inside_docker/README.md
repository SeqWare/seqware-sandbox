This is the base Dockerfile for tabix inside docker. 

1. Set permissions on datastore which will hold results of workflows after they run

        chmod a+w datastore

2. Copy or link all tabix data from https://s3.amazonaws.com/pan-cancer-data/workflow-data/SangerPancancerCgpCnIndelSnvStr/tabix\_data/data/unmatched/ into the datastore directory

2. Assuming docker is installed properly, build image with 

        sudo docker build  -t pancancer_tabix_server .

3. Run container and login with the following (while persisting workflow run directories to datastore)
 
        sudo docker run --rm -h master -t -v `pwd`/datastore:/datastore  -i pancancer_tabix_server

5. Exit the container and save the image

        exit
        sudo docker save -o pancancer_tabix_server.tar pancancer_tabix_server
