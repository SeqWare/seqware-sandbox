package io.seqware.queryengine.sandbox.testing;

import io.seqware.queryengine.sandbox.testing.impl.ADAMBackendTest;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

/**
 * TestBackends
 *
 * This tool is designed to iterate over the available backends and call them
 * one-by-one, producing data structures that can be turned into reports.
 *
 * Steps: 1) pick 6 VCF (chr21) and BAM files (pair) from 1000 genome project
 *
 * 2) for each backend, setup
 *
 * 3) load each of the 6 VCF (FeatureSet) and BAM (ReadSet)
 *
 * 4) then do 2-3 queries using the JSON format to specify key/value tags and
 * feature/readsets
 *
 * TODO: agree on the format of the file being returned... look like VCF and BAM
 * (SAM)
 *
 * 5) call each of the plugins 2-3 times with different JSON
 *
 * TODO: write plugins or make spec
 *
 * Data set 1: 
 * 
 * 6: teardown the backend
 *
 *
 *
 */
public class TestBackends {

    private static void testBackend(BackendTestInterface backend, boolean browseReport, String[] args) throws RuntimeException, IOException {
        PrintWriter output = null;
        File tempFile = null;
        try {

            // data to download
            // use the same donor order in each array so BAM and VCF can be matched up
            // assumes there is a bam index named *.bam.bai
            String[] bams = new String[]{"ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA12156/cg_data/NA12156_lcl_SRR801819.mapped.COMPLETE_GENOMICS.CGworkflow2_2_evidenceOnly.CEU.high_coverage.20130401.bam"};
            // assumes there is a vcf index named *.vcf.gz.tbi
            String[] vcfs = new String[]{"ftp://ftp-trace.ncbi.nih.gov/1000genomes/ftp/data/NA12156/cg_data/NA12156_lcl_SRR801819.wgs.COMPLETE_GENOMICS.20121201.snps_indels_svs_meis.high_coverage.genotypes.vcf.gz"};
            // now download
            String[] localBams = download(bams, "bai");
            String[] localVCFs = download(vcfs, "tbi");


            // read the settings file which is an INI
            HashMap<String, String> settings = (args != null && args.length > 0 ? readSettingsFile(args[0]) : null);

            // the test backends
            ArrayList<BackendTestInterface> backends = new ArrayList<BackendTestInterface>();
            backends.add(backend);
            tempFile = File.createTempFile("report", "html");
            // output file
            output = new PrintWriter(tempFile, "UTF-8");
            fillOutHeader(output);

            // so this is the heart of the testing process
            for (BackendTestInterface b : backends) {

                output.write("<h1>" + b.getName() + "</h1>");

                // get some initial docs 
                output.write(b.getIntroductionDocs().getKv().get(BackendTestInterface.DOCS));

                // setup the backend
                check(b.setupBackend(settings));

                // iterate over the featureSets
                ArrayList<String> featureSets = new ArrayList<String>();
                for (String vcfPath : localVCFs) {
                    featureSets.add(check(b.loadFeatureSet(vcfPath)).getKv().get("featureSetId"));
                }

                // iterate over the readSets
                ArrayList<String> readSets = new ArrayList<String>();
                for (String bamPath : localBams) {
                    readSets.add(check(b.loadReadSet(bamPath)).getKv().get("readSetId"));
                }

                // query the features
                output.write(testFeatureSets(featureSets, b));

                // query the reads
                output.write(testReadSets(readSets, b));

                // TODO: run the plugins
                // need to iterate over the available plugins
                // and then call b.runPlugin();

                // final docs
                output.write(b.getConclusionDocs().getKv().get(BackendTestInterface.DOCS));

                // teardown
                check(b.teardownBackend(settings));

            }

            fillOutFooter(output);

        } catch (Exception ex) {
            Logger.getLogger(TestBackends.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException(ex);
        }  finally {
            IOUtils.closeQuietly(output);
            
            if (tempFile != null && browseReport) Desktop.getDesktop().browse((tempFile.toURI()));
        }
    }
    
    @Test
    public void testADAMBackEnd(){
        try{
        testBackend(new ADAMBackendTest(), false, null);
        } catch (Exception e){
            Assert.assertTrue(false);
        }
    }

    /**
     * This tool assumes: "java TestBackends settings.ini"
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        testBackend(new ADAMBackendTest(), true, args);
    }

    private static void fillOutHeader(PrintWriter o) {
        o.write("<html><body>");
    }

    private static void fillOutFooter(PrintWriter o) {
        o.write("</body></html>");
    }

    /**
     * TODO: this is where it should do heavy lifting of testing featureSet
     * queries using the getFeatures() method for each of the featureSets loaded
     * in the backend. You should do multiple JSON queries and these should cut
     * across featureSets. Work will need to be done to agree with the output
     * format.
     *
     * @param featureSets
     * @return
     */
    private static String testFeatureSets(ArrayList<String> featureSets, BackendTestInterface b) {
        return ("<p>FEATURESET TESTING TO BE IMPLEMENTED!</p>");
    }

    /**
     * TODO: this is where it should do heavy lifting of testing readSet queries
     * using the getReads() method for each of the readSets loaded in the
     * backend. You should do multiple JSON queries and these should cut across
     * readSets. Work will need to be done to agree with the output format.
     *
     * @param featureSets
     * @return
     */
    private static String testReadSets(ArrayList<String> readSets, BackendTestInterface b) {
        return ("<p>READSET TESTING TO BE IMPLEMENTED!</p>");
    }

    /**
     * TODO
     *
     * @param string
     */
    private static HashMap<String, String> readSettingsFile(String iniFile) {
        // need to parse the ini file passed in
        return (new HashMap<String, String>());
    }

    /**
     * TODO: probably want a better check here, more sophisticated
     *
     * @param setupBackend
     */
    private static ReturnValue check(ReturnValue rv) {
        if (rv.getState() != ReturnValue.SUCCESS) {
            System.err.println("BOOM! Something bad happened! Error value: " + rv.getState());
        }
        return (rv);
    }

    /**
     * TODO
     *
     * @param bams
     * @return
     */
    private static String[] download(String[] files, String indexExtension) {
        // need to download the files (and indexes) to a local directory then
        // populate a return String[] with thier local paths
        return (files);
    }
}
