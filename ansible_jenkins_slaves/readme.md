# Introduction

This demonstrates the usage of Ansible to create jenkins slave nodes. This has several components which may be useful for Bindle including code that sets up postgres ready for regression testing, creating a seqware folder and settings file, setting up SSH keys, Java, and building /etc/hosts (albeit in a hard-coded simplified manner).

TO-DO: Setup seqware-vagrant (Bindle) without hard-coding floating IP addresses so that jenkins capacity can scale as needed.

# Usage

1. Add your public keys to files/public_keys and edit the listed keys under "setup SSH keys" in the site.yml accordingly. 
2. Either add your Maven XML configuration file to files or delete the task "Copy maven configuration" (This is used to setup Maven repository mirroring for jenkins) 
3. Setup your inventory file (production) appropriately.  (For this version, you will need to launch a VM in openstack manually)
4. Run this playbook via

    ansible-playbook -i production site.yml