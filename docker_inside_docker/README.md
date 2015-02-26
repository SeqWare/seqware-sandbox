This wraps docker in docker with seqware functionality 

------------------------

1. Assuming docker is installed properly, build image with 

        docker build  -t seqware_dind .

2. Run container and login with the following
 
        docker run --rm --privileged -h master -t -i seqware_dind

3. Explore the possibilities of running docker inside docker

        sudo docker run -t -i ubuntu bash

