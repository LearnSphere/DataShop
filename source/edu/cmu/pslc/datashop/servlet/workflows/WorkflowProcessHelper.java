package edu.cmu.pslc.datashop.servlet.workflows;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import edu.cmu.pslc.datashop.workflows.ThreadedStreamReader;

public class WorkflowProcessHelper {

    // Remote execution nonces
   public static ConcurrentLinkedQueue<String> nonceList = null;
   public static final String LS_WFC_NONCE_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
   public static SecureRandom secureRandom = new SecureRandom();
   public static final Integer LS_WFC_NONCE_LENGTH = 64;
   /** Kill command. */
   public static String PROCESS_TREE_KILL_COMMAND = "pkill -TERM -P ";

   /** Private map for component-id to ProcessBuilder (which is not synchronous). */
   public static Map<String, Process> wfProcessMap;
   /** Private map for component-id to ProcessBuilder for "proprietary" programs,
    * like an R script or an executable (not synchronous). */
   public static Map<String, Process> wfProprietaryProcessMap;

   /** Debug logging. */
   private static Logger staticLogger = Logger.getLogger(WorkflowFileHelper.class.getName());

    public static String randomAlphanumeric(Integer length) {
    StringBuffer sBuffer = new StringBuffer();
    for (Integer i = 0; i < length; i++)
        sBuffer.append(LS_WFC_NONCE_ALPHABET.charAt(secureRandom.nextInt(LS_WFC_NONCE_ALPHABET.length())));
    return sBuffer.toString();
}

    /**
     * Gets a nonce for remote components.
     * @return
     * @throws IllegalArgumentException if radius is negative.
     */
    public static String getNonce(Integer nonceLength) throws IllegalArgumentException {
        Boolean pass = false;
        String nonce = null;
        if (nonceLength < 32) {
            throw new IllegalArgumentException("Nonce length (" + nonceLength
                + ") must be greater than or equal to 32.");
        } else {
            while (!pass) {
                String tmp = randomAlphanumeric(nonceLength);
                if (!nonceList.contains(tmp)) {
                    nonceList.add(tmp);
                    nonce = tmp.toString();
                    pass = true;
                }
            }
        }
        return nonce;
    }

    public static boolean hasNonce(String nonce) {
        Boolean hasNonce = false;
        if (nonceList.contains(nonce)) {
            hasNonce = true;
        }
        return hasNonce;
    }
    public static void removeNonce(String nonce) {
        if (nonceList.contains(nonce)) {
            nonceList.remove(nonce);
        }
    }


    public static Map<String, Process> getwfProcessMap() {
        return wfProcessMap;
    }

    public static void addWfProcess(String key, Process process) {
        wfProcessMap.put(key, process);
    }

    public static Map<String, Process> getwfProprietaryProcessMap() {
        return wfProprietaryProcessMap;
    }

    public static void addWfProprietaryProcess(String key, Process process) {
        wfProprietaryProcessMap.put(key, process);
    }

    public static void markProcessCompleted(String key) {

        if (wfProprietaryProcessMap.containsKey(key)) {
            wfProprietaryProcessMap.remove(key);
        }
        if (wfProcessMap.containsKey(key)) {
            wfProcessMap.remove(key);
        }
    }

    public void removeWfProcess(String key) {
        // Try to cancel and remove process if running locally (Will not work in Windows b/c PID).
        removeLocalProcess(wfProprietaryProcessMap, key);
        removeLocalProcess(wfProcessMap, key);
    }

    private void removeLocalProcess(Map<String, Process> processMap, String key) {
        if (processMap.containsKey(key)) {
            Process process = processMap.get(key);
            Long parentPid = null;
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.indexOf("win") >= 0) {
                //String winCmd = "taskkill /F /T /PID " + JNAHandler.getPid(process);
            } else {

                if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
                    /* get the PID on unix/linux systems */
                    try {
                        Field f = process.getClass().getDeclaredField("pid");
                        f.setAccessible(true);
                        parentPid = f.getLong(process);
                    } catch (Throwable e) {
                        staticLogger.error("Canceled workflow could not kill process (PID = " + parentPid + ")");
                    }
                } else {
                    // Killing a windows process tree requires JNA or some binary.
                    // Not implementing for windows dev machines due to time constraints.
                }

                // Linux provides kill process tree functionality
                if (parentPid != null) {
                    staticLogger.info("Canceling process tree where PID = " + parentPid + "");
                    killCommand(parentPid);
                    process.destroy();
                }
            }

            processMap.remove(key);
        // Else, cancel and remove remote process
        }

    }
    /**
     * Kills a process tree given a PID
     *
     * @param treeParentPid
     *            the PID
     */
    public void killCommand(Long treeParentPid) {

        String kCmd = PROCESS_TREE_KILL_COMMAND + treeParentPid.toString();

        try {
            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.indexOf("win") < 0) {

                ThreadedStreamReader inputReader = null;
                ThreadedStreamReader errorReader = null;
                Process process = null;
                try {

                    process = Runtime.getRuntime().exec(kCmd);

                    inputReader = new ThreadedStreamReader(process.getInputStream());
                    errorReader = new ThreadedStreamReader(process.getErrorStream());
                    Thread inputReaderThread = new Thread(inputReader);
                    Thread errorReaderThread = new Thread(errorReader);

                    inputReaderThread.start();
                    errorReaderThread.start();

                    inputReaderThread.join();
                    errorReaderThread.join();

                } catch (IOException e) {
                    staticLogger.error(e.toString());
                } catch (InterruptedException e) {
                    staticLogger.error(e.toString());
                } finally {

                    try {
                        if (process != null) {
                            process.waitFor();
                            staticLogger.info("Canceled process tree where PID = " + treeParentPid + "");
                        }
                    } catch (InterruptedException e) {
                        staticLogger.error(e.toString());
                    }
                }

            }
        } catch (Exception e) {
            staticLogger.error(e.toString());
        }

    }
}
