package edu.rosehulman.keywordextractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.List;

import com.google.common.io.Files;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.stats.Counter;

public class AllDocumentKeywordExtractor {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		String modelPath = args[0];
		String inputDirPath = args[1];
		String outputDirPath = args[2];
		
		ObjectInputStream stream = new ObjectInputStream(new FileInputStream(modelPath));
		Counter<String> DF = (Counter<String>) stream.readObject();
		stream.close();
		
		KeywordExtractor extractor = new KeywordExtractor(DF);
		
		for (File document : (new File(inputDirPath)).listFiles()) {
			String content = IOUtils.slurpFile(document);
			List<String> keywords = extractor.extractKeywords(content, 5);
			String outputPath = outputDirPath+"/"+document.getName();
			Files.write(keywords.toString(), new File(outputPath), Charset.defaultCharset());
		}
		
	}

}
