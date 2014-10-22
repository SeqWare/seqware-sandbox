import argparse
from os import path as path
import re
from subprocess import check_output, Popen, PIPE
import json

parser = argparse.ArgumentParser(description="Convert a Seqware decider JSON file for BWA to an ansible host inventory file")
parser.add_argument("input_file_path", help="Full path to the input JSON file")
parser.add_argument("output_file_path", help="Full path to the inventory host file to be generated")
args = parser.parse_args()
arg_dict = vars(args)

input_file_path = arg_dict["input_file_path"]
output_file_path = arg_dict["output_file_path"]

infile = open(input_file_path, "r")
json_text = json.load(infile)
infile.close()

outfile = open(output_file_path, "w")
outfile.write("[all-masters]\n")

for instance_name in json_text:
	webservice_url = json_text[instance_name]["webservice"]
	
	my_match = re.search(r'[0-9]+(?:\.[0-9]+){3}', webservice_url)
	my_ip = ""
	if my_match:
		my_ip = my_match.group(0)
        outfile.write(instance_name + " ansible_ssh_host=" + my_ip + "\n") 

outfile.close()
