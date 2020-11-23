package com.thehuxley.oracle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thehuxley.model.Submission;
import com.thehuxley.runner.SolutionRunner;
import com.thehuxley.runner.TempFilesCreator;
import com.thehuxley.util.HashUtils;

public class Oracle {

	private static Logger logger = LoggerFactory.getLogger(Oracle.class);
	
	private Map<String, Integer> ranking = new HashMap<String, Integer>();
	
	private Map<String, String> hashResults = new HashMap<String, String>();
	
	private String tempDir;
	
	private String scriptDir;
	
	public Oracle(String scriptDir, String tempDir) {
		super();
        logger.info("New oracle created.");
        
		String randomString = null;
		
		do {
			randomString = tempDir + File.separator + UUID.randomUUID().toString();
		} while(new File(randomString).exists() && new File(randomString).isDirectory());
		
		this.tempDir = randomString;
		
		new File(this.tempDir).mkdirs();
		
		this.scriptDir = scriptDir;
	}

	public OracleResult run(String input, List<Submission> submissions) {
		logger.info("Running oracle... [input = " + input + "]");
		
		for (Submission submission : submissions) {
			TempFilesCreator tempFiles = new TempFilesCreator(this.tempDir);
			
			String inputFileName = tempFiles.createInput(input);
			
			createSubmissionFile(submission);
	        
			SolutionRunner runner = new SolutionRunner(this.scriptDir, submission.getLanguage().getScript(), submission.getSubmissionFile(), 1);
	        runner.configurePaths();
	        
	        try {
		        runner.run(inputFileName);
		        
	        	Path path = Paths.get(runner.getOutputFileName());
	        	
	        	String output = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	        	
	        	logger.debug("Ranking answer [output = " + output + "]");
	        	
	    		String hashResult = HashUtils.sha1(output);
	    		
	    		if (!this.hashResults.containsKey(hashResult)) {
                    // Primeira vez que a resposta aparece
	    			this.hashResults.put(hashResult, output);
	    		}
	    		
	    		if (runner.getExitVal() == 0) {
	    			this.rankSubmissionResult(hashResult);
	    		}
	        } catch (Exception e) {
				logger.error("unexpected-error", e);
	        	throw new RuntimeException(e);
	        }
		}
		
		try {
			FileUtils.cleanDirectory(new File(this.tempDir));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        
		OracleResult result = this.getResult();
		
		logger.debug("Chosen answer [output = " + result.toJson() + "]");
		
		logger.info("Oracle done.");
		
		return result;
	}
	
	public void close() {
		try {
			FileUtils.deleteDirectory(new File(this.tempDir));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    private void createSubmissionFile(Submission submission) {
        File distDir = new File(this.tempDir);
        
        if (distDir.exists() || distDir.mkdirs()) {
            File tempFile = Paths.get(this.tempDir.toString(), submission.getFilename()).toFile();
            PrintWriter writer = null;
            
            try {
                writer = new PrintWriter(tempFile);
                writer.write(submission.getSourceCode());
                writer.close();
                
                submission.setSubmissionFile(tempFile);
            } catch (Exception e) {
				logger.error("unexpected-error", e);
                throw new RuntimeException(e);
            }
        }
    }
	
	private void rankSubmissionResult(String hashResult) {
		if (this.ranking.containsKey(hashResult)) {
            // Resposta já existia, incrementa
			this.ranking.put(hashResult, this.ranking.get(hashResult) + 1);
		} else {
            // primeira vez
			this.ranking.put(hashResult, 1);
		}
	}
	
	private OracleResult getResult() {
        if (ranking.size() == 0) {
            return new OracleResult(OracleResult.Type.NO_ANSWER, 0,0, null);
        }

		String chosenHashResult = null;
		Integer greater = 0;
        Integer current;
		
		for (String output : this.ranking.keySet()) {
            current = this.ranking.get(output);

			if (current > greater) {
				chosenHashResult = output;
				greater = this.ranking.get(output);
			}
		}

		String chosenOutput = this.hashResults.get(chosenHashResult);

        Integer total = this.ranking.keySet().size();
        
        if (greater == total) {
            return new OracleResult(OracleResult.Type.CONSENSUS, greater ,0, chosenOutput);
        } else if (greater >= (total / 2)) {
            return new OracleResult(OracleResult.Type.MAJORITY, greater, total - greater, chosenOutput);
        } else {
            return new OracleResult(OracleResult.Type.INCONCLUSIVE, greater, total - greater, chosenOutput);
        }
	}
	
}
