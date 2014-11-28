# Setup SeqWare with the Sanger workflow
# Volume mount \datastore to persist the contents of your workflows
# ex:  sudo docker run -d -P --name web -v datastore:/datastore \bin\bash 

FROM seqware_1.1.0-alpha.5 
MAINTAINER Denis Yuen <denis.yuen@oicr.on.ca>

# at this point, assume seqware has been fully setup
# proceed on to layer on pan-cancer BWA workflow
USER seqware
WORKDIR /home/seqware
RUN git clone -b feature/workflow_Sanger https://github.com/ICGC-TCGA-PanCancer/pancancer-bag.git 
WORKDIR /home/seqware/pancancer-bag 
RUN ansible-playbook mini-sanger-pancancer.yml -c local --extra-vars "user_name=seqware"
