# Query Engine README

This is a demo showing how a faceted browser can be created for genomic features from the Query Engine.

## Setup

Download and install elasticsearch.  I found the debian package to be easiest.  Start it using the /etc/init.d/elasticsearch script.

Install apache and copy the contents of this directory to your root web dir.

## Load Sample Data

Just use the sample data and curl to load

    curl -s -XPOST 'http://localhost:9200/_bulk' --data-binary @data_bulk.json

## Setup Sense

Install the chrome Sense plugin for elasticsearch. This makes it easy to see what's in the index and to do searches.  Once you load data you can search using:

   curl -XPOST 'http://localhost:9200/queryengine/features/_search' -d @query.json

Or via the Sense UI.

## Try Out the App

You can now load:

   http://localhost

And you should see the faceted browser.  

## Next Steps

Make a ESBulkDumper plugin for the query engine that either writes the bulk JSON format or uses Elasticsearch's Java API to write an index.

After that, we can expand the demo to include all possible tags, some of which will be treated specially as they are here, others may be selectable via a search field, etc.  The rest of the interface needs to be completed as well and hooked up to the general SeqWare Query Engine REST API for file writeback, for example.

