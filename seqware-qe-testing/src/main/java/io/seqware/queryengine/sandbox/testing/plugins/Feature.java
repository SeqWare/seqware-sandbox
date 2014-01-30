/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing.plugins;

import io.seqware.queryengine.sandbox.testing.utils.Global;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author boconnor
 */
public class Feature implements FeaturePluginInterface{

	@Override
	public void map(long position,
			Map<FeatureSet, Collection<Feature>> features,
			Map<String, String> output) {
			Iterator featureIter = features.entrySet().iterator();
			while (featureIter.hasNext()){
				Map.Entry region_InfoPair = (Map.Entry)featureIter.next();
				
			}
	}

	@Override
	public void reduce(String key, Iterable<String> values,
			Map<String, String> output) {
		// TODO Auto-generated method stub
		
	}
  
}
