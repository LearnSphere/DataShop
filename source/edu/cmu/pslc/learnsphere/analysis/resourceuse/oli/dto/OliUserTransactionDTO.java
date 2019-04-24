package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.cmu.pslc.datashop.item.MessageItem;

/** Help convert data from resource_use_oli_transaction to simpler and functional object 
 *  
 */

public class OliUserTransactionDTO extends AbstractOliResourceUseDTO {
        public static String ACTION_VIEW_PAGE = "VIEW_PAGE";
        public static String ACTION_VIEW_MODULE_PAGE = "VIEW_MODULE_PAGE";
        public static String ACTION_START_SESSION = "START_SESSION";
        public static String ACTION_START_ATTEMPT = "START_ATTEMPT";
        public static String ACTION_EVALUATE_QUESTION = "EVALUATE_QUESTION";
        public static String ACTION_VIEW_HINT = "VIEW_HINT";
        public static String ACTION_SAVE_ATTEMPT = "SAVE_ATTEMPT";
        public static String ACTION_VIEW_PREFACE = "VIEW_PREFACE";
        public static String ACTION_SUBMIT_ATTEMPT = "SUBMIT_ATTEMPT";

        
        public static String COMBINED_START_SESSION_ATTEMPT = "COMBINED_START_SESSION_ATTEMPT";
        public static String COMBINED_START_ATTEMPT_VIEW = "COMBINED_START_ATTEMPT_VIEW";
        public static String COMBINED_VIEW_SAVE_ATTEMPT = "COMBINED_VIEW_SAVE_ATTEMPT";
        public static String COMBINED_START_ATTEMPT_VIEW_SAVE_ATTEMPT = "COMBINED_START_ATTEMPT_VIEW_SAVE_ATTEMPT";
        
        
        public static String ACTION_PLAY = "PLAY";
        public static String ACTION_MUTE = "MUTE";
        public static String ACTION_UNMUTE = "UNMUTE";
        public static String ACTION_STOP = "STOP";
        public static String ACTION_END = "END";
        
        public OliUserTransactionDTO () {}
                
        public boolean isViewPage () {
                if (action.equals(ACTION_VIEW_PAGE) ||
                                action.equals(ACTION_VIEW_MODULE_PAGE))
                        return true;
                else
                        return false;
        }
        
        public boolean isViewPreface () {
                if (action.equals(ACTION_VIEW_PREFACE))
                        return true;
                else
                        return false;
        }
        
        public boolean isSubmitAttempt () {
                if (action.equals(ACTION_SUBMIT_ATTEMPT))
                        return true;
                else
                        return false;
        }
        
        public boolean isPlainAction () {
                if (action.equals(ACTION_START_SESSION) ||
                                action.equals(ACTION_START_ATTEMPT) ||
                                action.equals(ACTION_EVALUATE_QUESTION) ||
                                action.equals(ACTION_VIEW_HINT) ||
                                action.equals(COMBINED_START_SESSION_ATTEMPT))
                        return true;
                else
                        return false;
        }
        
        public boolean isPlainActionStartSessionOrAttempt () {
                if (action.equals(ACTION_START_SESSION) ||
                                action.equals(ACTION_START_ATTEMPT) ||
                                action.equals(COMBINED_START_SESSION_ATTEMPT))
                        return true;
                else
                        return false;
        }
        
        public boolean isPlainMediaAction () {
                if (action.equals(ACTION_PLAY) ||
                                action.equals(ACTION_MUTE) ||
                                action.equals(ACTION_UNMUTE))
                        return true;
                else
                        return false;
        }
        
        public boolean isPlainMediaStopAction () {
                if (action.equals(ACTION_STOP) ||
                                action.equals(ACTION_END))
                        return true;
                else
                        return false;
        }
        
        
        public boolean isCombinedViewSaveAttemptAction () {
                if (action.equals(COMBINED_VIEW_SAVE_ATTEMPT) || action.equals(COMBINED_START_ATTEMPT_VIEW_SAVE_ATTEMPT))
                        return true;
                else
                        return false;
        }
        
        public boolean isPlainMediaPlayAction () {
                if (action.equals(ACTION_PLAY))
                        return true;
                else
                        return false;
        }

        public OliUserTransactionDTO combineTwoOLIIntermediateDataObject (OliResourceUseDTOInterface anotherObj) {
                if (!session.equals(anotherObj.getSession()))
                        return null;
                if (!(anotherObj instanceof OliUserTransactionDTO))
                        return null;
                OliUserTransactionDTO newObj = null;
                //combine Start_attempt and start_session or other action realted if time/info are the same 
                if (getUTCTime().equals(anotherObj.getUTCTime()) 
                                && getInfo().equals(anotherObj.getInfo())
                                && isPlainAction() && ((OliUserTransactionDTO)anotherObj).isPlainAction()) {
                        newObj = new OliUserTransactionDTO();
                        newObj.setStudent(student);
                        newObj.setSession(session);
                        newObj.setUTCTime(UTCTime);
                        newObj.setInfo(info);
                        newObj.setInfoType(infoType);
                        newObj.setAction(COMBINED_START_SESSION_ATTEMPT);
                } else if (getUTCTime().equals(anotherObj.getUTCTime()) 
                                && getInfo().equals(anotherObj.getInfo())
                                && ((isViewPage() && anotherObj.getAction().equals(ACTION_START_ATTEMPT))
                                                ||(action.equals(ACTION_START_ATTEMPT) && ((OliUserTransactionDTO)anotherObj).isViewPage()))) {
                        newObj = new OliUserTransactionDTO();
                        newObj.setStudent(student);
                        newObj.setSession(session);
                        newObj.setUTCTime(UTCTime);
                        newObj.setInfo(info);
                        newObj.setInfoType(infoType);
                        newObj.setAction(COMBINED_START_ATTEMPT_VIEW);
                } else if (getAction().equals(ACTION_SAVE_ATTEMPT)
                                && (anotherObj.getAction().equals(ACTION_VIEW_PAGE) || anotherObj.getAction().equals(COMBINED_START_ATTEMPT_VIEW))
                                && getInfo().equals(anotherObj.getInfo())) {
                        newObj = new OliUserTransactionDTO();
                        newObj.setStudent(student);
                        newObj.setSession(session);
                        newObj.setUTCTime(UTCTime);
                        newObj.setInfo(info);
                        newObj.setInfoType(infoType);
                        if (anotherObj.getAction().equals(ACTION_VIEW_PAGE))
                                newObj.setAction(COMBINED_VIEW_SAVE_ATTEMPT);
                        else if (anotherObj.getAction().equals(COMBINED_START_ATTEMPT_VIEW))
                                newObj.setAction(COMBINED_START_ATTEMPT_VIEW_SAVE_ATTEMPT);
                }
                if (newObj != null) {
                        if (newObj.isCombinedViewSaveAttemptAction()) {
                                if (getPrevTimeDiff() != null && anotherObj.getPrevTimeDiff() != null) {
                                        newObj.setPrevTimeDiff(getPrevTimeDiff() + anotherObj.getPrevTimeDiff());
                                } else {
                                        if (getPrevTimeDiff() != null)
                                                newObj.setPrevTimeDiff(getPrevTimeDiff());
                                        else if (anotherObj.getPrevTimeDiff() != null)
                                                newObj.setPrevTimeDiff(anotherObj.getPrevTimeDiff());
                                        else
                                                newObj.setPrevTimeDiff(0);
                                }
                                if (getNextTimeDiff() != null)
                                        newObj.setNextTimeDiff(getNextTimeDiff());
                                else
                                        newObj.setNextTimeDiff(0);
                        } else {
                                if (getPrevTimeDiff() == null || anotherObj.getPrevTimeDiff() == null) {
                                        if (getPrevTimeDiff() != null)
                                                newObj.setPrevTimeDiff(getPrevTimeDiff());
                                        else if (anotherObj.getPrevTimeDiff() != null)
                                                newObj.setPrevTimeDiff(anotherObj.getPrevTimeDiff());
                                } else {
                                        newObj.setPrevTimeDiff(getPrevTimeDiff() >= anotherObj.getPrevTimeDiff() ? getPrevTimeDiff() : anotherObj.getPrevTimeDiff());
                                }
                                
                                if (getNextTimeDiff() == null || anotherObj.getNextTimeDiff() == null) {
                                        if (getNextTimeDiff() != null)
                                                newObj.setNextTimeDiff(getNextTimeDiff());
                                        else if (anotherObj.getNextTimeDiff() != null)
                                                newObj.setNextTimeDiff(anotherObj.getNextTimeDiff());
                                } else {
                                        newObj.setNextTimeDiff(getNextTimeDiff() >= anotherObj.getNextTimeDiff() ? getNextTimeDiff() : anotherObj.getNextTimeDiff());
                                }
                        }
                }
                return newObj;
        }
        
}
