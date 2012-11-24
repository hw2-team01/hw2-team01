package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;

import banner.eval.uima.BANNERWrapper;



import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class MaibergKeyTermExtractor extends AbstractKeytermExtractor {

	private URL configFilePath;
	private String modelFilePath;

	@Override
	public void initialize(UimaContext aContext) {
		String configFilePathString = (String) aContext
				.getConfigParameterValue("configFile");

		String modelFilePathString = (String) aContext
				.getConfigParameterValue("modelFile");
		//configFilePath = getClass().getClassLoader().getResource(
			//	"config/" + configFilePathString);
		//modelFilePath = getClass().getClassLoader().getResource("output/" + modelFilePathString);
		configFilePath = Thread.currentThread().getContextClassLoader().getResource("config/" + configFilePathString);
		modelFilePath = "/output/" + modelFilePathString;
	}

	@Override
	protected List<Keyterm> getKeyterms(String question) {
		List<Keyterm> listKeyterms = new LinkedList<Keyterm>();
		BANNERWrapper banw = new BANNERWrapper();
		banw.initialize(configFilePath, modelFilePath);
		Map<String, String> annots = banw.getAnnotations(question);
		for (String k : annots.keySet()) {
			String mentionText = k, type = annots.get(k);
			Keyterm keyterm = new Keyterm(mentionText);
			keyterm.setComponentId(type);
			listKeyterms.add(keyterm);
			System.out.println("keyterm: " + mentionText + "type: " + type);
		}

		return listKeyterms;
	}
	
	public void testIntialize() {
		URL configFilePath = getClass().getClassLoader().getResource(
				"config/" + "banner_AZDC.xml");
		URL modelFilePath = getClass().getClassLoader().getResource("output/" +"model_AZDC.bin");
		if(configFilePath != null)
			System.out.println(configFilePath);
		if(modelFilePath != null)
			System.out.println(configFilePath);
	}
	
	public static void main(String[] args){
		MaibergKeyTermExtractor mkt = new MaibergKeyTermExtractor();
		mkt.testIntialize();
	}

}
