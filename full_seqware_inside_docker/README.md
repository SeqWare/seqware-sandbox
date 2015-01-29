This is the base Dockerfile for seqware inside docker. 

## Prerequisites

Install the AWS CLI. Refer to the following guides and remember to setup your AWS credentials. 

* https://aws.amazon.com/cli/ 
* http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html 

## Getting the image

### Building the image

1. Assuming docker is installed properly, build image with 

        sudo docker build  -t seqware_1.1.0-alpha.6 .

### Downloading and restoring the image

1. Rather than building the image, you can also download and restore it from S3 

        aws s3 cp s3://oicr.docker.images/seqware_1.1.0-alpha.6.tar .
        sudo docker load -i seqware_1.1.0-alpha.6.tar

## Running the Container

1. Set permissions on datastore which will hold results of workflows after they run

        chmod a+w datastore

2. Run container and login with the following (while persisting workflow run directories to datastore)
 
        sudo docker run --rm -h master -t -v `pwd`/datastore:/mnt/datastore  -i seqware_1.1.0-alpha.6

3. Run the HelloWorld (sample) workflow with 

        seqware bundle launch --dir ~/provisioned-bundles/Workflow_Bundle_HelloWorld_1.0-SNAPSHOT_SeqWare_1.1.0-alpha.6/ --no-metadata
        
## Saving the image

1. Exit the container and save the image

        exit
        sudo docker save -o seqware_1.1.0-alpha.6.tar seqware_1.1.0-alpha.6

2. Upload the image to S3 (given proper credentials)

        aws s3 cp seqware_1.1.0-alpha.6.tar s3://oicr.docker.images
