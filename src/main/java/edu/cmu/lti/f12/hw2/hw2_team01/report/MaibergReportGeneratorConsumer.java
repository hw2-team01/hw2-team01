package edu.cmu.lti.f12.hw2.hw2_team01.report;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.CasConsumer_ImplBase;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import edu.cmu.lti.oaqa.ecd.BaseExperimentBuilder;
import edu.cmu.lti.oaqa.ecd.phase.ProcessingStepUtils;
import edu.cmu.lti.oaqa.framework.eval.ExperimentKey;
import edu.cmu.lti.oaqa.framework.report.ReportComponent;
import edu.cmu.lti.oaqa.framework.report.ReportComponentBuilder;
import edu.cmu.lti.oaqa.framework.report.ReportGenerator;
import edu.cmu.lti.oaqa.framework.types.ExperimentUUID;

public class MaibergReportGeneratorConsumer extends CasConsumer_ImplBase implements ReportGenerator {

  private List<ReportComponentBuilder> builders;

  private Set<ExperimentKey> experiments = Sets.newHashSet();

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    Object builderNames = (Object) context.getConfigParameterValue("builders");
    if (builderNames != null) {
      this.builders = BaseExperimentBuilder.createResourceList(builderNames, 
              ReportComponentBuilder.class);
    }
  }

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    try {
      JCas jcas = aCAS.getJCas();
      ExperimentUUID experiment = ProcessingStepUtils.getCurrentExperiment(jcas);
      experiments.add(new ExperimentKey(experiment.getUuid(), experiment.getStageId()));
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    System.out.println(" ------------------ EVALUATION REPORT ------------------");
    for (ExperimentKey experiment : experiments) {
      System.out.println("Experiment: " + experiment);
      for (ReportComponentBuilder builder : builders) {
        ReportComponent rc = builder.getReportComponent(experiment.getExperiment(),
                String.valueOf(experiment.getStage()));
        System.out.println(print(rc));
      }
    }
    System.out.println(" -------------------------------------------------------");
  }

  private String print(ReportComponent rc) {
    List<String> headers = rc.getHeaders();
    String reportTable = "";
    Table<String, String, String> table = rc.getTable();
    List<String> values = Lists.newArrayList();
    
    for (Map<String, String> row : table.rowMap().values()) {
      for (String header : headers) {
        values.add(escape(row.get(header)));
      }
    }
    
    for(int i=0;i<values.size();i++){
    	float score;
    	String v = values.get(i);
    	//String header = headers.get(i);
    	try{
    		score = Float.parseFloat(v);
    		reportTable +=  (i<values.size())? score + ",": score +"";
    	} catch(NumberFormatException e){
    		
    	}
    }
    
    for(int i=2;i<headers.size();i++){
    	String header = headers.get(i);
    	reportTable +=  (i<headers.size())? header + ",": header+"";
    }
    
    return reportTable;
  }

  private String escape(String string) {
    return string.replace(",", "_");
  }
}