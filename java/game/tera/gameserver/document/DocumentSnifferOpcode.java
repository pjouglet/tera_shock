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
public final class DocumentSnifferOpcode extends AbstractDocument<VarTable>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentSnifferOpcode(File file)
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
				for(Node protocol = list.getFirstChild(); protocol != null; protocol = protocol.getNextSibling())
					if("protocol".equals(protocol.getNodeName()))
						for(Node packet = protocol.getFirstChild(); packet != null; packet = packet.getNextSibling())
						{
							if("packet".equals(packet.getNodeName()))
							{
								NamedNodeMap attrs = packet.getAttributes();

								Integer id = Integer.decode(attrs.getNamedItem("id").getNodeValue());

								String type = attrs.getNamedItem("class").getNodeValue();
								String name = attrs.getNamedItem("name").getNodeValue();

								result.set(type + "_" + name, id);
							}
						}
	}
}
