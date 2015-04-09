#!/usr/bin/env bash
set -o errexit
set -o pipefail

echo "testMode=true" > workflow.ini
echo "tabixSrvUri=http://tabix/" >> workflow.ini
cat workflow.ini

seqware bundle launch --dir /workflow --no-metadata --ini workflow.ini
