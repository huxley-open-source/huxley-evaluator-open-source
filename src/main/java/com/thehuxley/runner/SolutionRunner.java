package com.thehuxley.runner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolutionRunner {
	
	private static Logger logger = LoggerFactory.getLogger(SolutionRunner.class);
	
	/* These constants are derived from Readme.md */
	public static final int COMPILATION_ERROR = 105;
	public static final int NORMAL_EXECUTION = 0;
	public static final int RUNTIME_ERROR = 2;
	public static final int TIME_OUT = 3;

	/**
	 * Espera o TIME_LIMIT_TOLERANCE + (tempo limite do problema) pelo
	 * safeexec matar o processo. Caso isso não ocorra, ele tenta matar. É
	 * importante colocar um fator maior que 2 para que dê tempo de compilar e
	 * executar o programa.
	 */
	private final int TIME_LIMIT_TOLERANCE = 5;
	private final int ONE_MEGA = 1048576;
	private final int MAX_OUTPUT = ONE_MEGA;
	private final int KILL_THRESHOLD = 10;
	private final String scriptDir;

	private File sourceCode;
	
	private Integer exitVal = 0;

	private Integer timeLimit;
	
	private Double executionTime;
	private String outputFileName;	
	private String errorFileName;	
	private String safeScript;
	private String submissionFilename;
	
	private Boolean isOutputFull = false;
	private String errorMessage;

	public SolutionRunner(String scriptDir, String safeScript, File sourceCode, Integer timeLimit) {
		this.scriptDir = scriptDir;
		this.sourceCode = sourceCode;		
		this.safeScript = safeScript;
		this.timeLimit = timeLimit;
	}

    public void configurePaths() {
    	this.submissionFilename = sourceCode.getPath();
    	
    	String fileExec;
    	
    	String path = this.sourceCode.getPath();
    	
    	if (path.contains("\\.")) {
        	fileExec = path.substring(0, path.lastIndexOf("."));        	
        } else {
        	fileExec = path;
        }
    	
    	this.outputFileName = fileExec + ".out";
    	this.errorFileName = fileExec + ".err";
    	
    	if (!this.scriptDir.isEmpty()) {
    		this.safeScript = this.scriptDir + File.separator + this.safeScript;    		
    	}
    }

    public void run(String fileNameInputTestCase) {
        run(fileNameInputTestCase, MAX_OUTPUT);
    }
    
	public void run(String fileNameInputTestCase, int maxOutputSize) {
		Process proc = null;
		
		StreamGobbler outputGobbler = null;
		StreamGobbler errGobbler = null;
		
		this.exitVal = 0;		
		this.isOutputFull = false;
		
		try {			
			logger.debug("Executing = " + safeScript + " " + submissionFilename + " " + fileNameInputTestCase + " " + timeLimit);
			
			proc = Runtime.getRuntime().exec(new String[] {safeScript, submissionFilename, fileNameInputTestCase, String.valueOf(timeLimit)});
			
			TimeLimitEnforcer enforcer = new TimeLimitEnforcer(KILL_THRESHOLD, proc, timeLimit + TIME_LIMIT_TOLERANCE);
			
			outputGobbler = new StreamGobbler(proc.getInputStream(), maxOutputSize);
			errGobbler = new StreamGobbler(proc.getErrorStream(), MAX_OUTPUT);

			outputGobbler.start();
			errGobbler.start();
			
			enforcer.start();

			proc.waitFor();
			
			this.exitVal = proc.exitValue();
			
			logger.debug("Exit value = " + this.exitVal);
			
			setExecutionTime(errGobbler.getLastLine());
			logger.debug("Execution time = " + this.executionTime);
			
			writeOutputFile(this.outputFileName, outputGobbler);
			logger.debug("Output file = " + this.outputFileName);
			
			writeOutputFile(this.errorFileName, errGobbler);

			extractErrorMessage();
			
			logger.debug("Error file = " + this.errorFileName);
		} catch (Exception e) {
            throw new RuntimeException(e);
		} finally {
			try {
				if (outputGobbler != null && outputGobbler.isFull()) {
					if (outputGobbler.isFull()) {
						this.isOutputFull = true;
					}
					outputGobbler = null;	
				}				
				errGobbler = null;
				if (proc != null) {
					proc.destroy();
				}
				proc = null;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void setExecutionTime(String lastLine) {
		if (lastLine.contains("CPU_TIME_RUNNING")) {
			try {
				int beginIndex = lastLine.indexOf(':');
				String timeStr = lastLine.substring(beginIndex + 1, lastLine.length() - 1);

				this.executionTime = Double.parseDouble(timeStr);
			} catch (Exception e) {
				throw new RuntimeException("Erro ao tentar recuperar o tempo de execução da submissão", e);
			}
		} else {		
			this.executionTime = -1D;
		}
	}
	
	private void writeOutputFile(String filename, StreamGobbler outputGobbler) {
		OutputWriter outputWriter = null;

		try {
			StringBuilder outputBuffer = outputGobbler.getOutput();	
			outputWriter = new OutputWriter(filename);
			outputWriter.write(outputBuffer);
		} finally {
			outputWriter.close();
		}
	}
	
	private void extractErrorMessage() {
		try {
		this.errorMessage = new String(Files.readAllBytes(Paths.get(this.errorFileName)), StandardCharsets.UTF_8)
				.replace("huxley_jail", "")
				.replace(this.sourceCode.getParent(), "")
				.replaceAll("/usr/bin/ld.+\\n","")
				.replaceAll("CPU_TIME_RUNNING:\\d+.\\d+s", "");
		} catch (IOException e) {
			logger.error("Não foi possível extrair mensagem de erro.");
		}
	}
	
	public Double getExecutionTime() {
		return this.executionTime;
	}
	
	public int getExitVal() {
		return exitVal;
	}
	
	public String getOutputFileName() {
		return this.outputFileName;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}

	public boolean isOutputFull() {
		return this.isOutputFull;
	}
	
}
