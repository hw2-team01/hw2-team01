package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.resource.ResourceInitializationException;

public class MeSHQueryExpander {

  private Connection connection;
  private PreparedStatement synomymStatement;
  private Pattern wordPattern = Pattern.compile("[^\\W\\d_]+");

  public MeSHQueryExpander(String databasePath) throws ResourceInitializationException {
    try {
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection("jdbc:sqlite:"+databasePath);
      synomymStatement = connection.prepareStatement("select term from terms where concept_ui="
              +"(select concept_ui from terms where term=?)");
    }
    catch(SQLException e) {
      throw new ResourceInitializationException(e);
    }
    catch(ClassNotFoundException e) {
      throw new ResourceInitializationException(e);
    }
  }

  private String normalize(String term) {
    ArrayList<String> tokens = new ArrayList<String>();
    Matcher matcher = wordPattern.matcher(term.toLowerCase());
    while(matcher.find())
      tokens.add(matcher.group());
    Collections.sort(tokens);
    return StringUtils.join(tokens, " ");
  }

  public List<String> getSynomyms(String term) {
    ArrayList<String> synonyms = new ArrayList<String>();
    try {
      System.err.println("MeSH db query: "+normalize(term));
      synomymStatement.setString(1, normalize(term));
      ResultSet rs = synomymStatement.executeQuery();
      while(rs.next())
        synonyms.add(rs.getString("term"));
    }
    catch(SQLException e) {
      System.err.println(e.getMessage());
    }
    return synonyms;
  }

  public static void main(String[] args) {
    for(String query: Arrays.asList("mad cow disease", "T-cells", "adenomatous polyposis coli", "Huntington's disease")) {
      try {
        MeSHQueryExpander expander = new MeSHQueryExpander("src/main/resources/models/MeSH.db");
        for(String synonym: expander.getSynomyms(query))
          System.out.println("synonym = "+synonym);
      } catch (ResourceInitializationException e) {
        e.printStackTrace();
      }
    }
  }
}