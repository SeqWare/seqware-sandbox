package io.seqware.queryengine.sandbox.testing.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMProgramRecord;
import net.sf.samtools.SAMReadGroupRecord;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import edu.berkeley.cs.amplab.adam.avro.ADAMRecord;

public class ReadSearch {
    HashMap<String, String> readSetQuery; 
    HashMap<String, String> readsQuery;
    HashMap<String, String> regionsQuery;
    
    public ReadSearch(HashMap<String, String> readSet, HashMap<String, String> reads, HashMap<String, String> regions) {
      readSetQuery = readSet;
      readsQuery = reads;
      regionsQuery = regions;
    }
    
    public ArrayList<ADAMRecord> adamSearch( ArrayList<ADAMRecord> adamList ) {
      ArrayList<ADAMRecord> resultADAMList = new ArrayList<ADAMRecord>();
      for (ADAMRecord a: adamList) {
        if (!readSetQuery.isEmpty()) {
          boolean readSetMatch = true;
          for (Entry<String, String> entry : readSetQuery.entrySet()) {
            if (entry.getKey().equals("SQ.SN") && !(entry.getValue().equals(a.getReferenceName()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.CN") && !(entry.getValue().equals(a.getRecordGroupSequencingCenter()))) {
              readSetMatch = false; break;
            } 
            if (entry.getKey().equals("RG.DS") && !(entry.getValue().equals(a.getRecordGroupDescription()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.DT") && !(entry.getValue().equals(a.getRecordGroupRunDateEpoch()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.FO") && !(entry.getValue().equals(a.getRecordGroupFlowOrder()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.KS") && !(entry.getValue().equals(a.getRecordGroupKeySequence()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.LB") && !(entry.getValue().equals(a.getRecordGroupLibrary()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.PI") && !(entry.getValue().equals(a.getRecordGroupPredictedMedianInsertSize()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.PL") && !(entry.getValue().equals(a.getRecordGroupPlatform()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.PU") && !(entry.getValue().equals(a.getRecordGroupPlatformUnit()))) {
              readSetMatch = false; break;
            }
            if (entry.getKey().equals("RG.SM") && !(entry.getValue().equals(a.getRecordGroupSample()))) {
              readSetMatch = false; break;
            }
          }
          if (!readSetMatch)
            continue;
        }
        if (!regionsQuery.isEmpty()) {
          
        }
        if (!readsQuery.isEmpty()) {
          boolean readsMatch = true;
          for (Entry<String, String> entry : readsQuery.entrySet()) {
            if (entry.getKey().equals("rname") && !(entry.getValue().equals(a.getReferenceName()))) {
              readsMatch = false; break;
            }
            if (entry.getKey().equals("mapq") && !(entry.getValue().equals(a.getMapq()))) {
              readsMatch = false; break;
            }
            if (entry.getKey().equals("cigar") && !(entry.getValue().equals(a.getCigar()))) {
              readsMatch = false; break;
            }
            if (entry.getKey().equals("seq") && !(entry.getValue().equals(a.getSequence()))) {
              readsMatch = false; break;
            }
          }
          if (!readsMatch)
            continue;
        }
        resultADAMList.add(a);
      }
      return resultADAMList;
    }
    
    public SAMFileWriter bamSearch( SAMFileReader samReader, String output ) {
      File outputBam = new File(output);
      SAMFileWriterFactory samFactory = new SAMFileWriterFactory();      
      SAMFileHeader bamHeader = samReader.getFileHeader();
      SAMFileWriter bfw = samFactory.makeSAMWriter(bamHeader, true, outputBam);
      
      List<SAMReadGroupRecord> bamReadGroups = bamHeader.getReadGroups();
      
      String querySampleIds = new String();
      if (!readSetQuery.isEmpty()) {
        if (!readSetQuery.get("sample").isEmpty()) {
          querySampleIds = readSetQuery.get("sample"); 
          readSetQuery.remove("sample");
        }
      }
      // Query for the Sample IDs
      if (!querySampleIds.isEmpty()) {
        boolean sampleMatch = false; 
        for (SAMReadGroupRecord rec: bamReadGroups) {          
          if (rec.getAttribute("SM").equals(querySampleIds)) {
            sampleMatch = true;
          }
        }         
        if (!sampleMatch) {
          return bfw;
        }
      }
      
      // Organize and query read_set tags
      SAMSequenceDictionary seqDict = bamHeader.getSequenceDictionary();
      List<SAMProgramRecord> bamPGRecords = bamHeader.getProgramRecords();
      List<String> bamComments = bamHeader.getComments();
      // Not completely working for all the tags (there's lots of them)
      if (!readSetQuery.isEmpty()) {
        boolean readSetMatch = true;
        
        for (Entry<String, String> entry : readSetQuery.entrySet()) {
          //HD Line
          if (entry.getKey().equals("HD.SO") || entry.getKey().equals("HD.VN")) {
            if (!bamHeader.getAttribute(entry.getKey()).equals(entry.getValue())) {
              //No match
              readSetMatch = false;
              break;
            }
          }
          //SQ - SequenceDictionary
          if (entry.getKey().startsWith("SQ")) {
            boolean seqDictMatch = false;
            for (SAMSequenceRecord seq: seqDict.getSequences()) {
              //SN
              if (entry.getKey().equals("SQ.SN")) {
                if (seq.getSequenceName().equals(entry.getValue())) {
                  seqDictMatch = true;
                  break;
                }
              }
              //LN
              if (entry.getKey().equals("SQ.LN"))
                if (seq.getSequenceLength() != Integer.parseInt(entry.getValue())) {
                  seqDictMatch = true;
                  break;
                }
              //AS
              if (entry.getKey().equals("SQ.AS"))
                if (seq.getAssembly().equals(entry.getValue())) {
                  seqDictMatch = true;
                  break;
                }
              //M5
              if (entry.getKey().equals("SQ.M5"))
                if (seq.MD5_TAG.equals(entry.getValue())) {
                  seqDictMatch = true;
                  break;
                }
              //SP
              if (entry.getKey().equals("SQ.SP"))
                if (seq.getSpecies().equals(entry.getValue())) {
                  seqDictMatch = true;
                  break;
                }
              //UR
              if (entry.getKey().equals("SQ.UR")) {
                if (seq.URI_TAG.equals(entry.getValue())) {
                  seqDictMatch = true;
                  break;
                }
              }
            }
            if (!seqDictMatch) {
              readSetMatch = false; break;
            }
          }
          //Read Groups
          if(entry.getKey().startsWith("RG")) {
            boolean rgMatch = false;
            for (SAMReadGroupRecord rg: bamReadGroups) {
              //ID
              if (entry.getKey().equals("RG.ID")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.CN")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.DS")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.DT")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.FO")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.KS")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.LB")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.PG")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.PI")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.PL")) {
                rgMatch = true; break;
              }
              if (entry.getKey().equals("RG.PU")) {
                rgMatch = true; break;
              }
            }
            if (!rgMatch) {
              readSetMatch = false; break;
            }
          }
          //Program
          if (entry.getKey().startsWith("PG")) {
            boolean pgMatch = false;
            for (SAMProgramRecord pg: bamPGRecords) {
              if (entry.getKey().equals("PG.ID")) {
                pgMatch = true; break;
              }
              if (entry.getKey().equals("PG.PN")) {
                pgMatch = true; break;
              }
              if (entry.getKey().equals("PG.CL")) {
                pgMatch = true; break;
              }
              if (entry.getKey().equals("PG.PP")) {
                pgMatch = true; break;
              }
              if (entry.getKey().equals("PG.DS")) {
                pgMatch = true; break;
              }
              if (entry.getKey().equals("PG.VN")) {
                pgMatch = true; break;
              }
            }
            if (!pgMatch) {
              readSetMatch = false; break;
            }
          }
          if (entry.getKey().equals("CO")) {
            boolean coMatch = false;
            for (String co: bamComments) {
              if (entry.getValue().equals(co)) {
                coMatch = true; break;
              }
            }
            if (!coMatch) {
              readSetMatch = false;
              break;
            }
          }
        }
        
        if (!readSetMatch) {
          return bfw;
        }
      }
      
      // Narrow down Regions -- currently not working, need to find out how to 
      //   work with the API for this query
      /*
      if (regionArray.length() !=0) {
        
        for (int i=0; i < regionArray.length(); i++) {
          String region = regionArray.get(i).toString();
          if (region.contains(":")) {
            //int regionIndex = Integer.parseInt(region.substring(3, region.indexOf(":")));
            int regionStart = Integer.parseInt(region.substring(region.indexOf(":") + 1, region.indexOf("-")));
            int regionEnd = Integer.parseInt(region.substring(region.indexOf("-") + 1, region.length()));
            
            SAMRecordIterator iter = samReader.queryOverlapping(region, regionStart, regionEnd);
            if (!iter.hasNext()) {
              //if empty, return
              htmlReport.insertBeforeEnd(htmlReport.getElement(htmlReport.getDefaultRootElement(), StyleConstants.NameAttribute, HTML.Tag.BODY), "<p>No Results.</p></div>");
              rt.setState(ReturnValue.SUCCESS);
              return rt;
            }
            System.out.println(iter.next().toString());
            iter.close();
          }
        }
      }
      */

      // Check Attributes -- a little redundant here..
      //   to do: finish for the rest of the read attributes
      if (!readsQuery.isEmpty()) {
        boolean qname = false;
        boolean flag = false;
        boolean rname = false;
        boolean pos = false;
        boolean mapq = false;
        boolean cigar = false;
        boolean rnext = false;
        boolean pnext = false;
        boolean tlen = false;
        boolean seq = false;
        boolean qual = false;
        
        for (Entry<String, String> e : readsQuery.entrySet()) {
          switch (e.getKey()) {
            case "qname": qname = true; break;
            case "flag": flag = true; break;
            case "rname": rname = true; break;
            case "pos": pos = true; break;
            case "mapq": mapq = true; break;
            case "cigar": cigar = true; break;
            case "rnext": rnext = true; break;
            case "pnext": pnext = true; break;
            case "tlen": tlen = true; break;
            case "seq": seq = true; break;
            case "qual": qual = true; break;
          }
        }
        
        for (SAMRecord r: samReader) {
          if (qname && !readsQuery.get("qname").equals(r.getReadName())) {
              continue;
          } 
          if (flag && !readsQuery.get("flag").equals(r.getFlags())) {
              continue;
          }
          if (rname && !readsQuery.get("rname").equals(r.getReferenceName())) {
              continue;
          }
          if (pos && !readsQuery.get("pos").equals(r.getAlignmentStart())) {
              continue;
          }
          if (mapq && !readsQuery.get("mapq").equals(r.getMappingQuality())) {
              continue;
          }
          if (cigar && !readsQuery.get("cigar").equals(r.getCigarString())) {
              continue;
          }
          if (seq && !readsQuery.get("seq").equals(r.getReadString())) {
              continue;
          }
          if (qual && !readsQuery.get("qual").equals(r.getBaseQualityString())) {
              continue;
          }
          if (rnext && !readsQuery.get("rnext").equals(r.getMateReferenceName())) {
              continue;
          }
          if (pnext && !readsQuery.get("pnext").equals(r.getMateReferenceIndex())) {
              continue;
          }
          if (tlen && !readsQuery.get("tlen").equals(r.getReadLength())) {
              continue;
          }
          bfw.addAlignment(r);
        }
      } else {
        for (SAMRecord r: samReader) {
          bfw.addAlignment(r);
        }
      }
      // Finally, write a .bam file with result of query
      // Also write to htmlReport
      bfw.close();
      return bfw;
    }
    
}
