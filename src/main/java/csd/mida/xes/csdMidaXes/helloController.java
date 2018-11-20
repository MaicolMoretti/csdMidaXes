package csd.mida.xes.csdMidaXes;

import org.apache.tomcat.jni.Time;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class helloController {

    private static final String template = "Hello, %s !";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/hello", produces = "text/xml; charset=utf-8")

    public String hello(@RequestParam(value="name", defaultValue = "World") String name) {

        // Create document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = docBuilder.newDocument();

        // Root element
        Element log = doc.createElement("log");
        log.setAttribute("xes.version", "1.0");
        log.setAttribute("xes.features", "nested-attributes");
        log.setAttribute("openxes.version", "1.0RC7");
        log.setTextContent(name);
        doc.appendChild(log);

        generateHeaders(doc, log);
        generateTestTrace(doc, log);


        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            System.out.println(writer.getBuffer().toString());
            return writer.getBuffer().toString();


        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        return new String ("we couldn't make ");
        //return new hello(counter.incrementAndGet(),
          //                  String.format(template, name));
    }

    private static Document convertStringToXMLDocument(String xmlString)
    {
        //Parser that produces DOM object trees from XML content
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = null;
        try
        {
            //Create DocumentBuilder with default configuration
            builder = factory.newDocumentBuilder();

            //Parse the content to Document object
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private void generateHeaders(Document doc, Element log) {

        /*
        <extension name="Concept" prefix="concept" uri="http://code.deckfour.org/xes/concept.xesext"/>
	    <extension name="Time" prefix="time" uri="http://code.deckfour.org/xes/time.xesext"/>
         */


        Element extensionConcept = doc.createElement("extension");
        extensionConcept.setAttribute("name", "Concept");
        extensionConcept.setAttribute("prefix", "concept");
        extensionConcept.setAttribute("uri", "http://code.deckfour.org/xes/concept.xesext");

        Element extensionTime = doc.createElement("extension");
        extensionTime.setAttribute("name", "Time");
        extensionTime.setAttribute("prefix", "time");
        extensionTime.setAttribute("uri", "http://code.deckfour.org/xes/time.xesext");

        log.appendChild(extensionConcept);
        log.appendChild(extensionTime);

        /*
        * <global scope="trace">
		        <string key="concept:name" value="name"/>
	        </global>
	<global scope="event">
		<string key="concept:name" value="name"/>
		<date key="time:timestamp" value="2011-04-13T14:02:31.199+02:00"/>
	</global>
	*/
        Element globalTrace = doc.createElement("global");
        globalTrace.setAttribute("scope", "trace");

        Element globalTraceChild = doc.createElement("string");
        globalTraceChild.setAttribute("key", "concept:name");
        globalTraceChild.setAttribute("value","name");

        globalTrace.appendChild(globalTraceChild);
        log.appendChild(globalTrace);

        Element globalEvent = doc.createElement("global");
        globalEvent.setAttribute("scope", "event");

        Element globalEventChild1 = doc.createElement("string");
        globalEventChild1.setAttribute("key", "concept:name");
        globalEventChild1.setAttribute("value", "name");

        Element globalEventChild2 = doc.createElement("string");
        globalEventChild2.setAttribute("key", "time:timestamp");
        globalEventChild2.setAttribute("value", "2011-04-13T14:02:31.199+02:00");

        globalEvent.appendChild(globalEventChild1);
        globalEvent.appendChild(globalEventChild2);

        log.appendChild(globalEvent);

    }

    private void generateTestTrace(Document doc, Element log) {

        Element trace = doc.createElement("trace");
        Element trace2 = doc.createElement("trace");

        generateEvent(doc, trace);
        generateEvent(doc, trace2);

        log.appendChild(trace);
    }

    private void generateEvent(Document doc, Element trace) {
        Element event1 = doc.createElement("hahahaha");
        Element event2 = doc.createElement("event");

        generateEventAttributes(doc, event1);
        generateEventAttributes(doc, event2);

        trace.appendChild(event1);
        trace.appendChild(event2);

    }

    private void generateEventAttributes(Document doc, Element event) {
        Element eventChild1 = doc.createElement("string");
        eventChild1.setAttribute("key", "concept:name");
        eventChild1.setAttribute("value", "cane");

        Element eventChild2= doc.createElement("string");
        eventChild2.setAttribute("key", "time:timestamp");
        Date date = new Date();
        eventChild2.setAttribute("value", String.valueOf(date.getTime()));

        event.appendChild(eventChild1);
        event.appendChild(eventChild2);

    }


}
