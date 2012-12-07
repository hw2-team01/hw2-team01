package edu.cmu.lti.f12.hw2.hw2_team01.passage;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.oaqa.model.Passage;

import edu.cmu.lti.f12.hw2.hw2_team01.passage.CorrectedPassageCandidateFinder.PassageSpan;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;

public class BioPassageCandidate extends PassageCandidate {

  public int keytermMatches;

  private List<PassageSpan> keytermSpans;

  private String cleanText;

  public BioPassageCandidate(String docID, int start, int end, float score, String queryString)
          throws AnalysisEngineProcessException {
    super(docID, start, end, score, queryString);
    this.keytermSpans = new ArrayList<PassageSpan>();
     keytermMatches =0;
  }

  public BioPassageCandidate(String docID, int start, int end, String queryString)
          throws AnalysisEngineProcessException {
    this(docID, start, end, 0, queryString);
  }

  private void setScore(float score) {
    super.setProbablity(score);
  }

  public void addSpans(List<PassageSpan> ps) {
    keytermSpans = ps;

  }

  public String getText() {
    return cleanText;
  }
  
  public void setText(String text) {
    cleanText = text ;
  }

}
