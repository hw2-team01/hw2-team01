package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class VictorKeyTermExtractor extends AbstractKeytermExtractor {

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
    log("************** I am ready");
  }

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    Chunking chunking = chunker.chunk(question);
    for(Chunk chunk: chunking.chunkSet()) {
      keyterms.add(new Keyterm(question.substring(chunk.start(), chunk.end())));
    }
    return keyterms;
  }
}