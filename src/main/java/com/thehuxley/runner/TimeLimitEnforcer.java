package com.thehuxley.runner;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Esta classe é uma segurança adicional sobre o safeexec. 
 * Ele tentar matar o processo para os casos em que o safeexec não funcionou muito bem.
 * @author rodrigo
 *
 */
public class TimeLimitEnforcer extends Thread {

    private final int KILL_THRESHOLD;
    private static final String getProcessStr;
    private static final CommandLine getProcessCmd;

    private Process p;
	private long timeLimit;

	static Logger logger = LoggerFactory.getLogger(TimeLimitEnforcer.class);

    static{
//        getProcessStr = "ps -Ao pid,\"%a\"";
        getProcessStr = "ps -Ao \"%p,%a\"";
        getProcessCmd = CommandLine.parse(getProcessStr);
    }

	public TimeLimitEnforcer(Integer killThreshold, Process p, long timeLimit) {
		KILL_THRESHOLD = killThreshold;
		this.p = p;
		this.timeLimit = timeLimit;
	}

	public void run() {
		try {
			Thread.sleep(this.timeLimit * 1000L);
            kill_forks();
			try {
				this.p.exitValue();
			} catch (IllegalThreadStateException e) {
                logger.error(e.getMessage(),e);

				this.p.destroy();
			}

		} catch (Exception e) {
            logger.error(e.getMessage(),e);
		}
	}


    protected boolean kill_forks(){
        try {
            boolean hasKilled = true;
            while (hasKilled) {
                hasKilled = false;

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);

                // Lista todos os processos

                DefaultExecutor executor = new DefaultExecutor();
                executor.setStreamHandler(streamHandler);

                int exitValue = executor.execute(getProcessCmd);
                if (exitValue != 0) {
                    logger.error("The command [" + getProcessStr + "] returned an unxpected code: " + exitValue);
                    return false;
                }
                String line[] = outputStream.toString().split("\\r?\\n");
                HashMap<String, Integer> commandsCounter = new HashMap<String, Integer>(line.length);
                HashMap<String, ArrayList<Integer>> commandPids = new HashMap<String, ArrayList<Integer>>();
                for (int i = 1; i < line.length; i++) {
                    int index = line[i].indexOf(',');
                    Integer pid = Integer.parseInt(line[i].substring(0, index).trim());
                    String cmdStr = line[i].substring(index+1).trim();

                    ArrayList<Integer> pids = commandPids.get(cmdStr);
                    if (pids==null){
                        pids = new ArrayList<Integer>();
                    }
                    pids.add(pid);
                    commandPids.put(cmdStr,pids);

                    Integer cmdCounter = commandsCounter.get(cmdStr);
                    if (cmdCounter == null) {
                        cmdCounter = 0;
                    }
                    cmdCounter++;
                    commandsCounter.put(cmdStr, cmdCounter);
                }

                for (String cmdStr : commandsCounter.keySet()) {
                    Integer numberOfForks = commandsCounter.get(cmdStr);

                    // processos identificados no Ubuntu16
                    if (cmdStr.equals("[bioset]")) continue;;

                    if (numberOfForks > KILL_THRESHOLD) {
                        hasKilled = true;
                        //kill cmd

                        executor = new DefaultExecutor();

                        ArrayList<Integer> pids = commandPids.get(cmdStr);
                        StringBuilder sb = new StringBuilder();
                        for (Integer pid : pids){
                            sb.append(pid);
                            sb.append(" ");
                        }


//                        String killProcessStr = "pkill -9 -x \"" + cmdStr + "\"";
                        String killProcessStr = "kill -9 " + sb.toString();
                        CommandLine killProcessCmd = CommandLine.parse(killProcessStr);
                        logger.info(killProcessStr);

                        try {
                            exitValue = executor.execute(killProcessCmd);
                            logger.info(cmdStr + " Killed!");
                        }catch(ExecuteException e){
                            logger.warn("The command [" + killProcessStr + "] returned an unxpected code: " + exitValue + ". " +
                                    "This could mean the the fork bomb is still active OR other submission has " +
                                    "already killed this process");
                        }
                    }
                }
            }//end while

            return true;

        }catch(Exception e){
            logger.error(e.getMessage(),e);
            return false;
        }

    }
}