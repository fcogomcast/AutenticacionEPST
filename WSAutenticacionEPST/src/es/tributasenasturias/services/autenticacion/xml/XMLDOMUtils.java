/**
 * 
 */
package es.tributasenasturias.services.autenticacion.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



/**
 * @author crubencvs
 * Paquete con utilidades varias de XML.
 */
public class XMLDOMUtils {
	
	private static final String ERR_XML_TXT = "Imposible devolver la representación en texto de este XML:";
	private static final String ERROR_AL_INTERPRETAR_EL_CODIGO_XML = "Error al interpretar el código xml.";
	private XMLDOMUtils(){};
	/**
	 * Recupera el valor del atributo "nombreAtributo" del nodo "nodo".
	 * @param nodo
	 * @param nombreAtributo
	 * @return Valor del atributo o nulo si no se ha encontrado.
	 */
	public static String getAttributeValue (Node nodo, String nombreAtributo)
	{
		NamedNodeMap attrs=null;
		Node att=null;
		String valor=null;
		attrs = nodo.getAttributes();
		if (attrs!=null)
		{
			att = attrs.getNamedItem(nombreAtributo);
			if (att!=null)
			{
				valor = att.getNodeValue();
			}
		}
		return valor;
	}

	/**
	 * Recupera el nodo que contiene el valor (nodo Texto | CData)
	 * @param nodo Nodo del que se quiere recuperar el nodo valor.
	 * @return
	 */
	private static Node getNodoValor (Node nodo)
	{
		Node nodoValor=null;
		Node hijo=null;
		if (nodo!=null && nodo.hasChildNodes())
		{
			for (hijo=nodo.getFirstChild();hijo!=null;hijo=hijo.getNextSibling())
			{
				if (hijo.getNodeType()==Node.TEXT_NODE||hijo.getNodeType()==Node.CDATA_SECTION_NODE)
				{
					nodoValor = hijo;// El texto del nodo está en el nodo texto hijo.
					break;
				}
			}
		}
		return nodoValor;
	}
	/**
	 * Recupera el texto de un nodo.
	 * @param nodo
	 * @return El valor del nodo, o null si no tiene valor.
	 */
	public static String getNodeText(Node nodo)
	{
		String valor=null;
		Node hijo=null;
		if (nodo!=null && nodo.hasChildNodes())
		{
			for (hijo=nodo.getFirstChild();hijo!=null;hijo=hijo.getNextSibling())
			{
				if (hijo.getNodeType()==Node.TEXT_NODE||hijo.getNodeType()==Node.CDATA_SECTION_NODE)
				{
					valor = hijo.getNodeValue();// El texto del nodo está en el nodo texto hijo.
					break;
				}
			}
		}
		return valor;
	}
	/**
	 * Recupera el nesimo nodo con el tag indicado que sea hijo del nodo pasado por parámetro.
	 * @param padre Nodo padre en el que se buscarán sus hijos.
	 * @param tag Tag del nodo hijo a buscar.
	 * @param occur Número de ocurrencia del nodo (1,2,3, para primero, segundo, tercero, etc).
	 * @return Nodo hijo o null si no se ha encontrado.
	 */
	public static Node getNthChildNode (Node padre, String tag, int occur)
	{
		Node res=null;
		Node hijo=null;
		int i=1;
		if (padre!=null && padre.hasChildNodes() && padre.getChildNodes().getLength()>=occur)
		{   for (hijo=padre.getFirstChild();hijo!=null && i<=occur ;hijo=hijo.getNextSibling())
			{	
				if (hijo.getNodeType()==Node.ELEMENT_NODE)
				{
					if (hijo.getLocalName().equalsIgnoreCase(tag) && (i==occur))
					{
						res = hijo;// El texto del nodo está en el nodo texto hijo.
						break;
					}
					else if (hijo.getLocalName().equalsIgnoreCase(tag))
					{
						i++;
					}
				}
			}
		}
		return res;
	}
	/**
	 * Recupera el primer nodo hijo con ese tag.
	 * @param padre Nodo padre.
	 * @param tag Etiqueta del nodo hijo a buscar.
	 * @return Primer hijo con esa etiqueta o null si no se ha encontrado.
	 */
	public static Node getFirstChildNode(Node padre, String tag)
	{
		Node res=null;
		res = getNthChildNode (padre, tag,1);
		return res;
	}
	/**
	 * Recupera la lista de nodos con ese nombre en cualquier Namespace.
	 * @param doc Documento en el que buscar.
	 * @param tagNodo Etiqueta del nodo
	 * @return Lista de nodos DOM cuya etiqueta coincida.
	 */
	public static NodeList getAllNodes (Document doc, String tagNodo)
	{
		NodeList list=null;
		try
		{
			//Compilamos una expresión XPath que devuelva todos los nodos con el tag, en cualquier parte del documento.
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression exp = xpath.compile("//*[local-name()='"+tagNodo+"']");
			list = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);
		}
		catch (XPathExpressionException ex)
		{
			list=null;
		}
		return list;
	}
	/**
	 * Recupera la lista de nodos que cumplen una condición XPATH.
	 * @param doc Documento en el que buscar.
	 * @param condicion Condición a tener en cuenta.
	 * @return Lista de nodos DOM que cumplan esa condicion coincida.
	 */
	public static NodeList getAllNodesCondicion (Document doc, String condicion)
	{
		NodeList list=null;
		try
		{
			//Compilamos una expresión XPath que devuelva todos los nodos con la condición
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression exp = xpath.compile(condicion);
			list = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);
		}
		catch (XPathExpressionException ex)
		{
			list=null;
		}
		return list;
	}
	/**
	 * Recupera la lista de nodos con ese nombre en el namespace especificado.
	 * @param doc Documento en el que buscar
	 * @param tagNodo Tag del nodo
	 * @param namespace URI del namespace en el que se buscará el nodo.
	 * @return
	 */
	public static NodeList getAllNodesNS (Document doc, String tagNodo, String namespace)
	{
		NodeList list=null;
		try
		{
			//Compilamos una expresión XPath que devuelva todos los nodos con el tag, en cualquier parte del documento.
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression exp = xpath.compile("//*[local-name()='"+tagNodo+"' and namespace-uri()='"+namespace+"']");
			list = (NodeList)exp.evaluate(doc, XPathConstants.NODESET);
		}
		catch (XPathExpressionException ex)
		{
			list=null;
		}
		return list;
	}
	/**
	 * Inserta el texto en el nodo.
	 * @param doc Documento en el que se encuentra el texto a insertar.
	 * @param nodo Nodo en el que insertar texto (en un nodo hijo )
	 * @param texto  Texto.
	 */
	public static  void setNodeText (Document doc,Node nodo, String texto)
	{
		Node valorAnterior = getNodoValor (nodo);
		if (valorAnterior != null) //Existía un nodo previo con valor
		{
			valorAnterior.setNodeValue(texto);
		}
		else
		{
			Node textN = doc.createTextNode(texto);
			nodo.appendChild(textN);
		}
	}
	
	public static Node selectSingleNode (Node nodo, String path)
	{
		NodeList list;
		Node objetivo;
		try
		{
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression exp = xpath.compile(path);
			list = (NodeList)exp.evaluate (nodo, XPathConstants.NODESET);
			if (list.getLength()>0)
			{
				objetivo=list.item(0);//El primero
			}
			else
			{
				objetivo=null;
			}
		}
		catch (XPathExpressionException ex)
		{
			objetivo=null;
		}
		return objetivo;
	}
	
	/**
	 * Inserta el texto en el nodo.
	 * @param doc Documento en el que se encuentra el texto a insertar.
	 * @param ruta ruta del nodo en el que insertar texto (en un nodo hijo )
	 * @param texto  Texto.
	 */
	public static  void setNodeText (Document doc,String ruta, String texto)
	{
		Node nodo=selectSingleNode (doc, ruta);
		if (nodo!=null)
		{
			setNodeText(doc,nodo,texto);
		}
	}
	
	/**
	 * Parsea un xml que se incluye a través del InputStream, y lo asigna al objeto documento 
	 * @param iXML InputStream que se haya creado sobre el XML.
	 * @return Documento xml.
	 * @throws XMLDOMDocumentException
	 */
	public static Document parseXML (InputStream iXML) throws XMLDOMDocumentException
	{
		javax.xml.parsers.DocumentBuilderFactory fact;
		Document doc=null;
		javax.xml.parsers.DocumentBuilder db;
		try
		{
			fact = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			db= fact.newDocumentBuilder();
			doc = db.parse(iXML);
			return doc;
		}
		catch (javax.xml.parsers.ParserConfigurationException ex)
		{
			throw new XMLDOMDocumentException(ERROR_AL_INTERPRETAR_EL_CODIGO_XML,ex);
		}
		catch (org.xml.sax.SAXException ex)
		{
			throw new XMLDOMDocumentException(ERROR_AL_INTERPRETAR_EL_CODIGO_XML,ex);
		}
		catch (java.io.IOException ex)
		{
			throw new XMLDOMDocumentException(ERROR_AL_INTERPRETAR_EL_CODIGO_XML,ex);
		}
	}
	/**
	 * Parsea un xml que se incluye a través de fichero, y lo asigna al objeto documento 
	 * @param doc Objeto de tipo documento que se creará.
	 * @param pFicheroXML. Fichero que contiene el XML que se parseará.
	 * @return Documento xml
	 * @throws XMLDOMDocumentException
	 */
	public static Document parseXML (File pFicheroXML) throws XMLDOMDocumentException
	{
		try
		{
			Document doc=null;
			if (pFicheroXML !=null)
			{
				FileInputStream fs = new FileInputStream(pFicheroXML);
				doc = parseXML(fs);
				return doc;
			}
			else 
			{
				throw new XMLDOMDocumentException ("No se ha indicado fichero a tratar.");
			}
		}
		catch (FileNotFoundException ex)
		{
			throw new XMLDOMDocumentException ("No existe el fichero a tratar o no se puede leer.",ex);
		}
	}
	
	/**
	 * Parsea un xml que se incluye a través de un string, y lo asigna al objeto documento 
	 * @param pXml. String que contiene XML.
	 * @return Documento Xml
	 * @throws XMLDOMDocumentException
	 */
	public static Document parseXml(String pXML) throws XMLDOMDocumentException
	{
		javax.xml.parsers.DocumentBuilderFactory fact;
		javax.xml.parsers.DocumentBuilder db;
		Document doc=null;
		try
		{
			fact = javax.xml.parsers.DocumentBuilderFactory.newInstance();
			fact.setNamespaceAware(true);
			db= fact.newDocumentBuilder();
			org.xml.sax.InputSource inStr = new org.xml.sax.InputSource ();
			inStr.setCharacterStream(new java.io.StringReader(pXML));
			doc = db.parse(inStr);
			return doc;
		}
		catch (javax.xml.parsers.ParserConfigurationException ex)
		{
			throw new XMLDOMDocumentException(ERROR_AL_INTERPRETAR_EL_CODIGO_XML,ex);
		}
		catch (org.xml.sax.SAXException ex)
		{
			throw new XMLDOMDocumentException("Error al interpretar el código xml:" + ex.getMessage(),ex);
		}
		catch (java.io.IOException ex)
		{
			throw new XMLDOMDocumentException("Error al interpretar el código xml:" + ex.getMessage(),ex);
		}
	}
	/**
	 * Devuelve la representación XML de objeto doc.
	 * @param doc Documento que se devolverá  como texto
	 * @return Cadena que representa el texto de XML.
	 * @throws XMLDOMDocumentException
	 */
	public static String getXMLText(Document doc) throws XMLDOMDocumentException
	{
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no"); // No indentar salida, dejar tal como está.
	
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
	
			return result.getWriter().toString();
		}
		catch (TransformerConfigurationException ex)
		{
			throw new XMLDOMDocumentException (ERR_XML_TXT + ex.getMessage(),ex);
		}
		catch (TransformerException ex)
		{
			throw new XMLDOMDocumentException (ERR_XML_TXT + ex.getMessage(),ex);
		}
	}
	/**
	 * Devuelve la representación XML de objeto doc, formateada para legibilidad.
	 * @param doc Documento que se devolverá  como texto
	 * @return Cadena que representa el texto de XML.
	 * @throws XMLDOMDocumentException
	 */
	public static String getPrettyXMLText(Document doc) throws XMLDOMDocumentException
	{
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); 
	
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
	
			return result.getWriter().toString();
		}
		catch (TransformerConfigurationException ex)
		{
			throw new XMLDOMDocumentException (ERR_XML_TXT + ex.getMessage(),ex);
		}
		catch (TransformerException ex)
		{
			throw new XMLDOMDocumentException (ERR_XML_TXT + ex.getMessage(),ex);
		}
	}
	/**
	 * Devuelve la representación XML de un fragmento de XML comenzando en un nodo.
	 * @param nodo Nodo que se devolverá como texto.
	 * @return Cadena que representa el texto de XML.
	 * @throws XMLDOMDocumentException 
	 */
	public static String getXMLText(Node nodo) throws XMLDOMDocumentException
	{
		try
		{
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no"); // No indentar salida, dejar tal como está.
	
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(nodo);
			transformer.transform(source, result);
	
			return result.getWriter().toString();
		}
		catch (TransformerConfigurationException ex)
		{
			throw new XMLDOMDocumentException (ERR_XML_TXT + ex.getMessage(),ex);
		}
		catch (TransformerException ex)
		{
			throw new XMLDOMDocumentException (ERR_XML_TXT + ex.getMessage(),ex);
		}
	}
}
