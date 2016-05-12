package tera.gameserver.document;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.drop.Drop;
import tera.gameserver.model.drop.DropGroup;
import tera.gameserver.model.drop.DropInfo;
import tera.gameserver.model.drop.NpcDrop;
import tera.gameserver.model.drop.ResourseDrop;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;

/**
 * Парсер дропа с xml.
 *
 * @author Ronn
 * @created 17.03.2012
 */
public final class DocumentDrop extends AbstractDocument<Array<Drop>>
{
	public static final Set<Integer> filter = new HashSet<Integer>();

	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentDrop(File file)
	{
		super(file);
	}

	@Override
	protected Array<Drop> create()
	{
		return Arrays.toArray(Drop.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node dp = lst.getFirstChild(); dp != null; dp = dp.getNextSibling())
					if("drop".equals(dp.getNodeName()))
					{
						Drop drop = parseDrop(dp);

						if(drop == null)
							continue;

						result.add(drop);
					}
	}

	/**
	 * Парс дропа с хмл.
	 *
	 * @param nodes данные с хмл.
	 * @return новые данные с дропом.
	 */
	private final Drop parseDrop(Node nodes)
	{
		// получаем атрибуты дропа
		VarTable vars = VarTable.newInstance(nodes);

		// узнаем ид нпс, к котрому он принадлежит
		int templateId = vars.getInteger("templateId");
		// узнаем тип нпс, к котрому он принадлежит
		int templateType = vars.getInteger("templateType", -1);

		// создаем массив групп
		Array<DropGroup> groups = Arrays.toArray(DropGroup.class);

		// перебираем группы
		for(Node child = nodes.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// найденную группу парсим и добавляем в массив
			if("group".equals(child.getNodeName()))
			{
				DropGroup group = parseGroup(child);

				if(group != null)
					groups.add(group);
			}
		}

		// если групп не нашли, выходим
		if(groups.isEmpty())
			return null;

		// отрезаем пустые ячейки в массиве
		groups.trimToSize();

		// создаем дроп по массиву групп
		return templateType == -1? new ResourseDrop(templateId, groups.array()) : new NpcDrop(templateId, templateType, groups.array());
	}

	/**
	 * Парс группы дропа.
	 *
	 * @param node данные с хмл.
	 * @return новая группа дропа.
	 */
	private final DropGroup parseGroup(Node node)
	{
		// получаем атрибуты дропа
		VarTable vars = VarTable.newInstance(node);

		// получаем ид
		int id = vars.getInteger("id");
		// получаем шанс
		int chance = vars.getInteger("chance");
		// получаем кол-во
		int count = vars.getInteger("count", 1);

		// создаем массив итемов
		Array<DropInfo> items = Arrays.toArray(DropInfo.class);

		// ищем итеемы в группе
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			//найденные итемы парсим и лобавляем в массив итемов
			if("item".equals(child.getNodeName()))
			{
				DropInfo item = parseItem(child);

				if(item != null)
					items.add(item);
			}
		}

		// если итемы не найдены, возвращаем нулл
		if(items.isEmpty())
			return null;

		items.trimToSize();

		// создаем группу по массиву итемов
		return new DropGroup(id, chance, count, items.array());
	}

	/**
	 * Парс итема из дропа.
	 *
	 * @param node данные с хмл.
	 * @return информация о дроп итеме.
	 */
	private final DropInfo parseItem(Node node)
	{
		// получаем атрибуты итема
		VarTable vars = VarTable.newInstance(node);

		// получаем ид итема
		int id = vars.getInteger("templateId");
		// получаем мин кол-во
		int min = vars.getInteger("min");
		// получаем макс кол-вл
		int max = vars.getInteger("max");
		// получаем шанс
		int chance = vars.getInteger("chance");

		// получаем таблицу шаблонов итемов
		ItemTable itemTable = ItemTable.getInstance();

		// получаем шаблон итема
		ItemTemplate template = itemTable.getItem(id);

		// если такого нет, выходим
		if(template == null)
		{
			filter.add(id);
			return null;
		}

		// создаем инфу о итеме
		return new DropInfo(template, min, max, chance);
	}
}
