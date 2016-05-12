package tera.gameserver.document;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import rlib.logging.Loggers;
import rlib.util.array.Array;
import tera.gameserver.tasks.AnnounceTask;

/**
 * Модель работы с хмл, в котором описаны аннонсы сервера.
 *
 * @author Ronn
 * @created 29.03.2012
 */
public class DocumentAnnounce
{
	/** фаил с которого парсим */
	private File file;

	/** отпарсенный документ */
	private Document doc;

	/** интервальные анонсы */
	private Array<AnnounceTask> runningAnnouncs;
	/** стартовые анонсы */
	private Array<String> startAnnouncs;

	/**
	 * @param file фаил, в котором описаны аннонсы.
	 */
	public DocumentAnnounce(File file)
	{
		this.file = file;
	}

	/**
	 * @return периодические аннонсы.
	 */
	public final Array<AnnounceTask> getRuningAnnouncs()
	{
		return runningAnnouncs;
	}

	/**
	 * @return стартовые аннонсы.
	 */
	public final Array<String> getStartAnnouncs()
	{
		return startAnnouncs;
	}

	/**
	 * Парс аннонсов из файла.
	 */
	public final void parse()
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch(SAXException | IOException | ParserConfigurationException e)
		{
			Loggers.warning(this, e);
			return;
		}

		parseDocument(doc);

		runningAnnouncs.trimToSize();
		startAnnouncs.trimToSize();
	}

	private final void parseAnnounce(Node attrs)
	{
		// получаем атрибуты анонса
		NamedNodeMap vals = attrs.getAttributes();

		Node node = vals.getNamedItem("interval");

		//если интервал не указан, значит это стартовый анонс
		if(node == null)
			startAnnouncs.add(attrs.getFirstChild().getNodeValue());
		else
			//иначе это интервальный анонс
			runningAnnouncs.add(new AnnounceTask(attrs.getFirstChild().getNodeValue(), Integer.parseInt(node.getNodeValue())));
	}

	/**
	 * @param doc отпарсенный докусемн с аннонсами.
	 */
	private final void parseDocument(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node ann = lst.getFirstChild(); ann != null; ann = ann.getNextSibling())
					if("announce".equals(ann.getNodeName()))
						parseAnnounce(ann);
	}

	/**
	 * Сохранение аннонсов в фаил.
	 */
	public void save()
	{
		try(PrintWriter out = new PrintWriter(file))
		{
			out.println("<?xml version='1.0' encoding='utf-8'?>");
			out.println("<list>");

			for(String announce : startAnnouncs)
				out.println("	<announce>" + announce + "</announce>");

			for(AnnounceTask announce : runningAnnouncs)
				out.println("	<announce interval=\"" + announce.getInterval() + "\" >" + announce.getText() + "</announce>");

			out.println("</list>");
		}
		catch(IOException e)
		{
			Loggers.warning(this, e);
		}
	}

	/**
	 * @param runingAnnouncs список периодических аннонсов.
	 */
	public final void setRuningAnnouncs(Array<AnnounceTask> runingAnnouncs)
	{
		this.runningAnnouncs = runingAnnouncs;
	}

	/**
	 * @param startAnnouncs список стартовых аннонсов.
	 */
	public final void setStartAnnouncs(Array<String> startAnnouncs)
	{
		this.startAnnouncs = startAnnouncs;
	}
}
