package io.seqware.queryengine.sandbox.testing;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;

import io.seqware.queryengine.sandbox.testing.plugins.*;

public class FeaturePluginRunnerTest {
	@Test
	//This is basically and external implementation of the runPlugin function in BackendTestInterface, for ease of testing.
	//Will be added into GATK_Picard_Backend test.
	public void runPlugin(/**There will be a json input here + the plugin authors plugin class**/) throws IOException, JSONException{
		FeaturePluginRunner runMe = new FeaturePluginRunner();
		
		//Move these to some external global variable later, will not be called in the runPlugin params
		String outputDir = System.getProperty("user.home") + "/Filtered";
		String inputDir = "src/main/resources/PluginData/";
		
		File jsonQuery = new File("src/main/resources/testdata/query.json");
		InputStream is = new FileInputStream(jsonQuery);
		String jsonTxt = IOUtils.toString(is);
		
		//Filtering commences
		runMe.getFilteredFiles(inputDir, jsonTxt, outputDir);
		
		//TODO: run map for every genome position
	}
	
}
