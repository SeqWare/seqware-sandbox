REGISTER target/qe_pig_udf-1.0-SNAPSHOT.jar;
REGISTER target/lib/seqware-queryengine-1.0.4-SNAPSHOT.jar;
REGISTER target/lib/protobuf-java-2.4.1.jar;
REGISTER target/lib/protobuf-java-format-1.2.jar;
REGISTER target/lib/kryo-2.19.jar;
raw = LOAD 'hbase://BATMAN.hbaseTestTable_v2.Feature.hg823907118' USING org.apache.pig.backend.hadoop.hbase.HBaseStorage
('d:b9df3490-e7c6-4330-8045-8cb5ea8886b9', '-loadKey true -limit 5 -caster org.apache.pig.backend.hadoop.hbase.QEBinaryConverter') AS (id:bytearray, data:map[]); 
dump raw;
