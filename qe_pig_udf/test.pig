REGISTER target/qe_pig_udf-1.0-SNAPSHOT.jar;
raw = LOAD 'hbase://BATMAN.hbaseTestTable_v2.Feature.hg823907118' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage
('d:*', '-loadKey true -limit 5 -caster org.apache.pig.backend.hadoop.hbase.QEBinaryConverter'); 
dump raw;
