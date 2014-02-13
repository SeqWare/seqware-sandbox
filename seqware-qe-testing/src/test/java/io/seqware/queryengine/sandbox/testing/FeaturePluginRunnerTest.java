package io.seqware.queryengine.sandbox.testing;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import io.seqware.queryengine.sandbox.testing.impl.GATKPicardBackendTest;
import io.seqware.queryengine.sandbox.testing.plugins.*;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import io.seqware.queryengine.sandbox.testing.utils.VCFReader;

public class FeaturePluginRunnerTest {
	
	//This is basically and external implementation of the runPlugin function in BackendTestInterface, for ease of testing.
	//Will be added into GATK_Picard_Backend test.
	public void testBackend(BackendTestInterface b) throws IOException, JSONException{
		ReturnValue rv = new ReturnValue();

		File inputDir = new File("src/main/resources/PluginData/");
		for (File child : inputDir.listFiles()){
			b.loadFeatureSet(child.getAbsolutePath());
		}
		
		File jsonQuery = new File("src/main/resources/testdata/query.json");
		InputStream is = new FileInputStream(jsonQuery);
		String jsonTxt = IOUtils.toString(is);
		
		b.runPlugin(jsonTxt, FeatureCountPlugin.class);
	}
	
    @Test
    public void testGATK_PicardBackEnd() throws JSONException{
    	try{
    		testBackend(new GATKPicardBackendTest());
    	} catch (RuntimeException | IOException e) {
            Assert.assertTrue(false);
        }
    }

	public class FeatureCountPlugin implements FeaturePluginInterface{

		@Override
		public void map(long position,
				Map<FeatureSet, Collection<Feature>> features,
				Map<String, String> output) {
			Iterator<Feature> featureIter = features.get("vcf1chr5").iterator();
			while (featureIter.hasNext()){
				System.out.println();
			}
		}

		@Override
		public void reduce(String key, Iterable<String> values,
				Map<String, String> output) {
			
			
		}
		
	}
	
	
}
