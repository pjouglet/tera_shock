package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;

/**
 * Парсер конфига с xml.
 *
 * @author Ronn
 * @created 12.03.2012
 */
public final class DocumentConfig extends AbstractDocument<VarTable>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentConfig(File file)
	{
		super(file);
	}

	@Override
	protected VarTable create()
	{
		return VarTable.newInstance();
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node set = list.getFirstChild(); set != null; set = set.getNextSibling())
					if("set".equals(set.getNodeName()))
					{
						NamedNodeMap attrs = set.getAttributes();

						String name = attrs.getNamedItem("name").getNodeValue();
						String value = attrs.getNamedItem("value").getNodeValue();

						if(name == null || value == null)
						{
							log.warning(this, "error loading file " + file + ", set name " + name + " value " + value + ".");
							System.exit(0);
						}

						result.set(name, value);
					}
	}
}
