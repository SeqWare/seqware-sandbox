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

import io.seqware.queryengine.sandbox.testing.TestBackends.AbstractPlugin;
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
		
		b.runPlugin(jsonTxt, SimpleFeaturesCountPlugin.class);
	}
	
    @Test
    public void testGATK_PicardBackEnd() throws JSONException{
    	try{
    		testBackend(new GATKPicardBackendTest());
    	} catch (RuntimeException | IOException e) {
            Assert.assertTrue(false);
        }
    }

    public class SimpleFeaturesCountPlugin extends AbstractPlugin<Feature, FeatureSet> implements FeaturePluginInterface{
    }
    
    public abstract class AbstractPlugin <UNIT, SET>{
        public final String count = "COUNT";
        
        public void map(long position, Map<SET, Collection<UNIT>> reads, Map<String, String> output) {
            if (!output.containsKey(count)){
                output.put(count, String.valueOf(0));
            }
            for(Collection<UNIT> readCollection  :reads.values()){
                Integer currentCount = Integer.valueOf(output.get(count));
                int nextCount = currentCount += readCollection.size();
                output.put(count, String.valueOf(nextCount));
            }
        }

        public void reduce(String key, Iterable<String> values, Map<String, String> output) {
                Integer currentCount = Integer.valueOf(output.get(count));
                for(String value : values){
                    currentCount = currentCount += 1;
                }
                output.put(count, String.valueOf(currentCount));
        }
    }
	
	
}
