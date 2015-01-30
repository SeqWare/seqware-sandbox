This is the base Dockerfile for full seqware inside docker. 

## Prerequisites

Install the AWS CLI. Refer to the following guides and remember to setup your AWS credentials. 

* https://aws.amazon.com/cli/ 
* http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html 

## Getting the image

### Building the image

1. Assuming docker is installed properly, build image with 

        sudo docker build  -t seqware_1.1.0-alpha.6_base .

2. Unfortunately, the ansible script needs to be run on an active container with a proper hostname. (This is very far from optimal but is a workaround since the apt package for SGE seems to require a valid hostname)

        sudo docker run --rm -h master -t -v `pwd`/datastore:/mnt/datastore  -i seqware_1.1.0-alpha.6_base
        ansible-playbook docker-seqware-install.yml -c local --extra-vars "seqware_version=1.1.0-alpha.6"
     
3. Save the docker container from the outside (from outside the container)

        sudo docker ps (figure out the container id)
        sudo docker commit <container id> seqware_1.1.0-alpha.6_activated_base
        sudo docker save -o seqware_1.1.0-alpha.6_activated_base

4. Use a second Dockerfile to prime the container as a valid image (and save it for export).           

        cd launcher
        sudo docker run --rm -h master -t -v `pwd`/datastore:/mnt/datastore  -i seqware_1.1.0-alpha.6_full
        

### Downloading and restoring the image

1. Rather than building the image, you can also download and restore it from S3 

        aws s3 cp s3://oicr.docker.images/seqware_1.1.0-alpha.6_full.tar .
        sudo docker load -i seqware_1.1.0-alpha.6_full.tar

## Running the Container

1. Set permissions on datastore which will hold results of workflows after they run

        chmod a+w datastore

2. Run container and login with the following (while persisting workflow run directories to datastore)
 
        sudo docker run --rm -h master -t -v `pwd`/datastore:/mnt/datastore  -i seqware_1.1.0-alpha.6

3. Run the HelloWorld (sample) workflow with 

        seqware bundle launch --dir ~/provisioned-bundles/Workflow_Bundle_HelloWorld_1.0-SNAPSHOT_SeqWare_1.1.0-alpha.6/
        
## Saving the image

1. Exit the container and save the image

        exit
        sudo docker save -o seqware_1.1.0-alpha.6_full.tar seqware_1.1.0-alpha.6_full

2. Upload the image to S3 (given proper credentials)

        aws s3 cp seqware_1.1.0-alpha.6_full.tar s3://oicr.docker.images
