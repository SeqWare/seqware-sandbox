package io.seqware.queryengine.sandbox.testing.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import edu.berkeley.cs.amplab.adam.avro.ADAMVariant;

public class ADAMVariantSearch {
    HashMap<String, String> featuresQuery;
    HashMap<String, String> featureSetQuery;
    HashMap<String, String> regionsQuery;
    
    public ADAMVariantSearch(HashMap<String, String> features, HashMap<String, String> featureSets, HashMap<String, String> regions) {
        featuresQuery = features;
        featureSetQuery = featureSets;
        regionsQuery = regions;
    }
    
    public ArrayList<ADAMVariant> queryVariants( List<ADAMVariant> adamList ) {
        ArrayList<ADAMVariant> queryResults = new ArrayList<ADAMVariant>();
        for (ADAMVariant a: adamList) {
            if (!featureSetQuery.isEmpty()) {
                boolean featureSetMatch = true;
                for (Entry<String, String> entry : featureSetQuery.entrySet()) {
                    if (entry.getKey().equals("CHROM") && !(entry.getValue().equals(a.getId()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("POS") && !(entry.getValue().equals(a.getPosition()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("ID") && !(entry.getValue().equals(a.getReferenceId()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("REF") && !(entry.getValue().equals(a.getReferenceAllele()))) {
                        featureSetMatch = false; break;
                    }
                    /*if (entry.getKey().equals("ALT") && !(entry.getValue().equals(a.getReferenceName()))) {
                        featureSetMatch = false; break;
                    }*/
                    if (entry.getKey().equals("QUAL") && !(entry.getValue().equals(a.getQuality()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("FILTER") && !(entry.getValue().equals(a.getFilters()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.AF") && !(entry.getValue().equals(a.getAlleleFrequency()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.BQ") && !(entry.getValue().equals(a.getRmsBaseQuality()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.MQ") && !(entry.getValue().equals(a.getSiteRmsMappingQuality()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.MQ0") && !(entry.getValue().equals(a.getSiteMapQZeroCounts()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.NS") && !(entry.getValue().equals(a.getNumberOfSamplesWithData()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.SB") && !(entry.getValue().equals(a.getStrandBias()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.NS") && !(entry.getValue().equals(a.getNumberOfSamplesWithData()))) {
                        featureSetMatch = false; break;
                    }
                    if (entry.getKey().equals("INFO.AN") && !(entry.getValue().equals(a.getTotalNumberOfSamplesCount()))) {
                        featureSetMatch = false; break;
                    }
                }
                if (!featureSetMatch) {
                    continue;
                }
            }
            if (!regionsQuery.isEmpty()) {
                boolean regionsMatch = true;
                
                if (!regionsMatch) {
                    continue;
                }
            }
            if (!featuresQuery.isEmpty()) {
                boolean featuresMatch = true;
                for (Entry<String, String> entry : featuresQuery.entrySet()) {
                  /*
                    if (entry.getKey().equals("reference id") && !(entry.getValue().equals(a.getId()))) {
                        featuresMatch = false; break;
                    }
                    if (entry.getKey().equals("reference") && !(entry.getValue().equals(a.getReferenceName()))) {
                        featuresMatch = false; break;
                    }
                    if (entry.getKey().equals("reference") && !(entry.getValue().equals(a.getAlleleFrequency()))) {
                        featuresMatch = false; break;
                    }
                    if (entry.getKey().equals("reference") && !(entry.getValue().equals(a.getReferenceName()))) {
                        featuresMatch = false; break;
                    }
                    if (entry.getKey().equals("reference id") && !(entry.getValue().equals(a.getId()))) {
                      featuresMatch = false; break;
                    }
                    if (entry.getKey().equals("reference") && !(entry.getValue().equals(a.getReferenceName()))) {
                        featuresMatch = false; break;
                    }
                    if (entry.getKey().equals("reference") && !(entry.getValue().equals(a.getReferenceName()))) {
                        featuresMatch = false; break;
                    }
                    if (entry.getKey().equals("reference") && !(entry.getValue().equals(a.getReferenceName()))) {
                        featuresMatch = false; break;
                    }
                    */
                }
                if (!featuresMatch) {
                    continue;
                }
            }
            queryResults.add(a);
        }
        return queryResults;
    }
}