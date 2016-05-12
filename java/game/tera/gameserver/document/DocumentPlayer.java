package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.base.Race;
import tera.gameserver.model.base.Sex;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.parser.FuncParser;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.ItemTemplate;
import tera.gameserver.templates.PlayerTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Парсер темплейтов игроков с xml.
 *
 * @author Ronn
 * @created 16.03.2012
 */
public final class DocumentPlayer extends AbstractDocument<Array<PlayerTemplate>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentPlayer(File file)
	{
		super(file);
	}

	@Override
	protected Array<PlayerTemplate> create()
	{
		return Arrays.toArray(PlayerTemplate.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node template = list.getFirstChild(); template != null; template = template.getNextSibling())
					if("template".equals(template.getNodeName()))
						parseTemplate(template);
	}

	/**
	 * @param node данные с хмл.
	 * @return набор функций.
	 */
	private final Func[] parseFuncs(Node node)
	{
		Array<Func> array = Arrays.toArray(Func.class);

		// получаем парсер функций
		FuncParser parser = FuncParser.getInstance();

		// парсим функции
		parser.parse(node, array, file);

		// сжимаем список
		array.trimToSize();

		return array.array();
	}

	/**
	 * @param node данные с хмл.
	 * @return массив выдаваемых итемов.
	 */
	private final int[][] parseItems(Node node)
	{
		Array<int[]> items = Arrays.toArray(int[].class);

		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			if("item".equals(child.getNodeName()))
			{
				// получаем значения аргументов
				VarTable vars = VarTable.newInstance(child);

				int id = vars.getInteger("id");
				int count = vars.getInteger("count");

				// получаем if,kjy итема
				ItemTemplate template = itemTable.getItem(id);

				// если такого итема на сервере нет, пропускаем
				if(template == null)
					continue;

				// вносим итем
				items.add(Arrays.toIntegerArray(id, count));
			}

		// сжимаем массив
		items.trimToSize();

		// возвращаем
		return items.array();
	}

	/**
	 * @param node данные с хмл.
	 * @return массив скилов.
	 */
	private final Array<SkillTemplate[]> parseSkills(Node node)
	{
		Array<SkillTemplate[]> skills = Arrays.toArray(SkillTemplate[].class, 2);

		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			if("skill".equals(child.getNodeName()))
			{
				// получаем значения аргументов
				VarTable vars = VarTable.newInstance(child);

				int id = vars.getInteger("id");
				int classId = vars.getInteger("class");

				// получаем темплейт скила
				SkillTemplate[] template = skillTable.getSkills(classId, id);

				// если такой есть
				if(template != null)
					// вносим
					skills.add(template);
			}

		return skills;
	}

	/**
	 * @param node данные с хмл.
	 * @param playerClass класс игрока.
	 * @return массив скиллов.
	 */
	private final SkillTemplate[][] parseSkills(Node node, PlayerClass playerClass)
	{
		Array<SkillTemplate[]> skills = Arrays.toArray(SkillTemplate[].class, 2);

		// получаем таблицу скилов
		SkillTable skillTable = SkillTable.getInstance();

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			if("skill".equals(child.getNodeName()))
			{
				// получаем значения аргументов
				VarTable vars = VarTable.newInstance(child);

				// получаем ид скила
				int id = vars.getInteger("id");

				// получаем темплейт скила
				SkillTemplate[] template = skillTable.getSkills(playerClass.getId(), id);

				// если такой есть
				if(template != null)
					// вносим
					skills.add(template);
			}

		// сжимаем список
		skills.trimToSize();

		// возвращаем массив
		return skills.array();
	}

	/**
	 * @param node данные с хмл.
	 */
	private final void parseTemplate(Node node)
	{
		VarTable vars = VarTable.newInstance(node);

		// получаем класс игрока
		PlayerClass pclass = vars.getEnum("class", PlayerClass.class);

		VarTable set = null;
		int[][] items = null;
		SkillTemplate[][] skills = null;
		Func[] funcs = new Func[0];

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			else if("stats".equals(child.getNodeName()))
				set = VarTable.newInstance(child, "stat", "name", "val");
			else if("funcs".equals(child.getNodeName()))
				funcs = parseFuncs(child);
			else if("items".equals(child.getNodeName()))
				items = parseItems(child);
			else if("skills".equals(child.getNodeName()))
				skills = parseSkills(child, pclass);
			else if("races".equals(child.getNodeName()))
				parseTemplate(child, set, funcs, pclass, items, skills);
		}
	}

	/**
	 * @param node данные с хмл.
	 * @param stats таблица параметров.
	 * @param funcs набор функций.
	 * @param playerClass класс игрока.
	 * @param items массив итемов.
	 * @param skills массив скилов.
	 */
	private final void parseTemplate(Node node, VarTable stats, Func[] funcs, PlayerClass playerClass, int[][] items, SkillTemplate[][] skills)
	{
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			if("race".equals(child.getNodeName()))
			{
				// парсим атрибуты
				VarTable vars = VarTable.newInstance(child);

				// получаем расу
				Race race = vars.getEnum("type", Race.class);

				// контейнер скилов
				Array<SkillTemplate[]> skillList = null;

				// парсим рассовые скилы
				for(Node temp = child.getFirstChild(); temp != null; temp = temp.getNextSibling())
					if("skills".equals(temp.getNodeName()))
						skillList = parseSkills(temp);

				// вносим классовые скилы
				for(SkillTemplate[] skill : skills)
					skillList.add(skill);

				// сжимаем массив
				skillList.trimToSize();

				int modelId = vars.getInteger("male", -1);

				if(modelId != -1)
					result.add(new PlayerTemplate(stats, funcs, playerClass, race, Sex.MALE, modelId, items, skillList.array()));

				modelId = vars.getInteger("female", -1);

				if(modelId != -1)
					result.add(new PlayerTemplate(stats, funcs, playerClass, race, Sex.FEMALE, modelId, items, skillList.array()));
			}
	}
}
