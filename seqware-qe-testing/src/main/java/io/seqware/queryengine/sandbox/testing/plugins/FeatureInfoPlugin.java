package io.seqware.queryengine.sandbox.testing.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import io.seqware.queryengine.sandbox.testing.plugins.Feature;
import io.seqware.queryengine.sandbox.testing.plugins.FeatureSet;
import io.seqware.queryengine.sandbox.testing.model.txtJSONParser;
import io.seqware.queryengine.sandbox.testing.model.CompleteInfoTree;

public class FeatureInfoPlugin implements FeaturePluginInterface{
	
	//These will take the featuresInfoTree of VCF data from different files and perform mapreduce
	@Override
	public void map(long position,
			Map<FeatureSet, Collection<Feature>> features,
			Map<String, String> output) {
	}
	
	@Override
	public void reduce(String key, Iterable<String> values,
			Map<String, String> output) {
	}
	
	//Build featuresInfoTree for use in MapReduce
	public void getFilteredFiles(String directory, String queryJSON, String OutputFilePath) throws IOException{
		String InputFilePath;
		File filedir = 
				new File(directory);
		Feature FeatureID = 
				new Feature();
		
		txtJSONParser JParse = new txtJSONParser(queryJSON);
		HashMap<String, String> fsmapq = JParse.getFEATURE_SET_MAP_QUERY();
		
		//Generate Complete Map of FeatureSetId and INFO
		for (File child : filedir.listFiles()){
			
			InputFilePath = child.getAbsolutePath();
			String filename = child
					.getName()
					.substring(0, child.getName().indexOf("."));
			
			if (FilenameUtils.getExtension(InputFilePath).equals("vcf") 
					&& ((fsmapq.keySet().contains(filename)) || (fsmapq.size() ==0))){ 
				//Write this to temp file output
				FeatureID.readVCFinfo(InputFilePath, queryJSON, OutputFilePath, filename); //Read INFO fields in VCF only

			}
			
		}
	}
	
	
	//TODO Create function to go through input vcf file and implement map function
	
	
}
