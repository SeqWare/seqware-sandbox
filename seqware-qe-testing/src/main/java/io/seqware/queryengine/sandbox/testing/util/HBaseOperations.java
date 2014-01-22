package io.seqware.queryengine.sandbox.testing;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.json.JSONObject;

public class HBaseOperations {
 
  public static int queryReads() throws Exception {
    return 2;
  }
  
  /** loadReads
   * 
   * @param jsonFile
   * @return
   * @throws Exception
   */

  public static int loadFeatures() throws Exception {    
    return 2;
  }
  

  public static int loadReads(JSONObject jsonFile) throws Exception {
    // to do: Move to constructor of Object
    Configuration hConf = HBaseConfiguration.create();
    HBaseAdmin admin = new HBaseAdmin(hConf);
    
    // Create a Table in HBase connection
    HTableDescriptor readsTable = new HTableDescriptor(Bytes.toBytes("test"));
    readsTable.addFamily(new HColumnDescriptor("test")); //Test columnfamily
    admin.createTable(readsTable);
    
    // Connect to the table
    HTable hTable = new HTable(hConf, "test");
    Put put = new Put(Bytes.toBytes("rowkey")); // Take rowkey from JSON file or HashMap    
    put.add(Bytes.toBytes("test"), Bytes.toBytes("qualifier"), Bytes.toBytes("value")); //Qualifier: UUID for Readset, value: a Read
    hTable.put(put);
    
    // Close connections
    hTable.close();
    admin.close();
    return 2;
  }
  
  /** Mainly to test an HBase Connection. Creates a Table, puts an entry, disables, and drops a table.
   * 
   * @return
   * @throws Exception
   */
  public static int test() throws Exception {
    Configuration hConf = HBaseConfiguration.create();
    HBaseAdmin admin = new HBaseAdmin(hConf);

    // Test connection with put and deleted row
    HTableDescriptor testTable = new HTableDescriptor(Bytes.toBytes("test"));
    testTable.addFamily(new HColumnDescriptor("test_column_family"));
    admin.createTable(testTable);
    HTable hTable = new HTable(hConf, "test");
    
    // New Data Entry
    Put put = new Put(Bytes.toBytes("test_row"));
    put.add(Bytes.toBytes("test_column_family"), Bytes.toBytes("some_qualifier"), Bytes.toBytes("some_value"));
    hTable.put(put);
    
    // Create new Scan
    Scan scan = new Scan();
    scan.addColumn(Bytes.toBytes("test_column_family"), Bytes.toBytes("some_qualifier"));
    ResultScanner scanner = hTable.getScanner(scan);
    hTable.close();
    
         
    // Query tables
    String[] tableNames = admin.getTableNames();
    for (String s: tableNames) {
      System.out.println("Table: " + s);
    }
    
    // Query Rows
    for (Result rr = scanner.next(); rr != null; rr = scanner.next() ) {
      System.out.println("Found row: " + rr );
    }
    scanner.close();
    
    // Disable and Drop Table
    admin.disableTable("test");
    admin.deleteTable("test");
    admin.close();
    return 0;
  }
}
