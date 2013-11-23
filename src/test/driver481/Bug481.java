package driver481;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.util.*;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;

import java.util.Random;
import java.io.File;
import java.io.IOException;

class Bug481 {
	private static final Analyzer ANALYZER = new SimpleAnalyzer();
	private static final Random RANDOM = new Random();
	// private static Searcher SEARCHER;

	private static int ITERATIONS = 1;

	private static int random(int i) { // for JDK 1.1 compatibility
		int r = RANDOM.nextInt();
		if (r < 0)
			r = -r;
		return r % i;
	}

	private static class IndexerThread extends Thread {
		private final int reopenInterval = 30 + random(60);
		File indexDir;
		IndexWriter writer;

		public IndexerThread(File indexDirArg) {
			this.indexDir = indexDirArg;
			try {
				this.writer = new IndexWriter(this.indexDir, ANALYZER, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			try {


				
				for (int i = 0; i < 100; i++) {
					Document d = new Document();
					int n = RANDOM.nextInt();
					d.add(new Field("contents", English.intToEnglish(n),
							Field.Store.NO, Field.Index.TOKENIZED));

					// Switch between single and multiple file segments
					writer.setUseCompoundFile(true);

					writer.addDocument(d);

					{
						writer.close(); // always trigger it
						writer = new IndexWriter(indexDir, ANALYZER, false);
					}
				}

				writer.close();

			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
				System.exit(0);
			}
		}
	}

	private static class ReaderThread extends Thread {
		private final int reopenInterval = 10 + random(20);
		public File indexDir = null;

		public ReaderThread(File arg) throws java.io.IOException {
			this.indexDir = arg;
		}

		public void run() {
			try {
				IndexReader reader = IndexReader.open(this.indexDir);
				for (int i = 0; i < 5000; i++) {
//					if (reader.isCurrent())
					{
						long current = reader.getCurrentVersion(this.indexDir);
//						System.out.println("current version: " + current);
					}
				}
			} catch (Exception e) {
				System.out.println(e.toString());
				e.printStackTrace();
			}
		}

	}

	// public static boolean monitorrun= false;
	public static void main(String[] args) throws Exception {

		// in the FSDirectory.rename(), between delete("segments") and renameTo("segments.new", "segments"), the getCurrentVersion() interleaves to read the file that is deleted but not created by rename() yet.
		// leading to the following bug:
//		java.io.FileNotFoundException: /home/lpxz/eclipse/workspacegrail/lucene358684/index/segments (No such file or directory)
//		java.io.FileNotFoundException: /home/lpxz/eclipse/workspacegrail/lucene358684/index/segments (No such file or directory)
//			at java.io.RandomAccessFile.open(Native Method)
//			at java.io.RandomAccessFile.<init>(RandomAccessFile.java:233)
//			at org.apache.lucene.store.FSIndexInput$Descriptor.<init>(FSDirectory.java:425)
//			at org.apache.lucene.store.FSIndexInput.<init>(FSDirectory.java:434)
//			at org.apache.lucene.store.FSDirectory.openInput(FSDirectory.java:324)
//			at org.apache.lucene.index.SegmentInfos.readCurrentVersion(SegmentInfos.java:111)
//			at org.apache.lucene.index.IndexReader.getCurrentVersion(IndexReader.java:231)
//			at org.apache.lucene.index.IndexReader.getCurrentVersion(IndexReader.java:216)
//			at driver481.Bug481$ReaderThread.run(Bug481.java:111)
		
		//=======old version that simply triggers the bug.
//		File indexDir = new File("index");
//		if (!indexDir.exists())
//			indexDir.mkdirs();
//		Thread indexerThread = new IndexerThread(indexDir);
//		indexerThread.start();
//		ReaderThread searcherThread1 = new ReaderThread(indexDir);
//		searcherThread1.start();
		//======end of old version
		
		
		
		//involved:
		// FSDirectory.rename()  and IndexReader.getCurrentVersion(Directory dir)
		
		// eval: 713 vs 1234
		int threadNo = Integer.parseInt(args[0]);
		
		IndexerThread[] indexers = new  IndexerThread[threadNo];
		ReaderThread[] readers = new ReaderThread[threadNo];
		for(int i=0;i<threadNo ; i++)
		{
			File indexDir = new File("index"+i);
			if (!indexDir.exists())
				indexDir.mkdirs();
			indexers[i] = new IndexerThread(indexDir);
			readers[i] = new ReaderThread(indexDir);
		}
		
		long start = System.currentTimeMillis();
		for(int i=0;i<threadNo ; i++)
		{
			
			indexers[i].start();
			readers[i].start();
		}
		for(int i=0;i<threadNo ; i++)
		{
			
			indexers[i].join();
			readers[i].join();
		}
		long end = System.currentTimeMillis();
		System.out.println("duration:" + (end-start));
		

	}
}
