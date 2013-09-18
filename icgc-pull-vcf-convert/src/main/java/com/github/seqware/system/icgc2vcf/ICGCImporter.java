package com.github.seqware.system.icgc2vcf;

import com.github.seqware.queryengine.factory.SWQEFactory;
import com.github.seqware.queryengine.model.FeatureSet;
import com.github.seqware.queryengine.model.Tag;
import com.github.seqware.queryengine.system.ReferenceCreator;
import com.github.seqware.queryengine.system.importers.SOFeatureImporter;
import com.github.seqware.queryengine.util.SGID;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;

public class ICGCImporter {
  
  //String URL = "https://portal.dcc.icgc.org/api/download/info/prod-06e-32-22";
  public static String URL = "https://portal.dcc.icgc.org/api/download/info/release_14";

  public static void main(String[] args) {
    System.out.println("Dowloading files");
    List<File> downloadICGCFiles = downloadICGCFiles();
    System.out.println("Files downloaded to: " + downloadICGCFiles.get(0).getParent());
    List<File> unzippedFiles = unzipFiles(downloadICGCFiles);
    System.out.println("Files unzipped to: " + unzippedFiles.get(0).getParent());
    // convert files to VCF format
    Map<String, File> convertedFiles = convertFiles(unzippedFiles);
    // printout file locations
    System.out.println("Files converted to unsorted VCF and saved to: " + unzippedFiles.get(0).getParent());
    String loadName = "ICGC_load";
    if (args.length > 0) {
      loadName = args[0];
    }
    Random rand = new Random();
    // import to hbase
    // create a random reference (and thus a new feature table) for testing
    String randomReference = "hg19_"+ loadName + "_" + Math.abs(rand.nextInt());
    System.out.println("Attaching feature sets to reference: " + randomReference);
    ReferenceCreator.main(new String[]{randomReference});
    System.out.println("Importing VCF files into HBase: ");
    for (Entry<String, File> file : convertedFiles.entrySet()) {
      SGID sgid = SOFeatureImporter.runMain(new String[]{"-i", file.getValue().getAbsolutePath(), "-r", randomReference, "-w", "VCFVariantImportWorker", "-b", "100000"});
      System.out.println("Donor: " + file.getKey() + " file: " + file.getValue().getName() + " imported as feature set " + sgid.getRowKey());
      // double-check feature set level tags
      FeatureSet atomBySGID = SWQEFactory.getQueryInterface().getLatestAtomBySGID(sgid, FeatureSet.class);
      for (Tag tag : atomBySGID.getTags()) {
        System.out.println("Tagged with " + tag.getKey() + tag.getPredicate() + tag.getValue());
      }
    }
  }

  /**
   * This will return a map of icgc_donor_id to files, one file/feature set per
   * donor
   *
   * @param files
   * @return
   */
  public static Map<String, File> convertFiles(List<File> files) {
    Map<String, File> resultFiles = new HashMap<String, File>();
    try {
      File createTempDir = Files.createTempDir();
      for (File file : files) {
        System.out.println("Converting to VCF: " + file.getAbsolutePath());
        List<String> readLines = FileUtils.readLines(file);
        List<String> header = Arrays.asList(readLines.get(0).split("\t"));
        // map from icgc_donor_id -> icgc_mutation_id -> line in file
        Map<String, Map<String, List<String[]>>> mappedLines = new HashMap<String, Map<String, List<String[]>>>();
        for (String line : readLines) {
          if (line.contains("icgc_mutation_id")) {
            continue;
          }
          String[] columns = line.split("\t");
          String icgc_donor_id = columns[header.indexOf("icgc_donor_id")];
          String icgc_mutation_id = columns[header.indexOf("icgc_mutation_id")];
          if (!mappedLines.containsKey(icgc_donor_id)) {
            mappedLines.put(icgc_donor_id, new HashMap<String, List<String[]>>());
          }
          if (!mappedLines.get(icgc_donor_id).containsKey(icgc_mutation_id)) {
            mappedLines.get(icgc_donor_id).put(icgc_mutation_id, new ArrayList<String[]>());
          }
          mappedLines.get(icgc_donor_id).get(icgc_mutation_id).add(columns);
        }
        // dump to files named by combination of donor and project
        for (Entry<String, Map<String, List<String[]>>> donorCollection : mappedLines.entrySet()) {
          // determine file name and create file
          String project = file.getName().substring(file.getName().indexOf(".") + 1, file.getName().lastIndexOf("."));
          String filename = project + "." + donorCollection.getKey() + ".vcf";
          File outputFile = new File(createTempDir, filename);
          System.out.println("   output VCF: " + outputFile.getAbsolutePath());
          FileUtils.writeStringToFile(outputFile, "##fileformat=VCFv4.1\n", true);
          FileUtils.writeStringToFile(outputFile, "##" + SOFeatureImporter.PRAGMA_QE_TAG_FORMAT + "=project=" + project + "\n", true);
          FileUtils.writeStringToFile(outputFile, "##" + SOFeatureImporter.PRAGMA_QE_TAG_FORMAT + "=donor=" + donorCollection.getKey() + "\n", true);
          FileUtils.writeStringToFile(outputFile, "#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\n", true);
          for (Entry<String, List<String[]>> mutationCollection : donorCollection.getValue().entrySet()) {
            String id = mutationCollection.getKey();
            String chromosome = mutationCollection.getValue().get(0)[header.indexOf("chromosome")];
            String pos = mutationCollection.getValue().get(0)[header.indexOf("chromosome_start")];
            // TODO: fix this mismatch between data formats, icgc data format allows a null reference_genome_allele while VCF requires the allele to be one of A,C,G,T,N    
            String[] mutation = mutationCollection.getValue().get(0)[header.indexOf("mutation")].split(">");
            // TODO: this silly check is because simple_somatic_mutation.LICA-FR.tsv has values of TCATTAAATCTTTAG [... truncated]
            String ref = mutation.length > 0 ? mutation[0] : ".";
            String alt = mutation.length > 1 ? mutation[1] : ".";
            // TODO: not sure if the quality_score (which is often missing) is equivalent to phred (vcf) quality
            String qual = ".";
            // TODO: not sure if we care about filters
            String filter = ".";

            // mutationcollection wide values that are set by tags follow
            String mutation_type = mutationCollection.getValue().get(0)[header.indexOf("mutation_type")];
            mutation_type = mutation_type.contains("single base substitution") ? "SNV" : "INDEL";
            StringBuilder info = new StringBuilder();
            info.append("VarType=").append(mutation_type).append(";");
            // process tags by iterating through lines
            HashMap<String, String> geneIds = new HashMap<String, String>();
            for (String[] line : mutationCollection.getValue()) {
              // TODO: here we would normally pull out stuff from each line for tags, not sure which tags we want, leaving this blank for now
              // for example, consequence_type looks like it can be converted to SNPEFF_EFFECT, but how to we tie-break?
              //info.append("EnsemblGene=").append(line[header.indexOf("gene_affected")]).append(";");
              if (line[header.indexOf("gene_affected")] != null && !"".equals(line[header.indexOf("gene_affected")])) { geneIds.put(line[header.indexOf("gene_affected")], "true"); }
            }
            if (geneIds.keySet().size() > 0) {
              info.append("EnsemblGene=");
              boolean first = true;
              for (String geneId : geneIds.keySet()) {
                if (first) {
                  first = false;
                  info.append(geneId);
                } else {
                  info.append("," + geneId);
                }
              }
            }
            // write final line to file
            FileUtils.writeStringToFile(outputFile, chromosome + "\t" + pos + "\t" + id + "\t" + ref + "\t" + alt + "\t" + qual + "\t" + filter + "\t" + info + "\n", true);
          }
          resultFiles.put(donorCollection.getKey(), outputFile);
        }

      }
    } catch (IOException ex) {
      Logger.getLogger(ICGCImporter.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex);
    }
    return resultFiles;
  }

  public static String downloadIndex(String index) throws IOException {
    URL url = new URL(index);
    InputStream is = url.openStream();  // throws an IOException
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    String line = br.readLine();
    br.close();
    return line;
  }

  public static File downloadFile(String url, File file) throws IOException {
    URL actualURL = new URL("https://portal.dcc.icgc.org/api/download?fn=" + url);
    FileUtils.copyURLToFile(actualURL, file);
    return file;
  }

  public static List<File> downloadICGCFiles() throws JsonSyntaxException {
    // download result files from ICGC API
    List<File> resultFiles = new ArrayList<File>();
    File createTempDir = Files.createTempDir();
    try {
      String line = downloadIndex(URL);
      Gson gson = new Gson();

      Line[] projectLines = gson.fromJson(line, Line[].class);
      for (Line projLine : projectLines) {
        String somatic_mutations = downloadIndex("https://portal.dcc.icgc.org/api/download/info" + projLine.name);
        Gson pgson = new Gson();
        Line[] fileLines = pgson.fromJson(somatic_mutations, Line[].class);
        for (Line fileLine : fileLines) {
          if (fileLine.name.contains("simple_somatic_mutation")) {
            String url2 = "https://portal.dcc.icgc.org/api/download?fn=" + fileLine.name;
            System.out.println("  downloading " + url2);
            File file = new File(createTempDir, fileLine.name.substring(fileLine.name.lastIndexOf("/")));
            resultFiles.add(ICGCImporter.downloadFile(fileLine.name, file));
            // HACK
            //return resultFiles;
          }
        }
      }
    } catch (MalformedURLException mue) {
      Logger.getLogger(ICGCImporter.class.getName()).log(Level.SEVERE, null, mue);
      throw new RuntimeException(mue);
    } catch (IOException ioe) {
      Logger.getLogger(ICGCImporter.class.getName()).log(Level.SEVERE, null, ioe);
      throw new RuntimeException(ioe);
    }
    return resultFiles;
  }

  public static List<File> unzipFiles(List<File> downloadICGCFiles) {
    File createTempDir = Files.createTempDir();
    List<File> unzippedFiles = new ArrayList<File>();
    for (File file : downloadICGCFiles) {
      final GZIPInputStream iStream;
      try {
        iStream = new GZIPInputStream(Files.newInputStreamSupplier(file).getInput());
        InputSupplier<InputStream> inputSupplier = new InputSupplier<InputStream>() {
          public InputStream getInput() throws IOException {
            return iStream;
          }
        };
        File file1 = new File(createTempDir, file.getName().substring(0, file.getName().lastIndexOf(".")));
        Files.copy(inputSupplier, file1);
        unzippedFiles.add(file1);
      } catch (IOException ex) {
        Logger.getLogger(ICGCImporter.class.getName()).log(Level.SEVERE, null, ex);
        throw new RuntimeException(ex);
      }
    }
    return unzippedFiles;
  }

  private static class Line {

    private String name;
    private String type;
    private String size;
    private String date;

    public Line(String name, String type, String size, String date) {
      this.name = name;
      this.type = type;
      this.size = size;
      this.date = date;
    }

    @Override
    public String toString() {
      return name + " " + type + " " + size + " " + date;
    }
  }
}
