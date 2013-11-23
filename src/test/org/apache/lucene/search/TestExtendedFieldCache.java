package org.apache.lucene.search;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.English;

import java.io.IOException;

import junit.framework.TestCase;

public class TestExtendedFieldCache  {
  protected static IndexReader reader;
  private static final int NUM_DOCS = 1000;

  public TestExtendedFieldCache(String s) {
  }



  public static void main(String[] args) throws IOException {
	  RAMDirectory directory = new RAMDirectory();
	    IndexWriter writer= new IndexWriter(directory, new WhitespaceAnalyzer(), true);
	    int theInt = Integer.MAX_VALUE;
	    for (int i = 0; i < NUM_DOCS; i++){
	      Document doc = new Document();
	      doc.add(new Field("theInt", String.valueOf(theInt--), Field.Store.NO, Field.Index.UN_TOKENIZED));
	      writer.addDocument(doc);
	    }
	    writer.close();
	    reader = IndexReader.open(directory);
	  
	  FieldCacheImpl cache = new FieldCacheImpl();
    int [] ints = cache.getInts(reader, "theInt");
    
    boolean assertion = true;
    assertion =  assertion && (ints.length == NUM_DOCS);
    
    for (int i = 0; i < ints.length; i++) {
    	assertion = assertion && (ints[i] == (Integer.MAX_VALUE - i));
    }
    
    System.out.println("assertion is: " + assertion);
    
    
  }
}