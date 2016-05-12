package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.territory.Territory;
import tera.gameserver.model.territory.TerritoryType;

/**
 * Парсер территорий с xml.
 *
 * @author Ronn
 * @created 09.03.2012
 */
public final class DocumentTerritory extends AbstractDocument<Array<Territory>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentTerritory(File file)
	{
		super(file);
	}

	@Override
	protected Array<Territory> create()
	{
		return Arrays.toArray(Territory.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node node = list.getFirstChild(); node != null; node = node.getNextSibling())
					if("territory".equals(node.getNodeName()))
					{
						// получаем атрибуты территории
						VarTable vars = VarTable.newInstance(node);

						// создаем и добавляем территорию
						result.add(vars.getEnum("type", TerritoryType.class).newInstance(node));
					}
	}
}