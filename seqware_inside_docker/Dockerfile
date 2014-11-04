# SeqWare 
#
# VERSION               1.1.0-alpha.5 

FROM ubuntu:12.04
MAINTAINER Denis Yuen <denis.yuen@oicr.on.ca>

# use ansible to create our dockerfile, see http://www.ansible.com/2014/02/12/installing-and-building-docker-with-ansible
RUN apt-get -y update ;\
    apt-get install -y python-yaml python-jinja2 git wget;\
    git clone http://github.com/ansible/ansible.git /tmp/ansible
WORKDIR /tmp/ansible
RUN git checkout v1.7.2 ;\
    adduser --gecos 'Ubuntu user' --shell /bin/bash --disabled-password --home /home/ubuntu ubuntu ;\
    adduser ubuntu sudo ;\
    sed -i.bkp -e \
      's/%sudo\s\+ALL=(ALL\(:ALL\)\?)\s\+ALL/%sudo ALL=NOPASSWD:ALL/g' \
      /etc/sudoers
# create a ubuntu user and give it passwordless sudo
ENV PATH /tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
ENV ANSIBLE_LIBRARY /tmp/ansible/library
ENV PYTHONPATH /tmp/ansible/lib:$PYTHON_PATH
ENV HOME /home/ubuntu
WORKDIR /home/ubuntu
USER ubuntu
RUN git clone -b feature/mini-seqware https://github.com/SeqWare/seqware-bag.git
ADD inventory /etc/ansible/hosts
WORKDIR /home/ubuntu/seqware-bag 
RUN ansible-playbook mini-seqware-install.yml -c local --extra-vars "seqware_version=1.1.0-alpha.5"
ENV PATH /home/ubuntu/bin:/tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# at this point, seqware has been fully setup
EXPOSE 22 3000

