package org.hippodigital.idm.provisioner.service;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.hippodigital.idm.connectors.ConnIdCSVConnector;
import org.hippodigital.idm.connectors.ConnIdLDAPConnector;
import org.hippodigital.idm.dto.Response;
import org.identityconnectors.framework.common.objects.ConnectorObject;

@Path("/restservice")
public class ReconcileCSVToLDAP {
	
	@GET
	@Path("/csvtoldap")
	public javax.ws.rs.core.Response reconcileCSVToLDAP(){
		
		ConnIdCSVConnector connIdCSVConnector = new ConnIdCSVConnector();
		List<ConnectorObject> connectorObjects = connIdCSVConnector.loadCSVDataUsingConnector();
		
		ConnIdLDAPConnector connIdLDAPConnector=new ConnIdLDAPConnector();
		connIdLDAPConnector.synchroniseToLDAP(connectorObjects);
		
		Response response=new Response();
		response.setMessage("Successfully provisioned CSV file  data to OpenLAP");
		response.setStatus("Success");
		
		
		return javax.ws.rs.core.Response.status(200).entity("Successfully provisioned CSV file  data to OpenLAP").build()  ;
		

	}
	
	
}
