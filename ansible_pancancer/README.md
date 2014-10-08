This is a set of utility scripts for monitoring Pan-cancer BWA clusters using Ansible.
To run these you need to have Ansible installed and an inventory file prepared that details the IPs of your compute clusters.

The inventory file can be prepared by running the prepare_inventory.py script with Python:

python prepare_inventory.py inventory_file_location path_to_vagrant_target_dirs

Each ansible script can be invoked as follows:

ansible-playbook ./qstat-report.yaml -i ansible_host_inventory.ini --extra-vars "@./playbook_config.yaml"

The file playbook_config.yaml contains environment specific configurations like names of users and locations of various files. This will likely be customized for each configuration.

The file ansible.cfg indicates to Ansible that when sudo is used the sudo user's environment is loaded. This is needed to run seqware commands inside ansible scripts.
