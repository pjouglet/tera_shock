package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.TownInfo;

/**
 * Парсер городов с xml.
 *
 * @author Ronn
 */
public final class DocumentTown extends AbstractDocument<Array<TownInfo>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentTown(File file)
	{
		super(file);
	}

	@Override
	protected Array<TownInfo> create()
	{
		return Arrays.toArray(TownInfo.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node town = list.getFirstChild(); town != null; town = town.getNextSibling())
					if("town".equals(town.getNodeName()))
						result.add(new TownInfo(town));
	}
}