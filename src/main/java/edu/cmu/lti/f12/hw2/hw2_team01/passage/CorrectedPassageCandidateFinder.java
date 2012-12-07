package edu.cmu.lti.f12.hw2.hw2_team01.passage;

/*
 *  Copyright 2012 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import com.google.common.collect.Sets;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerProduct;

public class CorrectedPassageCandidateFinder {
  private String text;
  private String docId;
  
  private int textSize;      // values for the entire text
  private int totalMatches;  
  private int totalKeyterms;
  private double threshold;
  private KeytermWindowScorer scorer;
  
  public CorrectedPassageCandidateFinder( String docId ,  String text , KeytermWindowScorer scorer ) {
    super();
    this.text = text;
    this.docId = docId;
    this.textSize = text.length();
    this.scorer = scorer;
 //   this.threshold = 0.6; //threshold;
  }
  

  public List<PassageCandidate> extractPassages( String[] keyterms ) {
     String[] paragraphs = text.split("<p>");
     int start=0, end =0;
     List<PassageCandidate> passageList = new ArrayList<PassageCandidate>();
     for(String par:paragraphs){
       start = end + 3;
       end = start + par.length();
       String cleanPar = Jsoup.parse(par).text().replaceAll("([\177-\377\0-\32]*)", "");
       try {
        PassageCandidate pc = new PassageCandidate(docId,start,end,(float) getScore(keyterms,cleanPar),null);
   //     if(pc.getProbability() > threshold) 
          passageList.add(pc) ;
       } catch (AnalysisEngineProcessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
        
     }
      
     Collections.sort(passageList, new PassageCandidateComparator());
     return passageList;
     }
  
  public double getScore( String[] keyterms, String cleanText ) {
    List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<Double> scores = new ArrayList<Double>();
    double score =0;
    // Find all keyterm matches.
    for ( String keyterm : keyterms ) {
      Pattern p = Pattern.compile( keyterm );
      Matcher mClean = p.matcher(cleanText);
      while (mClean.find() ) {
        PassageSpan match = new PassageSpan(mClean.start() , mClean.end() ) ;
        matchedSpans.add( match );
        totalMatches++;
      }
      if (! matchedSpans.isEmpty() ) {
        matchingSpans.add( matchedSpans );
        totalKeyterms++;
      }
    }
    
    // create set of left edges and right edges which define possible windows.
    List<int[]> leftEdges = new ArrayList<int[]>();
    List<int[]> rightEdges = new ArrayList<int[]>();
    for ( List<PassageSpan> keytermMatches : matchingSpans ) {
      for ( PassageSpan keytermMatch : keytermMatches ) {
        int[] leftEdge = {keytermMatch.begin,keytermMatch.cleanBegin};
        int[] rightEdge = {keytermMatch.end,keytermMatch.cleanEnd}; 
        if (! leftEdges.contains( leftEdge ))
          leftEdges.add( leftEdge );
        if (! rightEdges.contains( rightEdge ))
          rightEdges.add( rightEdge );
      }
    }
    
    // For every possible window, calculate keyterms found, matches found; score window, and create passage candidate.
  //  List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for ( int[] begin : leftEdges ) {
      for ( int[] end : rightEdges ) {
        if ( end[0] <= begin[0] ) continue; 
        // This code runs for each window.
        int keytermsFound = 0;
        int matchesFound = 0;
        for ( List<PassageSpan> keytermMatches : matchingSpans ) {
          boolean thisKeytermFound = false;
          for ( PassageSpan keytermMatch : keytermMatches ) {
            if ( keytermMatch.containedIn( begin[0] , end[0] ) ){
              matchesFound++;
              thisKeytermFound = true;
            }
          }
          if ( thisKeytermFound ) keytermsFound++;
        }
        score = scorer.scoreWindow( begin[0] , end[0] , matchesFound , totalMatches , keytermsFound , totalKeyterms , cleanText.length() );
        scores.add(score);
      }
    }
   double sum =0;
   
   for(double s: scores){
     sum+=s;
   }
   double aScore = sum/(double)scores.size();
 //  System.out.println("average:" + aScore);
   return (scores.size() > 0)? aScore: 0;

  }
  private class PassageCandidateComparator implements Comparator {
    // Ranks by score, decreasing.
    public int compare( Object o1 , Object o2 ) {
      PassageCandidate s1 = (PassageCandidate)o1;
      PassageCandidate s2 = (PassageCandidate)o2;
      if ( s1.getProbability() < s2.getProbability() ) {
        return 1;
      } else if ( s1.getProbability() > s2.getProbability() ) {
        return -1;
      }
      return 0;
    }   
  }

  class PassageSpan {
    private int begin, end,cleanBegin,cleanEnd;
    public PassageSpan( int begin , int end ) {
      this.begin = begin;
      this.end = end;
    }
    
    public PassageSpan( int begin , int end, int cleanBegin, int cleanEnd ) {
      this.begin = begin;
      this.end = end;
      this.cleanBegin = cleanBegin;
      this.cleanEnd = cleanEnd;
    }
    
    public boolean containedIn ( int begin , int end ) {
      if ( begin <= this.begin && end >= this.end ) {
        return true;
      } else {
        return false;
      }
    }
  }
  
  public static void main ( String[] args ) {
    CorrectedPassageCandidateFinder passageFinder1 = new CorrectedPassageCandidateFinder( "1" , "The quick brown fox jumped over the quick brown fox." ,
        new KeytermWindowScorerProduct() );
    CorrectedPassageCandidateFinder passageFinder2 = new CorrectedPassageCandidateFinder( "1" , "The quick brown fox jumped over the quick brown fox." ,
        new KeytermWindowScorerSum() );
    String[] keyterms = { "quick" , "jumped" };
    List<PassageCandidate> windows1 = passageFinder1.extractPassages( keyterms );
    System.out.println( "Windows (product scoring): " + windows1 );
    List<PassageCandidate> windows2 = passageFinder2.extractPassages( keyterms );
    System.out.println( "Windows (sum scoring): " + windows2 );
  }
  

}