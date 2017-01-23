package org.hippodigital.idm.connectors;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.IOUtil;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.impl.api.ConnectorInfoManagerFactoryImpl;

public class ConnIdCSVConnector {

	public List<ConnectorObject> loadCSVDataUsingConnector() {
		ConnectorInfoManagerFactoryImpl fact = (ConnectorInfoManagerFactoryImpl) ConnectorInfoManagerFactory
				.getInstance();
		File bundleDirectory = new File("/Users/nitinprabhu/jconnserv/bundles");
		URL url;
		final List<ConnectorObject> results = new ArrayList<>();
		try {
			url = IOUtil.makeURL(bundleDirectory, "net.tirasa.connid.bundles.csvdir-0.8.5.jar");
			ConnectorInfoManager manager = fact.getLocalManager(url);
			ConnectorKey key = new ConnectorKey("net.tirasa.connid.bundles.csvdir", "0.8.5",
					"net.tirasa.connid.bundles.csvdir.CSVDirConnector");
			ConnectorInfo info = manager.findConnectorInfo(key);

			// From the ConnectorInfo object, create the default
			// APIConfiguration.
			APIConfiguration apiConfig = info.createDefaultAPIConfiguration();

			// From the default APIConfiguration, retrieve the
			// ConfigurationProperties.
			ConfigurationProperties properties = apiConfig.getConfigurationProperties();

			// Print out what the properties are (not necessary)
			List<String> propertyNames = properties.getPropertyNames();
			for (String propName : propertyNames) {
				ConfigurationProperty prop = properties.getProperty(propName);
				System.out.println("Property Name: " + prop.getName() + "\tProperty Type: " + prop.getType());
			}

			// Set all of the ConfigurationProperties needed by the connector.
			properties.setPropertyValue("sourcePath", "/Users/nitinprabhu/POC/Provisioning");
			properties.setPropertyValue("fileMask", "users.*\\.csv");

			String[] keyColumnNames = { "uid" };
			properties.setPropertyValue("keyColumnNames", keyColumnNames);

			String[] fields = { "cn", "sn", "displayName", "employeeType", "homePhone", "homePostalAddress", "uid" };
			properties.setPropertyValue("fields", fields);

			properties.setPropertyValue("quotationRequired", true);

			// Use the ConnectorFacadeFactory's newInstance() method to get a
			// new connector.
			ConnectorFacade conn = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);

			// Make sure we have set up the Configuration properly
			conn.validate();

			ResultsHandler handler = new ResultsHandler() {
				public boolean handle(ConnectorObject obj) {
					results.add(obj);
					return true;
				}
			};

			conn.search(ObjectClass.ACCOUNT, null, handler, null);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;

	}
}
