# Query Engine README

This is a demo showing how a faceted browser can be created for genomic features from the Query Engine project (or just a VCF file(s)).

# Setup

## Elasticsearch

Download and install elasticsearch.  I found the debian package to be easiest on Ubuntu.  Start it using the /etc/init.d/elasticsearch script. Or, if you're on a mac, you can download a tarball and just execute the ./bin/elasticsearch script.

## Sense (Optional)

Install the chrome Sense plugin for elasticsearch. This makes it easy to see what's in the index and to do searches.  Once you load data you can search using, for example:

    curl -XPOST 'http://localhost:9200/queryengine/features/_search' -d @query.json

Or via the Sense UI.

## Apache

### Ubuntu

Install apache if you have not already

    sudo apt-get install apache2

Copy the app (or symlink) to the install directory and edit the application to resolve to a resolveable ip address if you wish to access the app from a different computer:

    cp -R * /var/www
    vim /var/www/js/app.js (replace localhost with whatever IP you're using if on a server)

Restart apache:

    sudo /etc/init.d/apache2 restart

### Mac

See http://jason.pureconcepts.net/2014/11/install-apache-php-mysql-mac-os-x-yosemite/ It's already installed, just do

    sudo apachectl start

The location for the doc root is `/Library/WebServer/Documents`.  You can copy the contents to this location as instructed above.

# Example 1 - Basic Variant Browser

## Load Sample Data

Just use the sample data and curl to load

    curl -s -XPOST 'http://localhost:9200/_bulk' --data-binary @data.json

Alternatively, you can follow the SeqWare Query Engine readme to import a VCF and dump it back to JSON for use with elasticsearch.

## View Demo

Open in your browser:

    http://localhost/index.simple.html

You may run into cross domain Javascript issues although it seems to work for me when I host on Ubuntu and replace with an actual domain name here and in the js/app*.js files.

# Example 2 - Clinical Variant Browser

This example shows several "patients" and their variants.

## Making Custom Data

I created a simple script that will take a JSON describing patients and the VCF files to use for each. It will then directly generate a JSON doc ready to load:

    {
      "1002" : {
        "gender": "female",
        "age": "44",
        "race": "black",
        "diagnosis_code": ["555.1", "555.2"],
        "smoker": "former",
        "drugs": ["Azathioprine", "Infliximab"],
        "biorepo": "small intestine FFPE",
        "vcf_file": "VariantAnnotation_0.10.4_LS1155.annotated.vcf",
        "max_variants" : 100,
        "prob_of_inclusion" : 0.1
      },
      "1004" : {
        "gender": "male",
        "age": "33",
        "race": "white",
        "diagnosis_code": "555.1",
        "smoker": "yes",
        "drugs": "Azathioprine",
        "biorepo": "large intestine FFPE",
        "vcf_file": "VariantAnnotation_0.10.4_LS1155.annotated.vcf",
        "max_variants" : 100,
        "prob_of_inclusion" : 0.1
      },
      "1005" : {
        "gender": "female",
        "age": "55",
        "race": "black",
        "diagnosis_code": "555.2",
        "smoker": "no",
        "drugs": "Infliximab",
        "biorepo": "small intestine FFPE",
        "vcf_file": "VariantAnnotation_0.10.4_LS1155.annotated.vcf",
        "max_variants" : 100,
        "prob_of_inclusion" : 0.1
      }
    }

The command:

    perl make_data_from_vcf.pl patients.json  > data.json

You will need to supply your own VCF file in the above example.

## View Demo

Open in your browser:

    http://localhost/index.clinical.html

You may run into cross domain Javascript issues although it seems to work for me when I host on Ubuntu and replace with an actual domain name here and in the js/app*.js files.

# Example 3 - GIAB Data Visualization

This is a pretty complex example.  First, some background.  The Genome in a Bottle Consortium provides physical material and informatics variant calls to serve as a "gold standard" when variant calling whole genomes. See https://sites.stanford.edu/abms/giab.  This example starts with sequencing from Ion Torrent sequencing of NA12878.  Variant calling was then performed using a variety of parameterizations for the Torrent Variant Caller (TVC), resulting in several differnt sets of variant calls.  The idea being that we want to find a well-performing parameterization for this tool and we determine this by comparing to the "known good" variant calls from GIAB.  These variant calls are then annotated using

## Making Custom Data

I created a simple script that will take a JSON describing individual variant calling trials and the VCF files to use for each. First it calls SNPEff on each VCF then loads the data. It will then directly generate a JSON doc ready to load into Elasticsearch:

    {
      "trial1" : {
        "params": "params1.json",
        "true_positives": "truepositives1.vcf",
        "false_positives": "falsepositives1.vcf",
        "run_snpeff": true
      },
      "trial2" : {
        "params": "params2.json",
        "true_positives": "truepositives2.vcf",
        "false_positives": "falsepositives2.vcf",
        "run_snpeff": true
      }
    }

The command:

    perl make_data_from_giab_vcfs.pl --json-file giab.json --output-json data.json

You will need to supply your own VCF files in the above example, this demo assumes you're starting with NA12878 variants that have been run through GIAB tools to produce TP and FP variant calls.

## View Demo

Open in your browser:

    http://localhost/index.giab.html

You may run into cross domain Javascript issues although it seems to work for me when I host on Ubuntu and replace with an actual domain name here and in the js/app*.js files.

# Purging Data

If you want to clean out elastic search do the following:

    curl -XDELETE 'http://localhost:9200/queryengine'

You can see the general status and confirm you removed this with:

    curl 'localhost:9200/_cat/indices?v'

# Next Steps

Make a plugin that uses Elasticsearch's Java API to write an index.

After that, we can expand the demo to include all possible tags, some of which will be treated specially as they are here, others may be selectable via a search field, etc.  The rest of the interface needs to be completed as well and hooked up to the general SeqWare Query Engine REST API for file writeback, for example.
