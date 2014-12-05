This is the base Dockerfile for tabix inside docker. 

## Prerequisites

Install the AWS CLI. Refer to the following guides and remember to setup your AWS credentials. 
* https://aws.amazon.com/cli/ 
* http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html 

## Getting the image

### Building the image

1. Assuming docker is installed properly, build image with 

        sudo docker build  -t pancancer_tabix_server .

### Downloading and restoring the image

1. Rather than building the image, you can also download and restore it from S3 

        aws s3 cp s3://oicr.docker.images/pancancer_tabix_server.tar .
        sudo docker load -i pancancer_tabix_server.tar

## Running the Container

1. Copy or link all tabix data from https://s3.amazonaws.com/pan-cancer-data/workflow-data/SangerPancancerCgpCnIndelSnvStr/tabix\_data/data/unmatched/ into the datastore directory. These files are confidential and cannot be freely shared:

        aws s3 cp s3://pan-cancer-data/workflow-data/SangerPancancerCgpCnIndelSnvStr/tabix_data /media/large_volume/tabix/data --recursive

2. Run container in the background while mounting the tabix data. You should be able to browse to  http://localhost/ and see a listing of the tabix files after this step. 

        sudo docker run -h master -v /media/large_volume/tabix/data:/data  -d -p 80:80 --name=pancancer_tabix_server -t -i   pancancer_tabix_server 

## Saving the image

1. Save the image

        exit
        sudo docker save -o pancancer_tabix_server.tar pancancer_tabix_server

2. Upload the image to S3 (given proper credentials)

        aws s3 cp pancancer_tabix_server.tar s3://oicr.docker.images
