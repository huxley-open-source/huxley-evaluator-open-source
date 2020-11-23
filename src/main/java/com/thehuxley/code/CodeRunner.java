package com.thehuxley.code;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thehuxley.runner.SolutionRunner;
import com.thehuxley.runner.TempFilesCreator;

public class CodeRunner {
	
    private final int OUTPUT_DELTA = 4 * 1024;
    
	private static Logger logger = LoggerFactory.getLogger(CodeRunner.class);

    private String tempDir;
    
    private String scriptDir;

    public CodeRunner(String scriptDir, String tempDir) {
    	super();
        logger.info("New code runner created.");
    	
		String randomString = null;
		
		do {
			randomString = tempDir + File.separator + UUID.randomUUID().toString();
		} while(new File(randomString).exists() && new File(randomString).isDirectory());
		
		this.tempDir = randomString;
		
		new File(this.tempDir).mkdirs();
		
		this.scriptDir = scriptDir;
    }
    
    private String createFile(String filename, String content) {
        File distDir = new File(this.tempDir);
        
        if (distDir.exists() || distDir.mkdirs()) {
            File tempFile = Paths.get(this.tempDir.toString(), filename).toFile();
            PrintWriter writer = null;
            
            try {
                writer = new PrintWriter(tempFile);
                writer.write(content);
                writer.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            
            return tempFile.getAbsolutePath();
        }
        return null;
    }

    public String run(String script, String input, String filename, String content) {
    	String sourceCodeFilename = createFile(filename, content);

        TempFilesCreator tempFiles = null;
        
        try {
            SolutionRunner runner = new SolutionRunner(this.scriptDir, script, new File(sourceCodeFilename), 1);

            runner.configurePaths();
            
            tempFiles = new TempFilesCreator(this.tempDir);         

            String inputFileName = tempFiles.createInput(input);
                
            runner.run(inputFileName, OUTPUT_DELTA);
            
            String errors = checkExecutionErrors(runner);

            if ("OK".equals(errors)) {
                return FileUtils.readFileToString(new File(runner.getOutputFileName()));	
            } else {
            	return "!#ERROR:" + errors;
            }
        } catch (Exception e) {
        	e.printStackTrace();
        	throw new RuntimeException(e);
        } finally {
        	logger.info("Code runner done.");
            try {
				FileUtils.cleanDirectory(new File(this.tempDir));
			} catch (Exception e) {
				logger.error("Não foi possível apagar os arquivos criados.", e);
                throw new RuntimeException(e);
			}
        }		
    }
    
	public void close() {
		try {
			FileUtils.deleteDirectory(new File(this.tempDir));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private String checkExecutionErrors(final SolutionRunner solutionRunner) {
		switch (solutionRunner.getExitVal()) {
			case SolutionRunner.NORMAL_EXECUTION:
				return "OK";
			case SolutionRunner.RUNTIME_ERROR:
				if (!solutionRunner.isOutputFull()) {
					return solutionRunner.getErrorMessage();
				}
			case SolutionRunner.TIME_OUT:
				return "TIME_LIMIT_EXCEEDED";
			case SolutionRunner.COMPILATION_ERROR:
				return solutionRunner.getErrorMessage();
			default:
				return "HUXLEY_ERROR";
		}
	}
	
}
