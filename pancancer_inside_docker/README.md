This layers all pancancer dependencies (aside from the workflows themselves, which are fairly big)  on top of a base SeqWare docker image

## Prerequisites

Install the AWS CLI. Refer to the following guides and remember to setup your AWS credentials.
 
* https://aws.amazon.com/cli/ 
* http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html 

        sudo apt-get install python-pip
        pip install awscli


You will also require the tabix container in order to run the Sanger workflow. 

If you are building the container, you will require the seqware\_inside docker image on your system. 

## Getting the image

### Building the image

1. Assuming docker is installed properly, build image with 
 
        sudo docker build  -t seqware_1.1.0-alpha.6_pancancer .

### Downloading and restoring the image

1. Rather than building the image, you can also download and restore it from S3 

        aws s3 cp s3://oicr.docker.images/seqware_1.1.0-alpha.6_pancancer.tar .
        sudo docker load -i seqware_1.1.0-alpha.6_pancancer.tar

## Running the Container


1. Set permissions on datastore which will hold results of workflows after they run

         chmod a+w datastore

2. Run the tabix server as a named container if you have not already (see tabix\_inside\_docker) 

3. Download and expand your workflows using the SeqWare unzip tool. Here we use Sanger as an example (you should probably pick a shared directory outside of this directory to avoid interfering with the Docker context if you need to rebuild the image). 

         cd workflows
         wget https://seqwaremaven.oicr.on.ca/artifactory/seqware-release/com/github/seqware/seqware-distribution/1.1.0-alpha.6/seqware-distribution-1.1.0-alpha.6-full.jar
         wget https://s3.amazonaws.com/oicr.workflow.bundles/released-bundles/Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5.zip
         java -cp seqware-distribution-1.1.0-alpha.6-full.jar net.sourceforge.seqware.pipeline.tools.UnZip --input-zip Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5.zip --output-dir  Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5

4. Run container and login with the following (while persisting workflow run directories to datastore, and opening a secure link to the tabix server). Here we assume that a tabix container has already started, that you want to store your workflow results at /datastore and that the workflow that you wish to run (Sanger) is present in the workflows directory. Change these locations as required for your environment.  

         sudo docker run --rm -h master -t --link pancancer_tabix_server:pancancer_tabix_server -v `pwd`/datastore:/datastore -v workflows/Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5:/workflow  -i seqware_1.1.0-alpha.6_pancancer

5. Create an ini file (the contents of this will depend on your workflow). For testing purposes, you will require the following ini, note that the ip address for the tabix server will appear in your environment variables as PANCANCER\_TABIX\_SERVER\_PORT\_80\_TCP\_ADDR 

         # not "true" means the data will be downloaded using AliquotIDs
         testMode=true
         # the server that has various tabix-indexed files on it, see above, update with your URL
         tabixSrvUri=http://172.17.0.13/   

6. Run workflow sequentially with 

         seqware bundle launch --dir /workflow --no-metadata --ini workflow.ini

   Alternatively, run it in parallel with the following command. 
 
         seqware bundle launch --dir /workflow --no-metadata --ini workflow.ini --engine whitestar-parallel

7. For running real workflows, you will be provided with a gnos pem key that should be installed to the scripts directory of the Sanger workflow.

## Saving the image

1. Save the image

        exit
        sudo docker save -o seqware_1.1.0-alpha.6_pancancer.tar seqware_1.1.0-alpha.6_pancancer

2. Upload the image to S3 (given proper credentials)

        aws s3 cp seqware_1.1.0-alpha.6_pancancer.tar s3://oicr.docker.images
