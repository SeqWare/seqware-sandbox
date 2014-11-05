# SeqWare 
#
# VERSION               1.1.0-alpha.5 
#
# Setup SeqWare with the BWA workflow
# Volume mount \datastore to persist the contents of your workflows
# ex:  sudo docker run -d -P --name web -v datastore:/datastore \bin\bash 

#FROM ubuntu:12.04
FROM ubuntu:14.04
MAINTAINER Denis Yuen <denis.yuen@oicr.on.ca>

# allow for docker within docker, see https://github.com/jpetazzo/dind
# Let's start with some basic stuff.
RUN apt-get update -qq && apt-get install -qqy \
    apt-transport-https \
    ca-certificates \
    lxc \
    iptables
# Install Docker from Docker Inc. repositories.
RUN echo deb https://get.docker.io/ubuntu docker main > /etc/apt/sources.list.d/docker.list \
  && apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 36A1D7869245C8950F966E92D8576A8BA88D21E9 \
  && apt-get update -qq \
  && apt-get install -qqy lxc-docker
# Install the magic wrapper.
ADD ./wrapdocker /usr/local/bin/wrapdocker
RUN chmod +x /usr/local/bin/wrapdocker
VOLUME /var/lib/docker

# use ansible to create our dockerfile, see http://www.ansible.com/2014/02/12/installing-and-building-docker-with-ansible
RUN apt-get -y update ;\
    apt-get install -y python-yaml python-jinja2 git wget;\
    git clone http://github.com/ansible/ansible.git /tmp/ansible
WORKDIR /tmp/ansible
# get a specific version of ansible , add sudo to ubuntu, create a working directory
RUN git checkout v1.7.2 ;\
    adduser --gecos 'Ubuntu user' --shell /bin/bash --disabled-password --home /home/ubuntu ubuntu ;\
    adduser ubuntu sudo ;\
    sed -i.bkp -e \
      's/%sudo\s\+ALL=(ALL\(:ALL\)\?)\s\+ALL/%sudo ALL=NOPASSWD:ALL/g' \
      /etc/sudoers ;\
    mkdir /datastore && chown ubuntu /datastore
ENV PATH /tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ENV ANSIBLE_LIBRARY /tmp/ansible/library
ENV PYTHONPATH /tmp/ansible/lib:$PYTHON_PATH
ENV HOME /home/ubuntu
WORKDIR /home/ubuntu
USER ubuntu
# setup seqware white star
RUN git clone -b develop https://github.com/SeqWare/seqware-bag.git
ADD inventory /etc/ansible/hosts
WORKDIR /home/ubuntu/seqware-bag 
RUN ansible-playbook mini-seqware-install.yml -c local --extra-vars "seqware_version=1.1.0-alpha.5"
ENV PATH /home/ubuntu/bin:/tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# at this point, seqware has been fully setup
# proceed on to layer on pan-cancer BWA workflow
WORKDIR /home/ubuntu
RUN git clone -b feature/mini-pancancer https://github.com/ICGC-TCGA-PanCancer/pancancer-bag.git 
WORKDIR /home/ubuntu/pancancer-bag 
RUN ansible-playbook mini-pancancer.yml -c local --extra-vars "user_name=ubuntu"
# do final cleanup and setup
EXPOSE 22 3000
WORKDIR /home/ubuntu
USER root
CMD ["wrapdocker"]
