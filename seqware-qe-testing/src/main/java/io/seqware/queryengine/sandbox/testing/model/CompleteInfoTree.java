package io.seqware.queryengine.sandbox.testing.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CompleteInfoTree {
	Map<String,Map<String,ArrayList<Map<String,Object>>>> completetype;
	
	public CompleteInfoTree(){
		completetype = new HashMap<String,Map<String,ArrayList<Map<String,Object>>>>();
	}
	
	public Map<String,ArrayList<Map<String,Object>>> getFirstMap(String key){
		return completetype.get(key);
	}
	
	public Set<String> getKeySet(){
		return completetype.keySet();
	}
	
	public Map<String, ArrayList<Map<String, Object>>> put(String key, Map<String,ArrayList<Map<String,Object>>> value){
		return completetype.put(key, value);
	}
	
	public Map<String,ArrayList<Map<String,Object>>> get(String key){
		return completetype.get(key);
	}
}
