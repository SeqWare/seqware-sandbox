/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing.plugins;

import java.io.File;

/**
 *
 * @author boconnor
 */
public class FeatureSet{
	String FeatureSet;
	
	public FeatureSet(File filteredFile){
		FeatureSet = filteredFile.getName()
				.substring(0, filteredFile.getName().indexOf("."));
	}
	
	public String getString(){
		return FeatureSet;
	}

}
