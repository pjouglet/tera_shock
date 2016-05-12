package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.ai.npc.ConfigAI;

/**
 * Парсер конфига АИ нпс с хмл.
 *
 * @author Ronn
 */
public class DocumentNpcConfigAI extends AbstractDocument<Array<ConfigAI>>
{
	public DocumentNpcConfigAI(File file)
	{
		super(file);
	}

	@Override
	protected Array<ConfigAI> create()
	{
		return Arrays.toArray(ConfigAI.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node config = lst.getFirstChild(); config != null; config = config.getNextSibling())
					if("config".equals(config.getNodeName()))
						result.add(new ConfigAI(config));
	}
}
