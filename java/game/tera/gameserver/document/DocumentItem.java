package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.Strings;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.items.ItemClass;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.parser.FuncParser;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Парсер итемов с хмл.
 *
 * @author Ronn
 * @created 17.03.2012
 */
public final class DocumentItem extends AbstractDocument<Array<ItemTemplate>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentItem(File file)
	{
		super(file);
	}

	@Override
	protected Array<ItemTemplate> create()
	{
		return Arrays.toArray(ItemTemplate.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node temp = lst.getFirstChild(); temp != null; temp = temp.getNextSibling())
				{
					ItemClass itemClass = ItemClass.valueOfXml(temp.getNodeName());

					if(itemClass == null)
						continue;

					Array<SkillTemplate> skills = Arrays.toArray(SkillTemplate.class);
					Array<Func> funcs = Arrays.toArray(Func.class);

					VarTable vars = VarTable.newInstance(temp);

					vars.set("class", itemClass);

					ItemTemplate template = itemClass.newTemplate(vars);

					if(template == null)
						continue;

					skills.addAll(SkillTable.parseSkills(vars.getString("skills", Strings.EMPTY), template.getClassIdItemSkill()));

					for(Node child = temp.getFirstChild(); child != null; child = child.getNextSibling())
						switch(child.getNodeName())
						{
							case "skills": parseSkills(child, skills); break;
							case "funcs": parseFuncs(child, funcs); break;
						}

					skills.trimToSize();
					funcs.trimToSize();

					template.setSkills(skills.array());
					template.setFuncs(funcs.array());

					result.add(template);
				}
	}

	/**
	 * Парс набора функций итема.
	 *
	 * @param node хмл элемент.
	 */
	private void parseFuncs(Node node, Array<Func> funcs)
	{
		// получаем парсер функций
		FuncParser parser = FuncParser.getInstance();

		// парсим функции
		parser.parse(node, funcs, file);
	}

	/**
	 * Получение набора скилов для итема.
	 *
	 * @param node хмл объект.
	 * @param skills набор скилов.
	 */
	private void parseSkills(Node node, Array<SkillTemplate> skills)
	{
		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		for(Node temp = node.getFirstChild(); temp != null; temp = temp.getNextSibling())
			if("skill".equals(temp.getNodeName()))
			{
				NamedNodeMap attrs = temp.getAttributes();

				int templateId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
				int classId = Integer.parseInt(attrs.getNamedItem("class").getNodeValue());

				SkillTemplate skill = skillTable.getSkill(templateId, classId);

				if(skill != null)
					skills.add(skill);
			}
	}
}
