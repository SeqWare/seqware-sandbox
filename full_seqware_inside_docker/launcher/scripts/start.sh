#!/bin/bash

# kick off all services

sudo service tomcat7 start
sudo service postgresql start
sudo service hadoop-hdfs-datanode start
sudo service hadoop-hdfs-namenode start
sudo service hadoop-0.20-mapreduce-jobtracker start
sudo service hadoop-0.20-mapreduce-tasktracker start
sudo service oozie start 
sudo service gridengine-master start 
sudo service gridengine-exec start 
sudo nohup cron -f &
cd ~seqware
/bin/bash


