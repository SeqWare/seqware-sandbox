This layers all pancancer dependencies (aside from the workflow)  on top of a base SeqWare docker image

Assumes that seqware inside docker has already been built. 

---------------------------------------------------------------

1. Set permissions on datastore which will hold results of workflows after they run

     chmod a+w datastore

2. Run the tabix server as a named container if you have not already (see tabix\_inside\_docker) 

3. Download and expand your workflows using the SeqWare unzip tool. Here we use Sanger as an example.

     cd workflows
     wget https://seqwaremaven.oicr.on.ca/artifactory/seqware-release/com/github/seqware/seqware-distribution/1.1.0-alpha.6/seqware-distribution-1.1.0-alpha.6-full.jar
     wget https://s3.amazonaws.com/oicr.workflow.bundles/released-bundles/Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5.zip
     java -cp seqware-distribution-1.1.0-alpha.6-full.jar net.sourceforge.seqware.pipeline.tools.UnZip --input-zip Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5.zip --output-dir  Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5


4. Assuming docker is installed properly, build image with 
 
     cd ..
     sudo docker build  -t seqware_1.1.0-alpha.6_pancancer .

5. Run container and login with the following (while persisting workflow run directories to datastore, and opening a secure link to the tabix server). Here we assume that a tabix container has already started, that you want to store your workflow results at /datastore and that your workflow is present at /workflow\_volume

     sudo docker run --rm -h master -t --link pancancer_tabix_server:pancancer_tabix_server -v `pwd`/datastore:/datastore -v workflows/Workflow_Bundle_SangerPancancerCgpCnIndelSnvStr_1.0.1_SeqWare_1.1.0-alpha.5:/workflow  -i seqware_1.1.0-alpha.6_pancancer

6. Run workflow with 

     seqware bundle launch --dir /workflow --no-metadata --ini workflow.ini
