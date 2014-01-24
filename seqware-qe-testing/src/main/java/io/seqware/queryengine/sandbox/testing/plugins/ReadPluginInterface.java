/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing.plugins;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author dyuen
 */
public interface ReadPluginInterface extends PluginInterface {


    public void map(long position, Map<ReadSet, Collection<Reads>> reads, Map<String, String> output);


    public void reduce(String key, Iterable<String> values, Map<String, String> output);
    
}
