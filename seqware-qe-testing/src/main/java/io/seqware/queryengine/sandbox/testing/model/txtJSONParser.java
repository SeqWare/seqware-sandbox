package io.seqware.queryengine.sandbox.testing.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

public class txtJSONParser {
	
	//Initialize query stores to dump queries from input JSON
	HashMap<String, String> FEATURE_MAP_QUERY = new HashMap<String,String>();
	HashMap<String, String> FEATURE_SET_MAP_QUERY = new HashMap<String,String>();
	HashMap<String, String> REGION_MAP_QUERY = new HashMap<String,String>();
	
	public txtJSONParser(String queryJSON){
		
		JSONObject jsonObOuter = new JSONObject(queryJSON);
		JSONArray regionArray;
		Iterator<String> OutterKeys = jsonObOuter.keys();
	
		//READ THE JSON INPUT FILE
		/**	"OutKey":
		{
			"InKey": "jsonObInner.get(InKey)"
		}*/
		while (OutterKeys.hasNext()){
			String OutKey = OutterKeys.next();
			if (jsonObOuter.get(OutKey) instanceof JSONObject){
				JSONObject jsonObInner = jsonObOuter.getJSONObject(OutKey);
				Iterator<String> InnerKeys = jsonObInner.keys();
				
				while (InnerKeys.hasNext()){
					String InKey = InnerKeys.next();
					
					if (OutKey.equals("feature_sets")){
						FEATURE_SET_MAP_QUERY.put(InKey.toString(), 
										jsonObInner.getString(InKey));
					}
					
					if (OutKey.equals("features")){
						FEATURE_MAP_QUERY.put(InKey.toString(), 
										jsonObInner.getString(InKey));
					}
				}
			} else if (jsonObOuter.get(OutKey) instanceof JSONArray){
				JSONArray jsonArInner = jsonObOuter.getJSONArray(OutKey);
					if(OutKey.equals("regions")){
						regionArray = jsonObOuter.getJSONArray(OutKey);
						if (regionArray.length() == 0){
							REGION_MAP_QUERY.put(".", "any");
						} else {
							for (int i=0; i< regionArray.length(); i++){
								String region = regionArray
												.get(i)
												.toString();
								
								if (region.contains(":") == false){
									
									//i.e. selects "22" from "chr22"
									String chromosomeID = region.substring(
											region.indexOf("r")+1,
											region.length());
									
									REGION_MAP_QUERY.put(chromosomeID.toString(), 
													".");
									
								} else if (region.contains(":") == true){
									
									//i.e. selects "22" from "chr22:1-99999"
									String chromosomeID = region.substring(
											region.indexOf("r")+1,
											region.indexOf(":"));
									
									String range = region.substring(
											region.indexOf(":")+1,
											region.length());
									
									REGION_MAP_QUERY.put(chromosomeID.toString(), 
													range.toString());
								}
							}
						}
					}
			}
		}
	}

	public txtJSONParser(){};
	
	public HashMap<String, String> getFEATURE_MAP_QUERY(){
		return FEATURE_MAP_QUERY;
	}
	
	public HashMap<String, String> getFEATURE_SET_MAP_QUERY(){
		return FEATURE_SET_MAP_QUERY;
	}
	
	public HashMap<String, String> getREGION_MAP_QUERY(){
		return REGION_MAP_QUERY;
	}

	public String getJSONText(String filePath) throws IOException{
		BufferedReader in;
        String line;
        String temp = new String();
        in = new BufferedReader(new FileReader(filePath));
        while ((line = in.readLine()) != null){
        	temp = temp.concat(line);
        }
        return temp;
	}
}
