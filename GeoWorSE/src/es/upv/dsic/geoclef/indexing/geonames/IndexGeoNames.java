package es.upv.dsic.geoclef.indexing.geonames;

import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import es.upv.dsic.GeoNames.GeoNamesDisambiguator;
import es.upv.dsic.geoclef.GeoWorSE;
import es.upv.dsic.geoclef.indexing.DocumentVector;

import java.io.*;

import java.util.Date;


class IndexGeoNames {
  static final boolean USE_DISAMBIGUATION = false;
  
  public static void indexDir(String dir, String lang) 	{
  	Date start = new Date();
    try {

      String head_fn;
      if(USE_DISAMBIGUATION) head_fn="gp_index";
      else head_fn="nd_gp_index";
      IndexWriterConfig iwc = new IndexWriterConfig(GeoWorSE.LVERSION, new SnowballAnalyzer(GeoWorSE.LVERSION, lang));
      iwc.setOpenMode(OpenMode.CREATE);
      IndexWriter writer = new IndexWriter(FSDirectory.open(new File(head_fn+lang)), iwc);
      
      indexDocs(writer, new File(dir));

      writer.close();

      Date end = new Date();

      System.out.print(end.getTime() - start.getTime());
      System.out.println(" total milliseconds");

    } catch (IOException e) {
      System.out.println(" caught a " + e.getClass() +
       "\n with message: " + e.getMessage());
    }
  }
	
  public static void main(String[] args) throws IOException {
    String usage = "java " + IndexGeoNames.class + " <root_directory>";
//    if (args.length == 0) {
//      System.err.println("Usage: " + usage);
//      System.exit(1);
//    }
    
    GeoWorSE.init();
    GeoNamesDisambiguator.init();
	
    indexDir("/home/datasets/geoclef/data", "English");
    
    GeoWorSE.close();
  }

  public static void indexDocs(IndexWriter writer, File file)
    throws IOException {
  	// do not try to index files that cannot be read
  	if (file.canRead()) {
      if (file.isDirectory()) {
      	String[] files = file.list();
        // an IO error could occur
        if (files != null) {
          for (int i = 0; i < files.length; i++) {
            indexDocs(writer, new File(file, files[i]));
          }
        }
      } else {
        System.out.println("adding " + file);
        try {
        		if(USE_DISAMBIGUATION){
        			GNSAXHandler hdlr = new GNSAXHandler(file);
            		DocumentVector docs=hdlr.getDocuments();
            		for(int i=0; i< docs.size(); i++){
            			writer.addDocument(docs.getDocumentAt(i));
            		}
        		} else {
        			NDGNSAXHandler hdlr = new NDGNSAXHandler(file);
            		DocumentVector docs=hdlr.getDocuments();
            		for(int i=0; i< docs.size(); i++){
            			writer.addDocument(docs.getDocumentAt(i));
            		}
        		}
        	    
            //writer.addDocument(hdlr.getDocument());
            //writer.addDocument(FileDocument.Document(file));
        }
        catch (Exception e) {
        	e.printStackTrace();
        } 
      }
    }
  }
}
