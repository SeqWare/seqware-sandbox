import argparse
from os import path as path
import re
from subprocess import check_output, Popen, PIPE
import json
from os import remove

parser = argparse.ArgumentParser(description="Convert a Seqware decider JSON file for BWA to an ansible host inventory file")
parser.add_argument("--run_generator", help="Enabling this option automatically runs the generator and passes the standard out of the generator to the program as input. If you enable this option you should indicate the path to the generator JAR via the --generator_path argument.", action="store_true")
parser.add_argument("--generator_path", help="Path to the generator JAR") 
parser.add_argument("--input_file_path", help="Full path to the input JSON file")
parser.add_argument("output_file_path", help="Full path to the inventory host file to be generated")
args = parser.parse_args()
arg_dict = vars(args)

run_generator = arg_dict["run_generator"]
generator_path = arg_dict["generator_path"]
input_file_path = arg_dict["input_file_path"]
output_file_path = arg_dict["output_file_path"]

tmp_json_filename = "generator_tmp.json"

if run_generator:
	(prog_out, prog_err) = Popen(["java", "-jar", generator_path, "--aws", "--output", tmp_json_filename],stdin=PIPE, stdout=PIPE, stderr=PIPE).communicate()
	input_file_path = tmp_json_filename

infile = open(input_file_path, "r")
json_text = json.load(infile)
infile.close()

if run_generator:
	remove(tmp_json_filename)

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
