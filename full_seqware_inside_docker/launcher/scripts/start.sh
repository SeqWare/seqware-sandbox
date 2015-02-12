#!/bin/bash

# kick off all services

ansible-playbook /mnt/home/seqware/seqware-bag/docker-start.yml -c local --extra-vars "single_node=True"
sudo nohup cron -f &
cd ~seqware
/bin/bash
