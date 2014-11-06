# DIND 
#
# VERSION               1.1.0-alpha.5 
# 
# Wrap a SeqWare docker image with docker in docker functionality 

FROM seqware_1.1.0-alpha.5
MAINTAINER Denis Yuen <denis.yuen@oicr.on.ca>

USER root
# uncomment everything with #DIND to activate docker in docker functionality 
# allow for docker within docker, see https://github.com/jpetazzo/dind
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

# do cleanup and setup
EXPOSE 22 3000
WORKDIR /home/seqware
CMD ["wrapdocker"]
