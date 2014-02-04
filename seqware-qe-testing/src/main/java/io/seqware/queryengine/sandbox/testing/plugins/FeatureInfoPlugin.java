package io.seqware.queryengine.sandbox.testing.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.junit.Test;

import io.seqware.queryengine.sandbox.testing.plugins.Feature;
import io.seqware.queryengine.sandbox.testing.plugins.FeatureSet;
import io.seqware.queryengine.sandbox.testing.model.txtJSONParser;
import io.seqware.queryengine.sandbox.testing.model.VCFReader;

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
	
	public Map<String,String> applyMap(String filepath, Map<FeatureSet, Collection<Feature>> features) throws IOException{
		VCFReader inputVCF = 
				new VCFReader(filepath);
			
			Map<String,String> output = 
				new HashMap<String,String>();
			
		Iterator<VariantContext> vcfIter = inputVCF.getVCFIterator();
		String position;

		FeatureInfoPlugin  
		
		while(vcfIter.hasNext()){
			VariantContext variant = vcfIter.next();
			position = Integer.toString(variant.getStart());
			
		}
		
		
		return null;
	}

	//Apply filter as determined by the JSON query
	public void getFilteredFiles(String Directory, String queryJSON, String OutputFilePath) throws IOException{
		String InputFilePath;
		File filedir = 
				new File(Directory);
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
	public Map<FeatureSet, Collection<Feature>> makeMapInput(String Directory) throws IOException{
		File filedir = 
				new File(Directory);
		
		Map<FeatureSet,Collection<Feature>> MapInput = 
				new HashMap<FeatureSet,Collection<Feature>>();
		
		ArrayList<Feature> Features = 
				new ArrayList<Feature>();
		
		String line;
		for (File child : filedir.listFiles()){
			
			FeatureSet featureset = 
					new FeatureSet(child);
		
			BufferedReader in = 
					new BufferedReader(
							new FileReader(child.getAbsolutePath()));
			
			while((line = in.readLine()) != null){
				Feature feature = 
						new Feature(line);
				Features.add(feature);
			}
			
			MapInput.put(featureset, Features);
		}
		return MapInput;
	}


	
}
