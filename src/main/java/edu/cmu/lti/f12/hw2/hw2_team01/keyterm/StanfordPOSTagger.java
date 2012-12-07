package edu.cmu.lti.f12.hw2.hw2_team01.keyterm;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordPOSTagger {

  private StanfordCoreNLP pipeline;

  private StanfordPOSTagger() {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    pipeline = new StanfordCoreNLP(props);
  }
  
  public List<CoreMap> annotate(String text) {
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    return document.get(SentencesAnnotation.class);
  }
  
  private static StanfordPOSTagger instance;
  public static StanfordPOSTagger getInstance() {
    if(instance == null) {
      System.err.println("Loading Stanford POS tagging pipeline");
      instance = new StanfordPOSTagger();
      System.err.println("POS tagging pipeline loaded");
    }
    return instance;
  }

}
