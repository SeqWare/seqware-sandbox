# SeqWare Query Engine Testing

## About

This repo contains testing code that allows us to try out a variety of backends.  
We store some simplified genomic data in each backend type, try out queries on top
and record the result in a reporting format.  Ultimately, this info will help us 
evalaute tools to find the best backend for storing BAM/VCF data.

## Compiling

    mvn clean install

## Running the Test

Once you have your jar file from above go ahead and run the test.

The system assumes you have all the various backend requirements installed.

You will then run the tests and generate a report with:

    java -jar target/seqware-qe-testing-1.0.jar --tsv-file <tsv_file_path>

## Developing New Tests

First, setup whatever you will need for your particular backend.  Next, implement the test interface (BackendTestInterface).  Finally, hook in your test into the main class TestBackends.  Document how you setup your particular backend in the source of your test implementation, there's a method to display this info.

## Next Steps/TODO

* implement several of the backends
* implement a report generation tool to make HTML/PNG outputs that can be shared
* ultimately we will use this to decide what technologies we will move forward with for the main SeqWare Query Engine project.
* need to write a JSON parser so each backend doesn't need to re-write this
* need to define the way to encode key/values on VCF/TSV/BAM inputs... for featureSets, features, ReadSets, reads
