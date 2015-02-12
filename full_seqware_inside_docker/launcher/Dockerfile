# SeqWare 
#
# VERSION               1.1.0-alpha.6 
#
# Setup SeqWare with all services
# Volume mount \datastore to persist the contents of your workflows
# ex: udo docker run --rm -h master -t -v `pwd`/datastore:/mnt/datastore  -i seqware_1.1.0-alpha.6_full

FROM seqware_1.1.0-alpha.6_activated_base
MAINTAINER Denis Yuen <denis.yuen@oicr.on.ca>

# use ansible to create our dockerfile, see http://www.ansible.com/2014/02/12/installing-and-building-docker-with-ansible
ENV PATH /tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# setup seqware 
ENV HOSTNAME master
ENV PATH /home/seqware/bin:/tmp/ansible/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
# at this point, seqware has been fully setup
ENV HOME /home/seqware
USER seqware
WORKDIR /home/seqware
RUN git clone -b feature/oozie-sge_inside_docker  https://github.com/SeqWare/seqware-bag.git
ADD ./scripts/start.sh /start.sh
CMD ["/bin/bash", "/start.sh"]
