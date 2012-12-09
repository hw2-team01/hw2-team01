package edu.cmu.lti.f12.hw2.hw2_team01.passage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.cse.basephase.ie.AbstractPassageUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;

public class PassageSentenceChunkerExtractor extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
      public String apply(Keyterm keyterm) {
        return keyterm.getText();
      }
    });
    StringBuilder sb = new StringBuilder();
    for (String k : keytermStrings) {
      sb.append(".*?\\b" + k + "\\b.*?|");
    }

    Pattern p = Pattern.compile(sb.toString());

    for (RetrievalResult document : documents) {
      List<Passage> passages = new LinkedList<Passage>();
      System.out.println("RetrievalResult: " + document.toString());
      String id = document.getDocID();
      // Pattern p = new Pattern;
      String htmlText = "";
      try {
        htmlText = wrapper.getDocText(id);
      } catch (SolrServerException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      Map<String, int[]> sentences = BioSentenceChunker.getSentences(htmlText);
      for (String sentence : sentences.keySet()) {
        int[] indices = sentences.get(sentence);
        int start = indices[0], end = indices[1];
        String sentenceText = htmlText.substring(start, end);
        sentenceText = Jsoup.parse(sentenceText).text().replaceAll("([\177-\377\0-\32]*)", "").trim();
        //System.out.println(sentenceText);
        if (p.matcher(sentenceText).matches())
          try {
            System.out.println(sentenceText);
            result.add(new PassageCandidate(id,start,end,0,sentenceText));
          } catch (AnalysisEngineProcessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        else{
      //    System.out.println(sb.toString());
        }
      }

     // SimplePassageCandidateFinder finder = new SimplePassageCandidateFinder(id, passages,
      //        new KeytermWindowScorerSum(), htmlText.length());
      //result.addAll(finder.extractPassages(keytermStrings.toArray(new String[keytermStrings.size()])));
      //result.addAll(passages);
    }
    return result;

  }

}
