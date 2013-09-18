/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.pig.backend.hadoop.hbase;

import com.github.seqware.queryengine.impl.protobufIO.FeatureListIO;
import com.github.seqware.queryengine.model.Feature;
import com.github.seqware.queryengine.model.Tag;
import com.github.seqware.queryengine.model.impl.FeatureList;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pig.LoadCaster;
import org.apache.pig.ResourceSchema;
import org.apache.pig.backend.hadoop.hbase.HBaseBinaryConverter;
import org.apache.pig.data.DataBag;
import org.apache.pig.data.Tuple;
import org.joda.time.DateTime;

/**
 *
 * @author dyuen
 */
public class QEBinaryConverter implements LoadCaster {
    
    public QEBinaryConverter(){
        
    }
    
    private FeatureListIO io = new FeatureListIO();
    
    @Override
    public Map<String, Object> bytesToMap(byte[] b) throws IOException {
        FeatureList fList = io.byteArr2m(b);
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
    public Boolean bytesToBoolean(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Long bytesToLong(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Float bytesToFloat(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Double bytesToDouble(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DateTime bytesToDateTime(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Integer bytesToInteger(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String bytesToCharArray(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, Object> bytesToMap(byte[] b, ResourceSchema.ResourceFieldSchema fieldSchema) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Tuple bytesToTuple(byte[] b, ResourceSchema.ResourceFieldSchema fieldSchema) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataBag bytesToBag(byte[] b, ResourceSchema.ResourceFieldSchema fieldSchema) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
