package com.thehuxley.runner;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cria/apaga os arquivos temporários necessários para a avaliação de uma
 * submissão pelos programas externos (safeexec)
 */
public class TempFilesCreator {

	private static Logger logger = LoggerFactory.getLogger(TempFilesCreator.class);
	
	private String path;
	
	private String outputFileName;
	
	private String inputFileName;

	public TempFilesCreator(String path) {
		this.path = path;

		long now = System.currentTimeMillis();
		
		this.outputFileName = this.path + File.separatorChar + now + ".out";
		this.inputFileName = this.path + File.separatorChar + now + ".in";
	}

	public String createInput(String input) {
		BufferedWriter bufferedWriter = null;
		
		try {
			bufferedWriter = new BufferedWriter(new FileWriter(this.inputFileName));
			
			if (input != null) {
				bufferedWriter.write(input);
			}
			
			return this.inputFileName;
		} catch (IOException e) {
			logger.error("Falha ao criar o arquivo com os casos de teste de entrada.", e);
			throw new RuntimeException(e);
		} finally {
			try {
				bufferedWriter.close();
			} catch (IOException e) {
				logger.error("Não foi possível criar o arquivo com os testes de entrada.", e);
				throw new RuntimeException(e);
			}
		}
	}

	public void createExpectedOutput(String output) {
		DataOutputStream outputStream = null;
		
		try {
			outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(this.outputFileName)));
			outputStream.writeBytes(output.trim());
			
		} catch (IOException e) {
			logger.error("Falha ao criar o arquivo com os casos de teste de saida", e);
			throw new RuntimeException(e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.error("Não foi possível criar o arquivo com os testes de entrada.", e);
				throw new RuntimeException(e);
			}
		}
	}

	public String getExpectedOutputFileName() {
		return outputFileName;
	}

	public String getInputFileName() { return inputFileName; }
}
