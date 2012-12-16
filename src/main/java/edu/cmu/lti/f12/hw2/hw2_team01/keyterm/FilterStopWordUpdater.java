package edu.cmu.lti.f12.hw2.hw2_team01.keyterm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class FilterStopWordUpdater extends AbstractKeytermUpdater {

  private Set<String> stopwords;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    // Load stopwords
    stopwords = new HashSet<String>();
    String stopwordFile = (String) context.getConfigParameterValue("stopwords");
    log("Reading stop words from " + stopwordFile);
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
    } catch (IOException e) {
      System.err.println("Error while reading word file");
      throw new ResourceInitializationException(e);
    }
    log("Read " + stopwords.size() + " stop words.");
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    List<Keyterm> result = new ArrayList<Keyterm>();
    for (Keyterm word : keyterms) {
      if (stopwords.contains(word.toString().toLowerCase()))
        continue;
      result.add(word);
    }
    return result;
  }
}
