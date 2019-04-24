package edu.cmu.pslc.learnsphere.analysis.resourceuse.oli.dto;

import java.util.Date;

/** Interface that helps convert data from resource_use_oli_transaction to simpler and functional object 
 *  
 */

public interface OliResourceUseDTOInterface {
        public String getStudent ();
        public Date getUTCTime ();
        public String getSession ();
        public String getInfo ();
        public String getInfoType ();
        public String getAction ();
        public Integer getPrevTimeDiff ();
        public Integer getNextTimeDiff ();
        public void calculatePrevTimeDiff (OliResourceUseDTOInterface prevObj);
        public void calculateNextTimeDiff (OliResourceUseDTOInterface nextObj);
        public OliResourceUseDTOInterface combineTwoOLIIntermediateDataObject (OliResourceUseDTOInterface anotherObj);
        public String toString();
}
