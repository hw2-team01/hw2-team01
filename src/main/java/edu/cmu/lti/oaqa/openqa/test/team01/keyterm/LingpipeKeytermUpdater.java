package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;

import java.io.File;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class LingpipeKeytermUpdater extends AbstractKeytermUpdater {

  /**
   * HMM chunker instance.
   */
  private Chunker chunker;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    String modelFile = (String) context.getConfigParameterValue("model");
    try {
      chunker = (Chunker) AbstractExternalizable.readObject(new File(modelFile));
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    Chunking chunking = chunker.chunk(question);
    for(Chunk chunk: chunking.chunkSet()) {
      String word = question.substring(chunk.start(), chunk.end());
      keyterms.add(new Keyterm(word));
      log("LingPipe keyterm: "+word);
    }
    return keyterms;
  }
  
  public static void main(String[] args) {
    String modelFile = "src/main/resources/models/ne-en-bio-genetag.HmmChunker";
    String question = "What is the role of PrnP in mad cow disease (bovine spongiform encephalitis, BSE)?";
    try {
      Chunker achunker = (Chunker) AbstractExternalizable.readObject(new File(modelFile));
      Chunking chunking = achunker.chunk(question);
      for(Chunk chunk: chunking.chunkSet()) {
        System.out.println(question.substring(chunk.start(), chunk.end()));
      }
    } catch (Exception e) {
      System.err.println("oops");
    }
  }
}