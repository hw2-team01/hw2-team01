package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.util.AbstractExternalizable;


import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class MaibergKeyTermExtractor extends AbstractKeytermExtractor {
	

  private final String chunkerFilePath = "resources/ne-en-bio-genetag.HmmChunker";

@Override
protected List<Keyterm> getKeyterms(String question) {
	java.io.File f = new java.io.File(chunkerFilePath);
	List<Keyterm> listKeyterms = new LinkedList<Keyterm>();
	Chunker chunker = null;
	try {
		chunker = (Chunker) AbstractExternalizable.readObject(f);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	Chunking chunking = chunker.chunk(question);
	Set<Chunk> chunkSet = chunking.chunkSet();
	for (Chunk c : chunkSet)
		if (c != null) {
			String keyterm = question.substring(c.start(), c.end());
			Keyterm k = new Keyterm(keyterm);
			listKeyterms.add(k);
		}
	return listKeyterms;
}

  
	
	
	
}
