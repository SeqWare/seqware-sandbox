# Setup SeqWare with the Sanger workflow
# Volume mount \datastore to persist the contents of your workflows
# ex:  sudo docker run -d -P --name web -v datastore:/datastore \bin\bash 

FROM seqware_1.1.0-alpha.6_full
MAINTAINER Denis Yuen <denis.yuen@oicr.on.ca>

# at this point, assume seqware has been fully setup
# proceed on to layer on pan-cancer BWA workflow
USER seqware
WORKDIR /home/seqware
RUN git clone https://github.com/ICGC-TCGA-PanCancer/pancancer-bag.git 
WORKDIR /home/seqware/pancancer-bag 
RUN git checkout 1.0-beta.2
RUN ansible-playbook pancancer-dependencies.yml -c local --extra-vars "user_name=seqware"
