This wraps images with docker in docker functionality 

------------------------

1. Assuming docker is installed properly, build image with 

    sudo docker build  -t seqware_1.1.0-alpha.5_dind .

2. Run container and login with the following
 
    sudo docker run --privileged -h master -t -i seqware_1.1.0-alpha.5_dind

3. Explore the possibilities of running docker inside docker

    sudo docker run -t -i ubuntu bash

