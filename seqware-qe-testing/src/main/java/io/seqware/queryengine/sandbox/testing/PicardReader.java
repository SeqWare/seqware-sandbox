package io.seqware.queryengine.sandbox.testing;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMProgramRecord;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

import org.json.JSONObject;


public class PicardReader {

  /** parseBAM
   *    Attempts to parse the BAM file.
   *    Creates a JSON object output for generalized database entries.
   *    Take only required values.
   * @param bamfile
   */
  public JSONObject parseBAM(File bamfile) {
    JSONObject parsedBAM = new JSONObject();
    if (!bamfile.getName().endsWith(".bam")) {
      System.out.println("Error: Not a BAM file");
      return parsedBAM;
    }
    
    try {
      SAMFileReader fileReader = new SAMFileReader(bamfile);
      // ReadSet: Header of SAM file
      SAMFileHeader header = fileReader.getFileHeader();
      //@HD items: Version, SO - Sort Order
      String version = header.getVersion();
      String sortOrder = header.getSortOrder().toString();
      //@SQ Reference Sequence Dictionary
      SAMSequenceDictionary seqDictionary = header.getSequenceDictionary();
      //@RG ReadGroup
      List<SAMReadGroupRecord> readGroups = header.getReadGroups();
      //@PG Program
      List<SAMProgramRecord> programRecords = header.getProgramRecords();
      //@CO Comments
      List<String> comments = header.getComments();
      JSONObject readSet = new JSONObject();
      readSet.put("version", version);
      readSet.put("sortOrder", sortOrder);
      
      // Iterate over Sequence Dictionary
      if (!seqDictionary.isEmpty()) {
        JSONObject jsonSeqDict = new JSONObject();
        List<SAMSequenceRecord> seqRecords = seqDictionary.getSequences();
        for (SAMSequenceRecord rec: seqRecords) {
          jsonSeqDict.put("index" + rec.getSequenceIndex(), rec.getSequenceName());
        }
        readSet.put("sequenceDictionary", jsonSeqDict);
      }
      
      
      if (!readGroups.isEmpty()) {
        readSet.put("readGroups", readGroups.toString());
      }
      
      // Get Alignment Reads section -- Iterate over SAMRecords
      // Need to refactor
      JSONObject reads = new JSONObject();
      int limit = 0;
      for (SAMRecord samr: fileReader) {
        JSONObject singleRead = new JSONObject();
        singleRead.put("flag", samr.getFlags());
        singleRead.put("rname", samr.getReferenceName());
        singleRead.put("pos", samr.getMateAlignmentStart());
        singleRead.put("mapq", samr.getMappingQuality());
        singleRead.put("cigar", samr.getCigarString() );
        singleRead.put("rnext", samr.getMateReferenceName());
        singleRead.put("pnext", samr.getMateAlignmentStart());
        //singleRead.put("tlen", samr.get)
        singleRead.put("seq", samr.getReadString());
        //singleRead.put("qual", samr.get);
        reads.put(samr.getReadName(), singleRead);
        singleRead = null;
        if (limit > 100) {
          break;
        }
        limit = limit + 1;
      }
      
      parsedBAM.put("readsets", readSet);
      parsedBAM.put("reads", reads);
      
      PrintWriter out = new PrintWriter("testjson.json");
      out.print(readSet.toString());
      out.close();
      //System.out.println(readSet.toString());
      // Cleanup
      fileReader.close();
    } catch (Exception ex) {
      System.out.println("PicardReader.parseBAM Error: " + ex.getMessage());
      return parsedBAM;
    }
    return parsedBAM;
  }
  
  public void bitwiseParseBAM(File bamfile) {
    JSONObject bitwiseBAM = new JSONObject();
    if (!bamfile.getName().endsWith(".bam")){
      System.out.println("Error: Not a BAM file");
    }
    try {
      SAMFileReader fileReader = new SAMFileReader(bamfile);
    } catch (Exception ex) {
      System.out.println("Error parsing file");
    }
    
  }
  
  /** parseSAM
   *    Attempts to parse the requested SAM file.
   *    Creates a JSON object output for generalized database entries.
   * @param samfile
   */
  public JSONObject parseSAM(File samfile) {
    JSONObject parsedSAM = new JSONObject();
    if (!samfile.getName().endsWith(".sam")) {
      System.out.println("Error: Not a SAM file");
      return parsedSAM;
    }
    try {
      SAMFileReader fileReader = new SAMFileReader(samfile);
      
      // ReadSet: Header of SAM file
      SAMFileHeader header = fileReader.getFileHeader();
      //@HD items: Version, SO - Sort Order
      String version = header.getVersion();
      String sortOrder = header.getSortOrder().toString();
      //@SQ Reference Sequence Dictionary
      SAMSequenceDictionary seqDictionary = header.getSequenceDictionary();
      //@RG ReadGroup
      List<SAMReadGroupRecord> readGroups = header.getReadGroups();
      //@PG Program
      List<SAMProgramRecord> programRecords = header.getProgramRecords();
      //@CO Comments
      List<String> comments = header.getComments();
      JSONObject readSet = new JSONObject();
      readSet.put("version", version);
      readSet.put("sortOrder", sortOrder);
      
      // Iterate over Sequence Dictionary
      if (!seqDictionary.isEmpty()) {
        JSONObject jsonSeqDict = new JSONObject();
        List<SAMSequenceRecord> seqRecords = seqDictionary.getSequences();
        for (SAMSequenceRecord rec: seqRecords) {
          jsonSeqDict.put("index" + rec.getSequenceIndex(), rec.getSequenceName());
        }
        readSet.put("sequenceDictionary", jsonSeqDict);
      }
      
      
      if (!readGroups.isEmpty()) {
        readSet.put("readGroups", readGroups.toString());
      }
      
      // Get Alignment Reads section -- Iterate over SAMRecords
      JSONObject reads = new JSONObject();
      int limit = 0;
      for (SAMRecord samr: fileReader) {
        JSONObject singleRead = new JSONObject();
        singleRead.put("flag", samr.getFlags());
        singleRead.put("rname", samr.getReferenceName());
        singleRead.put("pos", samr.getMateAlignmentStart());
        singleRead.put("mapq", samr.getMappingQuality());
        singleRead.put("cigar", samr.getCigarString() );
        singleRead.put("rnext", samr.getMateReferenceName());
        singleRead.put("pnext", samr.getMateAlignmentStart());
        //singleRead.put("tlen", samr.get)
        singleRead.put("seq", samr.getReadString());
        //singleRead.put("qual", samr.get);
        reads.put(samr.getReadName(), singleRead);
        singleRead = null;
        if (limit > 10) {
          break;
        }
        limit = limit + 1;
      }
      parsedSAM.put("readsets", readSet);
      parsedSAM.put("reads", reads);
      
      PrintWriter out = new PrintWriter("testjson.json");
      out.print(readSet.toString());
      out.close();
      //System.out.println(readSet.toString());
      // Cleanup
      fileReader.close();
    } catch (Exception ex) {
      System.out.println("PicardReader.parseSAM Error: " + ex.getMessage());
      return parsedSAM;
    }
    return parsedSAM;
  }
  
  // Test Method to get outputs
  public JSONObject parseTest(File samfile) {
    JSONObject output = new JSONObject();
    try {
      output = parseBAM(samfile);
      System.out.println("Success?");
    } catch (Exception ex) {
      System.out.println("An Error has occured while parsing the file.");
    }
    return output;
  }
}
