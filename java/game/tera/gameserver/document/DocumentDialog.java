package tera.gameserver.document;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.gameserver.model.npc.interaction.DialogData;
import tera.gameserver.model.npc.interaction.IconType;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.npc.interaction.LinkType;
import tera.gameserver.model.npc.interaction.links.NpcLink;
import tera.gameserver.model.npc.interaction.replyes.Reply;

/**
 * Парсер ссылок нпс диалогов.
 *
 * @author Ronn
 */
public final class DocumentDialog extends AbstractDocument<Table<IntKey, Table<IntKey, DialogData>>>
{
	/**
	 * Парс ссылки в диалоге.
	 *
	 * @param node данные с хмл.
	 * @return новая ссылка.
	 */
	public static Link parseLink(Node node)
	{
		// получаем аттрибуты ссылки
		VarTable vars = VarTable.newInstance(node);

		// получаем название ссылки
		String name = vars.getString("name");

		// получаем иконку
		IconType icon = vars.getEnum("icon", IconType.class, IconType.NONE);

		Reply reply = null;

		//парсим ответ на ссылку
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			//находим ответ
			if("reply".equals(child.getNodeName()))
			{
				//получаем название ответа
				String replyName = child.getAttributes().getNamedItem("name").getNodeValue();

				if(replyName == null)
					continue;

				//создаем экземпляр ответа
				try
				{
					reply = (Reply) Class.forName("tera.gameserver.model.npc.interaction.replyes." + replyName).getConstructor(Node.class).newInstance(child);
				}
				catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					log.warning(DocumentDialog.class, e);
				}
			}
		}

		//Возвращаем готовую ссылку с ответом
		return new NpcLink(name, LinkType.DIALOG, icon, reply);
	}

	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentDialog(File file)
	{
		super(file);
	}

	@Override
	protected Table<IntKey, Table<IntKey, DialogData>> create()
	{
		return Tables.newIntegerTable();
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node npc = lst.getFirstChild(); npc != null; npc = npc.getNextSibling())
					if("npc".equals(npc.getNodeName()))
					{
						DialogData dialog = parseNpc(npc);

						Table<IntKey, DialogData> table = result.get(dialog.getNpcId());

						if(table == null)
						{
							table = Tables.newIntegerTable();

							result.put(dialog.getNpcId(), table);
						}

						table.put(dialog.getType(), dialog);
					}
	}

	/**
	 * Парсим диалог нпс с хмл.
	 *
	 * @param node данные с схмл.
	 * @return новый диалог.
	 */
	private DialogData parseNpc(Node node)
	{
		// поучаем атрибуты диалога
		VarTable vars = VarTable.newInstance(node);

		// получаем ид нпс, к которому принадлежит диалог
		int id = vars.getInteger("id");
		// получаем ид нпс, к которому принадлежит диалог
		int type = vars.getInteger("type");

		// создаем массив ссылок
		Array<Link> links = Arrays.toArray(Link.class, 2);

		// Находим ссылки и парсим их
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			if("link".equals(child.getNodeName()))
				links.add(parseLink(child));

		// сужаем массив
		links.trimToSize();

		// создаем и возвращаем диалог с указанныым массивом ссылок
		return new DialogData(links.array(), id, type);
	}
}
