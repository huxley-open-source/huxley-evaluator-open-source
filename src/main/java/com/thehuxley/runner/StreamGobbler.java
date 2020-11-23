package com.thehuxley.runner;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Armazena a saída de um processo.
 * 
 * @author rodrigo
 * 
 */
public class StreamGobbler extends Thread {
	
	private static final String TRUNCATED_OUTPUT = "<== Saida Truncada ==>";

	public static final String EMPTY_ANSWER_MARK = "** EMPTY ANSWER **";

	/** Fluxo de saída do processo */
	private InputStream is;

	/** Buffer onde a saída é armazenada */
	private StringBuilder buffer;

	/** Armazena a última linha de saída do processo */
	private String lastLine;

	/** true caso o processo já tenha se encerrado e toda a saída foi lida */
	private boolean end;

	/** Armazena o tamanho máximo do buffer */
	private int maxBufferSize;

	private static String LINE_SEPARATOR = System.getProperty("line.separator");

	/**
	 * Essa constante serve apenas para criar um tamanho inicial, mas não
	 * significa que não aceitará linhas maiores que ela
	 */
	private static int initialLineSize ;

	/**
	 * Caso ocorra um outofmemory, o tamanho inicial do buffer será dividido por
	 * esse fator e uma nova tentativa de alocação será realizada.
	 */
	private static final int FACTOR = 2;

	private static int bufferInitialCapacity;

	/** true quando o buffer alcançou o seu tamanho máximo */
	private boolean isFull;

	static Logger logger = LoggerFactory.getLogger(StreamGobbler.class);

	public StreamGobbler(InputStream is, int maxBufferSize) {
		this.is = is;
		this.buffer = null;
		this.maxBufferSize = maxBufferSize;
		this.lastLine = null;
		this.end = false;
		this.isFull = false;
		bufferInitialCapacity = maxBufferSize / 2;
		initialLineSize = 1024;
	}

	public void run() {
		synchronized (this) {
			String lastLineLocal = null;
			DataInputStream inputStream = null;
			boolean outOfMemory;
			do {
				outOfMemory = false;
				try {
					this.buffer = new StringBuilder(bufferInitialCapacity);
					inputStream = new DataInputStream(is);
					StringBuffer line = new StringBuffer(initialLineSize);
					char letter;

					int readByte = inputStream.read();

					/*
					 * Nesse loop, ele adiciona linha por linha. Para isso,
					 * utiliza o final de linha do sistema de arquivo que está
					 * sendo executado.
					 */
					while ((readByte != -1) && (!isFull)) {
                        letter = (char) readByte;
						if (!LINE_SEPARATOR.equals(Character.toString(letter))) {
							line.append(letter);
						} else {
							this.buffer.append(line + LINE_SEPARATOR);
							lastLineLocal = line.toString();
							line = new StringBuffer(initialLineSize);
						}
						readByte = inputStream.read();
						isFull = (line.length() + buffer.length()) >= maxBufferSize;
					}

					/*
					 * Ela sai do loop sob duas condições: 1- Chegou ao final do
					 * arquivo 2- Estourou o buffer
					 */

					/*
					 * Se a linha está com tamanho 0, podem ter ocorrido: 1-
					 * Nada foi lido da entrada e portanto o lastLineLocal está
					 * null 2- Foi lida apenas uma linha com um \n no final e
					 * lastLineLocal está definido.
					 */
					if (line.length() == 0) {
						if (lastLineLocal == null) {
							lastLineLocal = EMPTY_ANSWER_MARK;
						}
					} else {
						/*
						 * Este else significa que alguma coisa foi lida e não é
						 * um final de linha. Logo, pode ter sido: 1- Se
						 * lastLineLocal == null, então foi lida uma única linha
						 * e que não tinha um final de linha. Esse conteúdo que
						 * deve ser colocado em lastLineLocal 2- Se
						 * lastLineLocal !=null, então a já existem linhas
						 * anteriores, porém a última linha lida não tem um
						 * final de linha.
						 * 
						 * Ou seja, nas duas situações se faz necessário definir
						 * uma nova última linha.
						 */


                        if(isFull){
                            /*
                             * Se o programa ter uma resposta maior do q a esperada, deve ser adicionado um aviso na saida
                             */
                            this.buffer.append(line);
                            line = new StringBuffer(TRUNCATED_OUTPUT);
                            this.buffer.append(line + LINE_SEPARATOR);
                        }
                        else
                        {
                            /*
                             * Essa adição do LINE_SEPARATOR faz com que mesmo as
                             * entradas com uma única linha, sem final de linha ao
                             * final, contenha um final de linha
                             */
                             this.buffer.append(line + LINE_SEPARATOR);
                        }


                        lastLineLocal = line.toString();
                        line = new StringBuffer(initialLineSize);
					}
				} catch (OutOfMemoryError error) {
					final int MEGABYTE = (1024 * 1024);
					MemoryUsage heapUsage = ManagementFactory.getMemoryMXBean()
							.getHeapMemoryUsage();
					long maxMemory = heapUsage.getMax() / MEGABYTE;
					long usedMemory = heapUsage.getUsed() / MEGABYTE;
					logger.error(error.getMessage() + ". Memory Use :"
							+ usedMemory + "M/" + maxMemory + "M");
					
					buffer = null;

					Runtime.getRuntime().gc(); // pede ajuda ao garbage
												// collector
					bufferInitialCapacity /= FACTOR;
					initialLineSize /=FACTOR;
					outOfMemory = true;
					logger.error("Tentativa de se recuperar: chamando Garbage Collector, Diminuindo o tamanho do buffer para "
							+ bufferInitialCapacity);

				} catch (IOException e) {
                    logger.info(e.getMessage() + ". Provavelmente, foi uma submissão com timelimit.");

                } catch (Exception e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } while (outOfMemory && bufferInitialCapacity > 0);

			// Fecha a stream
			try {
				inputStream.close();
			} catch (Exception e) {
                logger.warn(e.getMessage(),e);
			}

			this.end = true;
			this.lastLine = lastLineLocal;
			notifyAll();
		}
	}

	public boolean isFull() {
		return isFull;
	}

	public synchronized StringBuilder getOutput() {
		while (!this.end) {
			try {
				wait();
			} catch (InterruptedException localInterruptedException) {
                logger.error(localInterruptedException.getMessage(), localInterruptedException);
			}
		}
		return this.buffer;
	}

	public synchronized String getLastLine() {
		while (!this.end) {
			try {
				wait();
			} catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
			}
		}
		return this.lastLine;
	}

    public InputStream getInputStream() {
        return is;
    }
}