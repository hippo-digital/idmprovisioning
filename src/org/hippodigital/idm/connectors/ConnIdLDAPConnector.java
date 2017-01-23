package org.hippodigital.idm.connectors;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConfigurationProperties;
import org.identityconnectors.framework.api.ConfigurationProperty;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.impl.api.ConnectorInfoManagerFactoryImpl;

public class ConnIdLDAPConnector {

	public void synchroniseToLDAP(List<ConnectorObject> connectorObjects) {

		ConnectorInfoManagerFactoryImpl fact = (ConnectorInfoManagerFactoryImpl) ConnectorInfoManagerFactory
				.getInstance();
		File bundleDirectory = new File("/Users/nitinprabhu/jconnserv/bundles");
		URL url;
		try {
			url = IOUtil.makeURL(bundleDirectory, "net.tirasa.connid.bundles.ldap-1.4.0.jar");
			ConnectorInfoManager manager = fact.getLocalManager(url);
			ConnectorKey key = new ConnectorKey("net.tirasa.connid.bundles.ldap", "1.4.0",
					"net.tirasa.connid.bundles.ldap.LdapConnector");
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

			properties.setPropertyValue("host", "192.168.1.145");
			properties.setPropertyValue("port", 389);
			properties.setPropertyValue("ssl", false);
			properties.setPropertyValue("principal", "uid=administrator,cn=users,dc=sdhic,dc=xsdhis,dc=nhs,dc=uk");

			char[] clearChars = new String("Password1").toCharArray();
			GuardedString guardedString = new GuardedString(clearChars);
			properties.setPropertyValue("credentials", guardedString);

			String[] baseContexts = { "dc=sdhic,dc=xsdhis,dc=nhs,dc=uk" };
			properties.setPropertyValue("baseContexts", baseContexts);

			String[] accountObjectClasses = { "top", "person", "organizationalPerson", "inetOrgPerson" };
			properties.setPropertyValue("accountObjectClasses", accountObjectClasses);

			properties.setPropertyValue("dnAttribute", "dn");
			properties.setPropertyValue("uidAttribute", "dn");

			String[] attributesToSynchronize = { "FirstName", "Lastname", "Uid" };
			properties.setPropertyValue("attributesToSynchronize", attributesToSynchronize);

			// Use the ConnectorFacadeFactory's newInstance() method to get a
			// new connector.
			ConnectorFacade conn = ConnectorFacadeFactory.getInstance().newInstance(apiConfig);

			// Make sure we have set up the Configuration properly
			conn.validate();

			for (ConnectorObject connectorObject : connectorObjects) {

				Set<Attribute> attributes = new HashSet<Attribute>();

				for (Attribute attribute : connectorObject.getAttributes()) {
					if (attribute.getName().equalsIgnoreCase("__NAME__")
							|| attribute.getName().equalsIgnoreCase("__UID__")) {
						continue;
					}
					if (attribute.getName().equalsIgnoreCase("uid")) {
						String uid = attribute.getValue().toString().replaceAll("\\[", "").replaceAll("\\]", "");
						attributes.add(new Name("uid=" + uid + ",cn=users,dc=sdhic,dc=xsdhis,dc=nhs,dc=uk"));
					} else {
						attributes.add(AttributeBuilder.build(attribute.getName(), attribute.getValue()));
					}

				}

				Uid user = conn.create(ObjectClass.ACCOUNT, attributes, null);
				System.out.println(user.getUidValue());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
