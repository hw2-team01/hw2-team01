package edu.cmu.lti.f12.hw2.hw2_team01.keyterm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

public class VerbKeytermUpdater extends AbstractKeytermUpdater {

  private Set<String> stopwords;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // Load stopwords
    stopwords = new HashSet<String>();
    String stopwordFile = (String) context.getConfigParameterValue("stopwords");
    log("Reading stop words from "+stopwordFile);
    try {
      BufferedReader br = new BufferedReader(new FileReader(stopwordFile));
      String line;
      while ((line = br.readLine()) != null) {
        stopwords.add(line.trim());
      }
      br.close();
    } catch (FileNotFoundException e) {
      System.err.println("Stop word file not found");
      throw new ResourceInitializationException(e);
    }
    catch(IOException e) {
      System.err.println("Error while reading word file");
      throw new ResourceInitializationException(e);
    }
    log("Read "+stopwords.size()+" stop words.");
    // Load POS tagging pipeline
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    for (CoreMap sentence : StanfordPOSTagger.getInstance().annotate(question)) {
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        String pos = token.get(PartOfSpeechAnnotation.class);
        if (pos.startsWith("V")) {
          String word = token.get(TextAnnotation.class);
          if(stopwords.contains(word)) continue;
          Keyterm keyterm = new Keyterm(word);
          keyterm.setComponentId("VERB");
          keyterms.add(keyterm);
        }
      }
    }
    return keyterms;
  }
}
