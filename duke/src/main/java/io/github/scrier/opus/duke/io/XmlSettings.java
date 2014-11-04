package io.github.scrier.opus.duke.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlSettings {
	
	private static Logger log = LogManager.getLogger(XmlSettings.class);
	
	private String input;
	private InputStream filestream;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Map<String,String> params;
	
	public XmlSettings() {
		this.filestream = getClass().getResourceAsStream("/DefaultSettings.xml");
		params = new HashMap<String, String>();
	}
	
	public XmlSettings(String fileName) {
		input = fileName;
		params = new HashMap<String, String>();
	}
	
	public boolean init() {
		boolean retValue = true;
		try {
			if( null == this.filestream ) {
				File file = new File(this.input);
				if( true != file.exists() ) {
					log.error("File " + this.input + " doesn't exist.");
					retValue = false;
				} else {
					this.filestream = new FileInputStream(file);
				}
			}
			if( true == retValue ) {
				if( null == getDbFactory() ) {
					setDbFactory(DocumentBuilderFactory.newInstance());
				}
				if( null == getDBuilder() ) {
					setDBuilder(getDbFactory().newDocumentBuilder());
				}
				Document doc = getDBuilder().parse(this.filestream);
				doc.getDocumentElement().normalize();

				log.debug("root of xml file: " + doc.getDocumentElement().getNodeName());
				IterateNodes(doc.getDocumentElement(), "");
			}
		} catch(SAXException e) {
			log.error("SAXException", e);
			retValue = false;
		} catch(ParserConfigurationException e) {
			log.error("ParserConfigurationException", e);
			retValue = false;
		} catch(IOException e) {
			log.error("IOException", e);
			retValue = false;
		}
		return retValue;
	}
	
	/**
	 * Method to iterate through the xml nodes.
	 * @param node Node
	 * @param parent String
	 */
	private void IterateNodes(Node node, String parent) {
		if ( node.getNodeName().equals("setting") ) {
			params.put(node.getAttributes().getNamedItem("name").getNodeValue(), node.getTextContent());
		} else if ( node.getNodeName().equals("settings") ) {
			// do nothing, root node.
		} else {
			log.error("Unhandled nodename: " + node.getNodeName() + ".");
		}
		NodeList nodeList = node.getChildNodes();
		for( int i = 0; i < nodeList.getLength(); i++ ) {
			Node currentNode = nodeList.item(i);
			if( currentNode.getNodeType() == Node.ELEMENT_NODE ) {
				IterateNodes(currentNode, node.getNodeName());
			}
		}
	}

	/**
	 * @return the dbFactory
	 */
	public DocumentBuilderFactory getDbFactory() {
		return dbFactory;
	}

	/**
	 * @param dbFactory the dbFactory to set
	 */
	public void setDbFactory(DocumentBuilderFactory dbFactory) {
		this.dbFactory = dbFactory;
	}

	/**
	 * @return the dBuilder
	 */
	public DocumentBuilder getDBuilder() {
		return dBuilder;
	}

	/**
	 * @param dBuilder the dBuilder to set
	 */
	public void setDBuilder(DocumentBuilder dBuilder) {
		this.dBuilder = dBuilder;
	}
	
	public Map<String, String> getSettings() {
		return params;
	}
	
	@Override
	public String toString() {
		String output = "XmlSettings:[";
		boolean notFirst = false;
		for( Entry<String, String> item : params.entrySet() ) {
			if( notFirst ) {
				output += ",";
			} else {
				notFirst = true;
			}
			output += "{\"name\":\"" + item.getKey() + "\",";
			output += "\"value\":\"" + item.getValue() + "\"}";
		}
		output += "]";
		return output;
	}

}
