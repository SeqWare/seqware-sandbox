package io.seqware.queryengine.sandbox.testing;

/**
 * TestBackends
 * 
 * This tool is designed to iterate over the available backends and call them
 * one-by-one, producing data structures that can be turned into reports.
 *
 * Steps:
 * 1) pick 6 VCF (chr21) and BAM files (pair) from 1000 genome project
 * 
 * 2) for each backend, setup
 * 
 * 3) load each of the 6 VCF (FeatureSet) and BAM (ReadSet)
 * 
 * 4) then do 2-3 queries using the JSON format to specify key/value tags and feature/readsets
 * 
 * TODO: agree on the format of the file being returned... look like VCF and BAM (SAM) 
 * 
 * 5) call each of the plugins 2-3 times with different JSON
 * 
 * TODO: write plugins or make spec
 * 
 */
public class TestBackends 
{
    public static void main( String[] args )
    {
        System.out.println( "Implement Me!" );
    }
}

