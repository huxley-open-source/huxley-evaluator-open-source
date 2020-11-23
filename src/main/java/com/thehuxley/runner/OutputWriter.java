package com.thehuxley.runner;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Essa classe sempre salva no arquivo utilizando o trim()
 *
 */
public class OutputWriter {
	
	private static Logger logger = LoggerFactory.getLogger(OutputWriter.class);
	
	private DataOutputStream outputStream;	
	
	private String outputFile;	
	
	public OutputWriter(String outputFile){
		this.outputFile = outputFile;
		
		try {
			this.outputStream = new DataOutputStream(new FileOutputStream(new File(outputFile)));
		} catch (FileNotFoundException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Não foi possível encontrar o stream para o arquivo " + this.outputFile + ".", e);
			}
		}
	}
	public void write(StringBuilder output){
		try {
			this.outputStream.writeBytes(output.toString().trim());
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Não foi possível escrever no arquivo " + this.outputFile + ".", e);
			}
		}
	}
	public void write(String output){
		try {
			this.outputStream.writeBytes(output.trim());
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Não foi possível escrever no arquivo " + this.outputFile + ".", e);
			}
		}
	}
	public void close(){
		try {
			this.outputStream.close();
		} catch (IOException e) {
			if (logger.isErrorEnabled()) {
				logger.error("Não foi possível fechar o arquivo " + this.outputFile + ".", e);
			}
		}
	}

}
