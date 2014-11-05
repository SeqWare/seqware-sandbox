This is the base Dockerfile for seqware inside docker. 

1. Assuming docker is installed properly, build image with 

        sudo docker build  -t seqware/seqware_1.1.0-alpha.5 .

2. Run container and login with the following (while persisting workflow run directories to datastore)

        sudo docker run --privileged -h master -t -i seqware/seqware_1.1.0-alpha.5 -v datastore:/datastore /bin/bash

3. Run workflow with 

        seqware bundle launch --dir <hello world> --no-metadata

