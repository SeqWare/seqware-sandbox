This is the base Dockerfile for tabix inside docker. 


1. Copy or link all tabix data from https://s3.amazonaws.com/pan-cancer-data/workflow-data/SangerPancancerCgpCnIndelSnvStr/tabix\_data/data/unmatched/ into the datastore directory. For example, after installing the AWS CLI and setting up your AWS credentials ( https://aws.amazon.com/cli/ and http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html ):

        aws s3 cp s3://pan-cancer-data/workflow-data/SangerPancancerCgpCnIndelSnvStr/tabix_data /media/large_volume/tabix/data --recursive

2. Assuming docker is installed properly, build image with 

        sudo docker build  -t pancancer_tabix_server .

3. Run container in the background while mounting the tabix data 

        sudo docker run -h master -v /media/large_volume/tabix/data:/data  -d -p 80:80 --name=pancancer_tabix_server -t -i   pancancer_tabix_server 

5. Save the image

        exit
        sudo docker save -o pancancer_tabix_server.tar pancancer_tabix_server
