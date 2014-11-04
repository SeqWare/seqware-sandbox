

1. Assuming docker is installed properly, build image with 

        sudo docker build .

2. Run container and login with 

        sudo docker run -h master -t -i <last container ID> /bin/bash

3. Run workflow with 

        seqware bundle launch --dir ~/provisioned-bundles/Workflow_Bundle_BWA_2.6.1_SeqWare_1.1.0-alpha.2/ --no-metadata
