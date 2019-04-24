/*
 * Carnegie Mellon University, Human Computer Interaction Institute
 * Copyright 2009
 * All Rights Reserved
 */
package edu.cmu.learnsphere.dto;

import java.util.Date;
import java.util.List;

import edu.cmu.pslc.datashop.dto.DTO;
import edu.cmu.pslc.datashop.dto.DatasetDTO;
import edu.cmu.pslc.datashop.item.DatasetItem;
import static edu.cmu.pslc.datashop.util.CollectionUtils.checkNull;

/**
 * Used to transfer connections data as XML, JSON, etc.
 *
 * @author Hui Cheng
 * @version $Revision: 15485 $
 * <BR>Last modified by: $Author: hcheng $
 * <BR>Last modified on: $Date: 2018-09-26 10:05:46 -0400 (Wed, 26 Sep 2018) $
 * <!-- $KeyWordsOff: $ -->
 */
@DTO.Properties(root = "connections",
                properties = {"connections" })
public class WorkflowConnectionsDTO extends DTO {
        /** annotations */
        private List<WorkflowConnectionDTO> connections;
    
    
        /** this component's connections. @param connections this component's connections */
        public void setConnections(List<WorkflowConnectionDTO> connections) { this.connections = connections; }

        /** this component's connections. @return the connections */
        public List<WorkflowConnectionDTO> getConnections() {
                connections = checkNull(connections);
            return connections;
        }

        /** Add connection. @param WorkflowConnectionDTO */
        public void addConnection(WorkflowConnectionDTO connection) { getConnections().add(connection); }
       
}
