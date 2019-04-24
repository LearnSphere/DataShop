package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dto;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.cmu.pslc.datashop.item.MessageItem;

/** Help convert data from resource_use_oli_transaction to simpler and functional object 
 *  
 */

abstract public class AbstractOliResourceUseDTO implements OliResourceUseDTOInterface {
        protected long resourceUseTransactionId;
        protected String student;
        protected String session;
        protected Date UTCTime;
        protected String action;
        protected String info;
        protected String infoType;
        protected Integer prevTimeDiff;
        protected Integer nextTimeDiff;
        
        protected String javaDateFormatStr = "yyyy-MM-dd HH:mm:ss";
        protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat(javaDateFormatStr);
        
        public AbstractOliResourceUseDTO () {}
        
        public void setResourceUseTransactionId (long id) {
                this.resourceUseTransactionId = id;
        }
        public long getResourceUseTransactionId () {
                return this.resourceUseTransactionId;
        }
        
        public void setStudent (String student) {
                this.student = student;
        }
        public String getStudent () {
                return this.student;
        }
        
        public void setSession (String session) {
                this.session = session;
        }
        public String getSession () {
                return this.session;
        }
        
        public void setUTCTime (Date time) {
                this.UTCTime = time;
        }
        public Date getUTCTime () {
                return this.UTCTime;
        }
        
        public void setAction (String action) {
                this.action = action;
        }
        public String getAction () {
                return this.action;
        }
        
        public void setInfo (String info) {
                this.info = info;
        }
        public String getInfo () {
                return this.info;
        }
        
        public void setInfoType (String infoType) {
                this.infoType = infoType;
        }
        public String getInfoType () {
                return this.infoType;
        }
        
        public void setPrevTimeDiff (Integer prevTime) {
                this.prevTimeDiff = prevTime;
        }
        public Integer getPrevTimeDiff () {
                return this.prevTimeDiff;
        }
        public void calculatePrevTimeDiff (OliResourceUseDTOInterface prevObj) {
                if (UTCTime != null && prevObj.getUTCTime() != null && this.session.equals(prevObj.getSession())) {
                        long timeDiffInMilliseconds = this.UTCTime.getTime() - prevObj.getUTCTime().getTime();
                        this.prevTimeDiff = (int)(timeDiffInMilliseconds/1000);
                } else
                        this.prevTimeDiff = null;
        }
        
        public void setNextTimeDiff (Integer nextTime) {
                this.nextTimeDiff = nextTime;
        }
        public Integer getNextTimeDiff () {
                return this.nextTimeDiff;
        }
        public void calculateNextTimeDiff (OliResourceUseDTOInterface nextObj) {
                if (UTCTime != null && nextObj.getUTCTime() != null && this.session.equals(nextObj.getSession())) {
                        long timeDiffInMilliseconds = nextObj.getUTCTime().getTime() - this.UTCTime.getTime();
                        this.nextTimeDiff = (int)(timeDiffInMilliseconds/1000);
                } else
                        this.nextTimeDiff = null;
        }

        public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append("resourceUseTransactionId: " + resourceUseTransactionId + "\t");
                sb.append("student: " + student + "\t");
                sb.append("session: " + session + "\t");
                sb.append("UTCTime: " + simpleDateFormat.format(UTCTime) + "\t");
                sb.append("action: " + action + "\t");
                sb.append("info: " + info + "\t");
                sb.append("infoType: " + infoType + "\t");
                sb.append("prevTimeDiff: " + prevTimeDiff + "\t");
                sb.append("nextTimeDiff: " + nextTimeDiff + "\t");
                return sb.toString();
        }
}
