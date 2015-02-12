nginx
==============

A playbook that does the following:

	- formats and mounts an EBS volume attached to the AWS instance as ext4

	- installs Nginx

	- configure Nginx to use the EBS volume as document root

## Usage

1. Spin up a Ubuntu 12.04 instance with a 350GB epehermal drive at /dev/sdb (Amazon maps this to /dev/xvdb)
2. Point the inventory file at your tabix server
3. Run the playbook

    ansible-playbook -i inventory site.yml

4. Login to the instance, add your aws credentials ( http://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html ), and populate /data/unmatched with tabix data

    mkdir ~/.aws
    vim ~/.aws/config    
    sudo aws s3 cp s3://pan-cancer-data/workflow-data/SangerPancancerCgpCnIndelSnvStr/tabix_data /data/unmatched --recursive
    
    
