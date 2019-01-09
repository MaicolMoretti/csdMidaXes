package csd.mida.xes.csdMidaXes;

import org.springframework.web.bind.annotation.CrossOrigin;
//*****************REQUIREMENTS******************
//Spring
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
//EventModel
import csd.mida.xes.csdMidaXes.models.EventModel;
//XML tools
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
//Streaming parser XML
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
//Java standard library
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@RestController
public class helloController {

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/hello", produces = "text/xml; charset=utf-8")

    public String hello(@RequestBody String jsonLog) {


        //EventModel event = new ObjectMapper().readValue(jsonLog, EventModel.class);
        //System.out.println(event.conceptName);
        ObjectMapper mapper = new ObjectMapper();
        try {

            //Crea una lista di eventi letti dal JSON fornito dalla richiesta
            List<EventModel> myEvents = mapper.readValue(jsonLog, new TypeReference<List<EventModel>>() {});

            //Mostra ogni evento acquisito
            for (EventModel event : myEvents)
                System.out.println(event.conceptName + "   " + event.type + "   " + event.lifecycleTransition);


            // Create document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            try {
                docBuilder = docFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }


            // Creation of a document
            Document doc = docBuilder.newDocument();


            // Root element
            Element log = this.generateRoot(doc);

            //Headers element
            this.generateHeaders(doc, log);


            //Genero eventi acquisiti
            ArrayList<Element> events1 = new ArrayList<>();
            for (int i = 0; i < myEvents.size(); i++)
                events1.add(generateEvent(doc, myEvents.get(i)));

            //Genero tracce
            generateTrace(doc, log, events1);


            TransformerFactory tf = TransformerFactory.newInstance();
            try {
                Transformer transformer = tf.newTransformer();
                StringWriter writer = new StringWriter();
                transformer.transform(new DOMSource(doc), new StreamResult(writer));
                return writer.getBuffer().toString();


            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return new String("we couldn't make ");
        //return new hello(counter.incrementAndGet(), String.format(template, name));
    }


    /**
     * Genero root (tag log)
     *
     * @param doc documento di riferimento
     * @return log generato
     */
    private Element generateRoot(Document doc) {
        Element log = doc.createElement("log");
        log.setAttribute("xes.version", "1.0");
        log.setAttribute("xes.features", "nested-attributes");
        log.setAttribute("openxes.version", "1.0RC7");
        doc.appendChild(log);
        return log;
    }

    /**
     * Genero gli header (extension and classifier)
     * TODO Aggiungi classifier
     *
     * @param doc documento di riferimento
     * @param log log di riferimento
     */
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
           <global scope="trace">
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
        globalTraceChild.setAttribute("value", "");

        globalTrace.appendChild(globalTraceChild);
        log.appendChild(globalTrace);

        Element globalEvent = doc.createElement("global");
        globalEvent.setAttribute("scope", "event");

        Element globalEventChild1 = doc.createElement("string");
        globalEventChild1.setAttribute("key", "concept:name");
        globalEventChild1.setAttribute("value", "");

        Element globalEventChild2 = doc.createElement("string");
        globalEventChild2.setAttribute("key", "type");
        globalEventChild2.setAttribute("value", "Send Message Task");

        Element globalEventChild3 = doc.createElement("date");
        globalEventChild3.setAttribute("key", "time:timestamp");
        globalEventChild3.setAttribute("value", "2011-04-13T14:02:31.199+02:00");

        globalEvent.appendChild(globalEventChild1);
        globalEvent.appendChild(globalEventChild2);
        globalEvent.appendChild(globalEventChild3);

        log.appendChild(globalEvent);

        /*
        <classifier name="Event Name" keys="concept:name"/>
	    <classifier name="(Event Name AND Lifecycle transition)" keys="concept:name time:timestamp"/>
	    */

        Element classifierTime = doc.createElement("classifier");
        classifierTime.setAttribute("name", "Event Name");
        classifierTime.setAttribute("key", "concept:name");


        Element classifier2Time = doc.createElement("classifier");
        classifier2Time.setAttribute("name", "Event Name and TimeStamp");
        classifier2Time.setAttribute("key", "concept:name time:timestamp");


        log.appendChild(classifierTime);
        log.appendChild(classifier2Time);


    }


    /**
     * Genero una traccia
     *
     * @param doc    documento di riferimento
     * @param log    log di riferimento
     * @param events eventi da aggiungere alla traccia
     */
    private void generateTrace(Document doc, Element log, ArrayList<Element> events) {
        Element trace = doc.createElement("trace");
        Element traceChild = doc.createElement("string");
        traceChild.setAttribute("key", "concept:name");
        traceChild.setAttribute("value", "Trace 1");
        trace.appendChild(traceChild);
        for (Element event : events)
            trace.appendChild(event);
        log.appendChild(trace);
    }

    /**
     * Genero un evento
     *
     * @param doc documento di riferimento
     * @return evento generato
     */
    private Element generateEvent(Document doc, EventModel myEvent) {
        Element event = doc.createElement("event");
        ArrayList<Element> eventAttributes = generateEventAttributes(doc, myEvent);
        for (Element eventAttribute : eventAttributes)
            event.appendChild(eventAttribute);
        return event;
    }

    /**
     * Genero gli attributi di un evento
     *
     * @param doc documento di riferimento
     */
    private ArrayList<Element> generateEventAttributes(Document doc, EventModel myEvent) {
        /*
        EXAMPLE
        <event>
			<string key="concept:instance" value="0"/>
			<string key="lifecycle:transition" value="complete"/>
			<date key="time:timestamp" value="2014-09-04T10:19:00.000+02:00"/>
			<string key="concept:name" value="Generazione-132"/>
		</event>
         */
        Element eventChild1 = doc.createElement("string");
        eventChild1.setAttribute("key", "concept:name");
        eventChild1.setAttribute("value", myEvent.conceptName);

        Element eventChild2 = doc.createElement("string");
        eventChild2.setAttribute("key", "type");
        eventChild2.setAttribute("value", myEvent.type);

        Element eventChild3 = doc.createElement("date");
        eventChild3.setAttribute("key", "time:timestamp");
        eventChild3.setAttribute("value", myEvent.lifecycleTransition);

        Element eventChild4 = doc.createElement("string");
        eventChild4.setAttribute("key", "id");
        eventChild4.setAttribute("value", myEvent.id);


        ArrayList<Element> eventAttributes = new ArrayList<>();
        eventAttributes.add(eventChild4);
        eventAttributes.add(eventChild1);
        eventAttributes.add(eventChild2);
        eventAttributes.add(eventChild3);
        return eventAttributes;

    }
}
