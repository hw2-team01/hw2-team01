package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import banner.eval.uima.BANNERWrapper;


import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class BannerKeytermUpdater extends AbstractKeytermUpdater {

	private BANNERWrapper banw;

	@Override
	public void initialize(UimaContext aContext)
			throws ResourceInitializationException {
		super.initialize(aContext);
		String configFilePathString = (String) aContext
				.getConfigParameterValue("configFile");
		String modelFilePathString = (String) aContext
				.getConfigParameterValue("modelFile");

		URL configFilePath = Thread.currentThread().getContextClassLoader()
				.getResource("config/" + configFilePathString);
		String modelFilePath = "/output/" + modelFilePathString;

		banw = new BANNERWrapper();
		banw.initialize(configFilePath, modelFilePath);
	}

	protected List<Keyterm> testUpdateKeyterms(String question,
			List<Keyterm> keyterms) {
		Map<String, String> annots = banw.getAnnotations(question + " ."); // XXX
																			// added
																			// extra
																			// content
																			// to
																			// force
																			// complete
																			// processing
																			// (??)
		for (String k : annots.keySet()) {
			String mentionText = k, type = annots.get(k);
			Keyterm keyterm = new Keyterm(mentionText);
			keyterm.setComponentId(type);
			keyterms.add(keyterm);
			// log("BANNER keyterm: " + mentionText + " type: " + type);
		}
		return keyterms;
	}

	@Override
	protected List<Keyterm> updateKeyterms(String question,
			List<Keyterm> keyterms) {
		Map<String, String> annots = banw.getAnnotations(question + " ."); // XXX
																			// added
																			// extra
																			// content
																			// to
																			// force
																			// complete
																			// processing
																			// (??)
		for (String k : annots.keySet()) {
			String mentionText = k, type = annots.get(k);
			Keyterm keyterm = new Keyterm(mentionText);
			keyterm.setComponentId(type);
			keyterms.add(keyterm);
			log("BANNER keyterm: " + mentionText + " type: " + type);
		}
		return keyterms;
	}

	public void testIntialize(String configFile, String modelFile) {
		URL configFilePath = getClass().getClassLoader()
				.getResource(configFile);// "config/banner_AZDC.xml");
		String modelFilePath = modelFile; // "/output/model_AZDC.bin";
		if (configFilePath != null)
			System.out.println(configFilePath);
		if (modelFilePath != null)
			System.out.println(configFilePath);
		banw = new BANNERWrapper();
		banw.initialize(configFilePath, modelFilePath);
		File input = new File("src/main/resources/input/trecgen06.txt");
		Scanner scanner = null;
		try {
			scanner = new Scanner(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (scanner.hasNext()) {
			String q = scanner.nextLine().replaceAll("^[0-9]*\\|", "");
			System.out.println("Question: " + q);
			for (Keyterm term : testUpdateKeyterms(q, new LinkedList<Keyterm>()))
				System.out.println(term);
		}

	}

	public static void main(String[] args) {
		BannerKeytermUpdater mkt = new BannerKeytermUpdater();

		System.out.println("Diseases: ");
		mkt.testIntialize("config/banner_AZDC.xml", "/output/model_AZDC.bin");

		System.out.println("Genes:");
		mkt.testIntialize("config/banner_BC2GM.xml", "/output/model_BC2GM.bin");

		System.out.println("Protiens:");
		
		
	}

}
