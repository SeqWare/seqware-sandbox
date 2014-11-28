# SeqWare 
#
# VERSION               1.1.0-alpha.5 
#
# Setup SeqWare with WhiteStar (no sge, no oozie, no metadata)
# Volume mount \datastore to persist the contents of your workflows
# ex:  sudo docker run -d -P --name web -v datastore:/datastore \bin\bash 

FROM ubuntu:12.04
MAINTAINER Denis Yuen <denis.yuen@oicr.on.ca>

# use ansible to create our dockerfile, see http://www.ansible.com/2014/02/12/installing-and-building-docker-with-ansible
RUN apt-get -y update ;\
    apt-get install -y python-yaml python-jinja2 git wget;\
    git clone http://github.com/ansible/ansible.git /tmp/ansible
WORKDIR /tmp/ansible
# get a specific version of ansible , add sudo to seqware, create a working directory
RUN git checkout v1.6.10 ;\
    adduser --gecos 'Ubuntu user' --shell /bin/bash --disabled-password --home /home/seqware seqware ;\
    apt-get install -y sudo vim;\
    adduser seqware sudo ;\
    sed -i.bkp -e \
      's/%sudo\s\+ALL=(ALL\(:ALL\)\?)\s\+ALL/%sudo ALL=NOPASSWD:ALL/g' \
      /etc/sudoers ;\
    mkdir /datastore && chown seqware /datastore
ENV PATH /tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ENV ANSIBLE_LIBRARY /tmp/ansible/library
ENV PYTHONPATH /tmp/ansible/lib:$PYTHON_PATH
ENV HOME /home/seqware
WORKDIR /home/seqware
USER seqware
# setup seqware white star
RUN git clone -b develop https://github.com/SeqWare/seqware-bag.git
ADD inventory /etc/ansible/hosts
WORKDIR /home/seqware/seqware-bag 
RUN ansible-playbook mini-seqware-install.yml -c local --extra-vars "seqware_version=1.1.0-alpha.5"
ENV PATH /home/seqware/bin:/tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# at this point, seqware has been fully setup

