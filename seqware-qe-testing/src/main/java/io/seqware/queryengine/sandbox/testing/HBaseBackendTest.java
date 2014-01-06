/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.seqware.queryengine.sandbox.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 * @author boconnor
 */
public class HBaseBackendTest implements BackendTest {
  
  private Configuration config;
  private HBaseAdmin admin;

  public HBaseBackendTest() throws MasterNotRunningException {
    config = HBaseConfiguration.create();
    admin = new HBaseAdmin(config);
  }
  
  /*
   * Not supported 
   */
  public String loadFile(String file) {
    String id = UUID.randomUUID().toString();
    try {
      // create table
      /*HTableDescriptor htd = new HTableDescriptor(id);
      HColumnDescriptor hcd = new HColumnDescriptor("f");
      hcd.setCompressionType(Compression.Algorithm.GZ);
      htd.addFamily(hcd);
      admin.createTable(htd);
      byte[] tablename = htd.getName();
      HTableDescriptor[] tables = admin.listTables();
      if(tables.length != 1 && Bytes.equals(tablename, tables[0].getName())) {
        throw new IOException("failed to create table");
      }*/
      // iterate through the BAM file and save features to table
      int max = 10000;
      SAMFileReader inputSam = new SAMFileReader(new File(file));
      inputSam.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
      CloseableIterator<SAMRecord> iter = null;
      try {
        iter = inputSam.iterator();
                //query(contig, start, end, this.containsbamRecord);
        while (iter.hasNext()) {
          SAMRecord rec = iter.next();
          System.out.println(rec.getReferenceName()+":"+rec.getAlignmentStart());
          max--;
          if (max <= 0) { break; }
        }
      } catch (Exception e) {
        throw new IOException(e);
      } finally {
        if (iter != null) {
          iter.close();
        }
        if (inputSam != null) {
      inputSam.close();
    }
      }
      
    } catch (IOException ex) {
      Logger.getLogger(HBaseBackendTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return(id);
  }
  
  public void dumpResult(String id) {
    
  }

  public ArrayList<String> getRange(String featureSetName, String contig, int start, int stop) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public ArrayList<String> intersect(String featureSetName1, String featureSetName2) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /*
   * Not supported really
   */
  public ArrayList<String> mapReducePluginTest(String mrPluginClassName) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public static void main(String[] args) {
    try {
      if (args.length > 0) {
        HBaseBackendTest test = new HBaseBackendTest();
        String id = test.loadFile(args[0]);
        test.dumpResult(id);
      } else {
        System.err.println("USAGE: java HBaseBackendTest bamfile");
      }
    } catch (MasterNotRunningException ex) {
      Logger.getLogger(HBaseBackendTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    
  }
}
