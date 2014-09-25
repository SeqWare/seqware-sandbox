# Query Engine README

This is a demo showing how a faceted browser can be created for genomic features from the Query Engine.

## Setup

Download and install elasticsearch.  I found the debian package to be easiest.  Start it using the /etc/init.d/elasticsearch script.

Install apache and copy the contents of this directory to your root web dir.

## Load Sample Data

Just use the sample data and curl to load

    curl -s -XPOST 'http://localhost:9200/_bulk' --data-binary @data.json

Alternatively, you can follow the SeqWare Query Engine readme to import a VCF and dump it to JSON. 

## Making More Data

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

## Pruging Data

If you want to clean out elastic search do the following:

    curl -XDELETE 'http://localhost:9200/queryengine'

You can see the general status and confirm you removed this with:

    curl 'localhost:9200/_cat/indices?v'

## Setup Sense

Install the chrome Sense plugin for elasticsearch. This makes it easy to see what's in the index and to do searches.  Once you load data you can search using:

    curl -XPOST 'http://localhost:9200/queryengine/features/_search' -d @query.json

Or via the Sense UI.

## Try Out the App

Install apache if you have not already
 
    sudo apt-get install apache2

Copy the app to the install directory and edit the application to resolve to a resolveable ip address if you wish to access the app from a different computer:

    cp -R * /var/www
    vim /var/www/js/app.js (replace localhost)

Restart apache:

    sudo /etc/init.d/apache2 restart

You can now load:

    http://localhost

And you should see the faceted browser.

## Next Steps

Make a plugin that uses Elasticsearch's Java API to write an index.

After that, we can expand the demo to include all possible tags, some of which will be treated specially as they are here, others may be selectable via a search field, etc.  The rest of the interface needs to be completed as well and hooked up to the general SeqWare Query Engine REST API for file writeback, for example.

