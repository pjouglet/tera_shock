package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.table.Table;

import tera.gameserver.model.npc.playable.NpcAppearance;
import tera.gameserver.model.playable.PlayerAppearance;

/**
 * Парсер готовых внешностей игроков с xml.
 *
 * @author Ronn
 */
public final class DocumentNpcAppearance extends AbstractDocument<Void>
{
	private Table<String, NpcAppearance> table;

	public DocumentNpcAppearance(File file, Table<String, NpcAppearance> table)
	{
		super(file);

		this.table = table;
	}

	@Override
	protected Void create()
	{
		return null;
	}

	@Override
	protected void parse(Document doc)
	{
		VarTable vars = VarTable.newInstance();

		Table<String, NpcAppearance> table = getTable();

		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node child = list.getFirstChild(); child != null; child = child.getNextSibling())
					if(child.getNodeType() == Node.ELEMENT_NODE && "appearance".equals(child.getNodeName()))
					{
						// парсим атрибуты
						vars.parse(child);

						String id = vars.getString("id");

						// парсим параметры внешности
						vars.parse(child, "set", "name", "value");

						// парсим внешность
						NpcAppearance appearance = PlayerAppearance.fromXML(new NpcAppearance(), vars);

						table.put(id, appearance);
					}
	}

	public Table<String, NpcAppearance> getTable()
	{
		return table;
	}
}