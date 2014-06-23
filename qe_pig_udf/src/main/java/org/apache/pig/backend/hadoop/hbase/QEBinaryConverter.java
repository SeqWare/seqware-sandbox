/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.pig.backend.hadoop.hbase;

import com.github.seqware.queryengine.impl.ProtobufSerialization;
import com.github.seqware.queryengine.model.Feature;
import com.github.seqware.queryengine.model.Tag;
import com.github.seqware.queryengine.model.impl.FeatureList;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pig.ResourceSchema;

/**
 *
 * @author dyuen
 */
public class QEBinaryConverter extends HBaseBinaryConverter {
    
    public QEBinaryConverter(){
        
    }
    
    private ProtobufSerialization deserializer = new ProtobufSerialization();
    
    @Override
    public Map<String, Object> bytesToMap(byte[] b) throws IOException {
        System.out.println("Called custom converter with " + b.toString());
        if (b.length == 0){
            return new HashMap<String, Object>();
        }
        FeatureList fList = deserializer.deserialize(b, FeatureList.class);
        // convert each into a map and dump into a map
        Map<String, Object> result = new HashMap<String, Object>();
        int count = 0;
        for(Feature f : fList.getFeatures()){
            result.put(count + ".phase", f.getPhase());
            result.put(count + ".score", f.getScore());
            result.put(count + ".seqid", f.getSeqid());
            result.put(count + ".source", f.getSource());
            result.put(count + ".start", f.getStart());
            result.put(count + ".stop", f.getStop());
            result.put(count + ".type", f.getType());
            
            for(Tag t : f.getTags()){
                result.put(count +".tag." + t.getKey(), t.getValue());
            }
            count++;
        }
        return result;
        
    }
    
       @Override
    public Map<String, Object> bytesToMap(byte[] b, ResourceSchema.ResourceFieldSchema fieldSchema) throws IOException {
        return bytesToMap(b);
    }
}