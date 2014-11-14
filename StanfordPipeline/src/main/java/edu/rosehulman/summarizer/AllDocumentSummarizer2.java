package edu.rosehulman.summarizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.stats.Counter;

public class AllDocumentSummarizer2 {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		String modelPath = args[0];
		String inputDirPath = args[1];
		String outputDirPath = args[2];
		
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(modelPath));
		Counter<String> dfCounter = (Counter<String>) stream.readObject();
		stream.close();
		
		DocumentSummarizer2 summarizer = new DocumentSummarizer2(dfCounter);
		
		for (int numSentences = 1; numSentences <= 5; numSentences++) {
			for (File document : (new File(inputDirPath)).listFiles()) {
				String content = IOUtils.slurpFile(document);
				String summary = summarizer.summarize(content, numSentences);
				String outputPath = outputDirPath+"/"+numSentences+"SentenceSummary/"+document.getName();
				IOUtils.writeStringToFileNoExceptions(summary, outputPath, "UTF-8");
			}
		}
		
	}

}
