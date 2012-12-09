package edu.cmu.lti.f12.hw2.hw2_team01.passage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;


public class BioSentenceChunker {
  static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;

  static final SentenceModel SENTENCE_MODEL = new MedlineSentenceModel();

  static final SentenceChunker SENTENCE_CHUNKER = new SentenceChunker(TOKENIZER_FACTORY,
          SENTENCE_MODEL);
  
  public static void main(String[] args) throws IOException{
    String path = "../hw2-team01/src/main/resources/input/sample_doc.txt";
 //   Scanner s = new Scanner(f);
    String file2text = "";
    FileInputStream stream = new FileInputStream(new File(path));
    try {
      FileChannel fc = stream.getChannel();
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      /* Instead of using default, pass in a decoder. */
      file2text = Charset.defaultCharset().decode(bb).toString();
    }
    finally {
      stream.close();
    }
    
    for(String sentence: getSentences(file2text).keySet()){
     
    }
    
  }
  
  public static Map<String,int[]> getSentences(String text){ 
    Chunking chunking = SENTENCE_CHUNKER.chunk(text.toCharArray(), 0, text.length());
    Set<Chunk> sentences = chunking.chunkSet();
    String slice = chunking.charSequence().toString();
    Map<String,int[]> sentencesOut = new HashMap<String,int[]>();
    int i = 1;
    for (Chunk sentence : sentences) {
        int start = sentence.start();
        int end = sentence.end();
        String sentenceText = slice.substring(start,end);
        System.out.println("SENTENCE "+(i++)+":");
   //     System.out.println(sentenceText);
        sentencesOut.put(sentenceText,new int[]{start,end});
    }
    return sentencesOut;
  }
}
