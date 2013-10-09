## Dependencies

Build and configure the SeqWare Query Engine using the develop branch in the https://github.com/SeqWare/queryengine repo
Make sure you setup your .seqware/settings
Test reference creation and VCF importing using the commands from that project's README before attempting to use this utility. 

## Basic Commands

Download ICGC files, unzip them, convert them into VCF-like files, and upload into the query engine using the following commands:

    mvn clean install
    java -Xmx6G  -jar target/icgc-2-vcf-converter-1.0-SNAPSHOT.jar &> output.txt

Runtime should be about 6m35.579s and you will probably want to hang on the randomly generated reference SGID (near the beginning of the output) for map/reduce analysis in a follow-up utility. 
