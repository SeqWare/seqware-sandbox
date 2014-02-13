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
import org.junit.Test;

import io.seqware.queryengine.sandbox.testing.impl.GATKPicardBackendTest;
import io.seqware.queryengine.sandbox.testing.plugins.*;
import io.seqware.queryengine.sandbox.testing.ReturnValue;
import io.seqware.queryengine.sandbox.testing.utils.VCFReader;

public class FeaturePluginRunnerTest {
	
	//This is basically and external implementation of the runPlugin function in BackendTestInterface, for ease of testing.
	//Will be added into GATK_Picard_Backend test.
	@Test
	public void runPlugin() throws IOException, JSONException{
		GATKPicardBackendTest b = new GATKPicardBackendTest();
		ReturnValue rv = new ReturnValue();
		VCFReader vcfReader;
		Iterator<VariantContext> vIter;
		Map<FeatureSet, Collection<Feature>> features;
		Map<String,String> output = new HashMap<String,String>();
		
		
		//Move these to some external global variable later, will not be called in the runPlugin params
		String outputDir = "src/main/resources/PluginData_Filtered";
		
		//This is to do a mock load of the input files
		//TAKE OUT in impl
		File inputDir = new File("src/main/resources/PluginData/");
		for (File child : inputDir.listFiles()){
			b.loadFeatureSet(child.getAbsolutePath());
		}
		
		
		File jsonQuery = new File("src/main/resources/testdata/query.json");
		InputStream is = new FileInputStream(jsonQuery);
		String jsonTxt = IOUtils.toString(is);
		
		/**Filtering commences**/
		b.getFilteredFiles(jsonTxt, outputDir);
		
		/**Create feature data set from filtered files**/
		features = b.makeMapInput(outputDir);
		
//		//TODO: run map method from pluginClass for every genome position in every filtered file
//		FeatureCountPlugin countInstance = new FeatureCountPlugin();
//		
//		String vcfFilePath = new String();
//		vcfReader = new VCFReader(vcfFilePath);
//		vIter = vcfReader.getVCFIterator();
//		while (vIter.hasNext()){
//			VariantContext vContext = vIter.next();
//			countInstance.map(vContext.getStart(), features, output); //Run plugin for every position.
//			
//		}
		
		
		rv.getKv().put("pluginResultFile", "resultFilePath");
//		return rv;
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
