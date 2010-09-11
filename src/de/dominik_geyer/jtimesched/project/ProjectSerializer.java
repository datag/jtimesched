package de.dominik_geyer.jtimesched.project;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import de.dominik_geyer.jtimesched.JTimeSchedApp;

public class ProjectSerializer {
	private String filename;
	
	public ProjectSerializer(String filename) {
		this.filename = filename;
	}
	

	public void writeXml(List<Project> projects) throws TransformerConfigurationException, SAXException, IOException {
		OutputStreamWriter out = new OutputStreamWriter(
				new FileOutputStream(filename), "UTF8");
		StreamResult streamResult = new StreamResult(out);
		SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6296446
		tf.setAttribute("indent-number", new Integer(4));

		TransformerHandler hd = tf.newTransformerHandler();
		Transformer serializer = hd.getTransformer();
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		//serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "projects.dtd");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		hd.setResult(streamResult);
		hd.startDocument();
		AttributesImpl atts = new AttributesImpl();
		
		addXmlAttribute(atts, "version", JTimeSchedApp.APP_VERSION);
		startXmlElement(hd, "projects", atts);

		for (Project p: projects)
		{
		  startXmlElement(hd, "project", null);
		  
		  addXmlElement(hd, "title", null, p.getTitle());
		  addXmlElement(hd, "created", null, new Long(p.getTimeCreated().getTime()));
		  addXmlElement(hd, "started", null, new Long(p.getTimeStart().getTime()));
		  addXmlElement(hd, "running", null, p.isRunning() ? "yes" : "no");
		  addXmlElement(hd, "checked", null, p.isChecked() ? "yes" : "no");
		  
		  atts.clear();
		  addXmlAttribute(atts, "overall", new Integer(p.getSecondsOverall()));
		  addXmlAttribute(atts, "today", new Integer(p.getSecondsToday()));
		  addXmlElement(hd, "time", atts, null);
		  
		  Color color = p.getColor();
		  if (color != null) {
			  atts.clear();
			  addXmlAttribute(atts, "red", new Integer(color.getRed()));
			  addXmlAttribute(atts, "green", new Integer(color.getGreen()));
			  addXmlAttribute(atts, "blue", new Integer(color.getBlue()));
			  addXmlAttribute(atts, "alpha", new Integer(color.getAlpha()));
			  addXmlElement(hd, "color", atts, null);
		  }
		  
		  endXmlElement(hd, "project");
		}
		endXmlElement(hd, "projects");
		
		hd.endDocument();
		
		out.close();
	}
	
	public ArrayList<Project> readXml() throws ParserConfigurationException, SAXException, IOException {
		ArrayList<Project> arPrj = new ArrayList<Project>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(this.filename));
		
		Element root = document.getDocumentElement();
		NodeList nl = root.getElementsByTagName("project");
		//NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Element pe = (Element) nl.item(i);
			
			Project p = new Project();
			
			Element e;
			e = getFirstElement(pe, "title");
			p.setTitle(e.getFirstChild().getNodeValue());
			
			e = getFirstElement(pe, "created");
			long ts = Long.parseLong(e.getFirstChild().getNodeValue());
			p.setTimeCreated(new Date(ts));
			
			e = getFirstElement(pe, "started");
			ts = Long.parseLong(e.getFirstChild().getNodeValue());
			p.setTimeStart(new Date(ts));
			
			e = getFirstElement(pe, "running");
			p.setRunning((e.getFirstChild().getNodeValue().equals("yes")) ? true : false);
			
			e = getFirstElement(pe, "checked");
			p.setChecked((e.getFirstChild().getNodeValue().equals("yes")) ? true : false);
			
			e = getFirstElement(pe, "time");
			int seconds = Integer.parseInt(e.getAttribute("overall"));
			p.setSecondsOverall(seconds);
			seconds = Integer.parseInt(e.getAttribute("today"));
			p.setSecondsToday(seconds);
			
			NodeList pnl;
			pnl = pe.getElementsByTagName("color");
			if (pnl.getLength() != 0) {
				e = (Element) pnl.item(0);
				int r = Integer.parseInt(e.getAttribute("red"));
				int g = Integer.parseInt(e.getAttribute("green"));
				int b = Integer.parseInt(e.getAttribute("blue"));
				int a = Integer.parseInt(e.getAttribute("alpha"));
				
				p.setColor(new Color(r, g, b, a));
			}
			
			arPrj.add(p);
			System.out.println(p);
		}
		
		return arPrj;
	}
	
	protected Element getFirstElement(Element e, String name) {
		return ((Element) e.getElementsByTagName(name).item(0));
	}
	
	protected static void startXmlElement(TransformerHandler hd, String element, AttributesImpl atts) throws SAXException {
		if (atts == null)
			atts = new AttributesImpl();
		hd.startElement("", "", element, atts);
	}
	
	protected static void endXmlElement(TransformerHandler hd, String element) throws SAXException {
		hd.endElement("", "", element);
	}
	
	protected static void addXmlElement(TransformerHandler hd, String element, AttributesImpl atts, Object data) throws SAXException {
		if (atts == null)
			atts = new AttributesImpl();
		hd.startElement("", "", element, atts);
		if (data != null) {
			String strData = data.toString();
			hd.characters(strData.toCharArray(), 0, strData.length());
		}
		hd.endElement("", "", element);
	}
	
	protected static void addXmlAttribute(AttributesImpl atts, String attribute, Object data) {
		atts.addAttribute("", "", attribute, "CDATA", data.toString());
	}
}
