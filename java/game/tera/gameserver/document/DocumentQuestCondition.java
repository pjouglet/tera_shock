package tera.gameserver.document;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import rlib.logging.Loggers;
import rlib.util.VarTable;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.base.Race;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.npc.interaction.conditions.ConditionLogicAnd;
import tera.gameserver.model.npc.interaction.conditions.ConditionLogicNot;
import tera.gameserver.model.npc.interaction.conditions.ConditionLogicOr;
import tera.gameserver.model.npc.interaction.conditions.ConditionNpcId;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayeClasses;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerRaces;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerHasItem;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerHeart;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerLearnedSkill;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerMaxLevel;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerMinLevel;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerMoreVar;
import tera.gameserver.model.npc.interaction.conditions.ConditionPlayerVar;
import tera.gameserver.model.npc.interaction.conditions.ConditionQuestComplete;
import tera.gameserver.model.npc.interaction.conditions.ConditionQuestState;
import tera.gameserver.model.quests.Quest;

/**
 * Класс для парса кондишенов с xml.
 *
 * @author Ronn
 */
public final class DocumentQuestCondition
{
	private static DocumentQuestCondition instance;

	public static final DocumentQuestCondition getInstance()
	{
		if(instance == null)
			instance = new DocumentQuestCondition();

		return instance;
	}

	/**
	 * Объеденяет 2 кондишена в 1.
	 *
	 * @param first первый кондишен.
	 * @param second второй кондишен.
	 * @return общий кондишен.
	 */
	public static Condition joinAnd(Condition first, Condition second)
	{
		// если первого нет
		if(first == null)
			// создаем его
			first = new ConditionLogicAnd();

		// добавляем к первому второй
		((ConditionLogicAnd) first).add(second);

		// возвращаем объедененный
		return first;
	}

	private DocumentQuestCondition()
	{
		super();
	}

	/**
	 * @return отпаршенное условие.
	 */
	private Condition parse(Node node, Quest quest)
	{
		switch(node.getNodeName())
		{
			case "npc": return parseNpcCondition(node, quest);
			case "quest": return parseQuestCondition(node, quest);
			case "player": return parsePlayerCondition(node, quest);
			case "and": return parseLogicAnd(node, quest);
			case "or": return parseLogicOr(node, quest);
			case "not": return parseLogicNot(node, quest);
		}

		return null;
	}

	/**
	 * Парсер условий в теге И
	 */
	public void parseCondition(ConditionLogicAnd container, Node node, Quest quest)
	{
		// если узла нет, выходим
		if(node == null)
			return;

		// перебираем кондишены в узле
		for(Node cond = node.getFirstChild(); cond != null; cond = cond.getNextSibling())
		{
			if(cond.getNodeType() != Node.ELEMENT_NODE)
				continue;

			Condition condition = parse(cond, quest);

			if(condition != null)
				container.add(condition);

			condition = null;
		}
	}

	/**
	 * Парсер условий в теге ИЛИ
	 */
	public void parseCondition(ConditionLogicOr container, Node node, Quest quest)
	{
		// если узла нет, выходим
		if(node == null)
			return;

		// перебираем кондишены в узле
		for(Node cond = node.getFirstChild(); cond != null; cond = cond.getNextSibling())
		{
			if(cond.getNodeType() != Node.ELEMENT_NODE)
				continue;

			Condition condition = parse(cond, quest);

			if(condition != null)
				container.add(condition);

			condition = null;
		}
	}

	/**
	 * Парс соответствующего кондишена с xml.
	 *
	 * @param node узел из хмл.
	 * @param quest квест.
	 * @return новый кондишен.
	 */
	public Condition parseCondition(Node node, Quest quest)
	{
		// если узла нет, выходим
		if(node == null)
			return null;

		// перебираем кондишены в узле
		for(Node cond = node.getFirstChild(); cond != null; cond = cond.getNextSibling())
		{
			if(cond.getNodeType() != Node.ELEMENT_NODE)
				continue;

			Condition condition = parse(cond, quest);

			if(condition != null)
				return condition;
		}

		return null;
	}

	private Condition parseLogicAnd(Node node, Quest quest)
	{
		// создаем объеденитель
		ConditionLogicAnd condition = new ConditionLogicAnd();

		// парсим внутренние условия
		parseCondition(condition, node, quest);

		// возвращаем итоговый
		return condition;
	}

	private Condition parseLogicNot(Node node, Quest quest)
	{
		// парсим рекурсивно кондишен
		Condition condition = parseCondition(node, quest);

		// если он есть
		if(condition != null)
			// оборачиваем в отрицание
			return new ConditionLogicNot(condition);

		return null;
	}

	/**
	 * Парсим набор условий для ИЛИ
	 */
	private Condition parseLogicOr(Node node, Quest quest)
	{
		// создаем объеденяющий кондишен
		ConditionLogicOr condition = new ConditionLogicOr();

		// парсим условия
		parseCondition(condition, node, quest);

		// возвращаем результат
		return condition;
	}

	private Condition parseNpcCondition(Node node, Quest quest)
	{
		Condition newCondition = null;

		//таблица атрибутов
		NamedNodeMap attrs = node.getAttributes();

		// парсим атрибуты
		VarTable vars = VarTable.newInstance(node);

		for(int i = 0, length = attrs.getLength(); i < length; i++)
		{
			//получаем текущий итем
			Node item = attrs.item(i);

			//Создаем соответсвующий кондишен
			switch(item.getNodeName())
			{
				// если первый атрибут ид
				case "id":
				{
					int id = vars.getInteger("id");
					int type = vars.getInteger("type");

					newCondition = new ConditionNpcId(quest, id, type);

					break;
				}
			}
		}

		if(newCondition == null)
			Loggers.warning(this, "unrecognized <npc> condition " + vars + " in quest " + quest.getName());

		return newCondition;
	}

	private Condition parsePlayerCondition(Node node, Quest quest)
	{
		Condition newCondition = null;

		// таблица атрибутов
		NamedNodeMap attrs = node.getAttributes();

		// парсим атрибуты
		VarTable vars = VarTable.newInstance(node);

		for(int i = 0, length = attrs.getLength(); i < length; i++)
		{
			// получаем первый итем
			Node item = attrs.item(i);

			// смотри какой первый атрибут
			switch(item.getNodeName())
			{
				case "classes":
				{
					// получаем массив строковых классов
					String[] classes = vars.getString("classes").split(";");

					// создаем массив классов игроков
					PlayerClass[] playerClasses = new PlayerClass[classes.length];

					// заполняем массив
					for(int g = 0, size = classes.length; g < size; g++)
						playerClasses[g] = PlayerClass.valueOf(classes[g]);

					// создаем кондишен
					newCondition = new ConditionPlayeClasses(quest, playerClasses);

					break;
				}
				case "races":
				{
					// получаем массив строковых рас
					String[] classes = vars.getString("races").split(";");

					// создаем массив рас игроков
					Race[] races = new Race[classes.length];

					// заполняем массив
					for(int g = 0, size = classes.length; g < size; g++)
						races[g] = Race.valueOf(classes[g]);

					// создаем кондишен
					newCondition = new ConditionPlayerRaces(quest, races);

					break;
				}
				case "var":
				{
					// создаем кондишен
					newCondition = new ConditionPlayerVar(quest, vars.getString("var"), vars.getInteger("val"));

					break;
				}
				case "moreVar":
				{
					// создаем кондишен
					newCondition = new ConditionPlayerMoreVar(quest, vars.getString("moreVar"), vars.getInteger("val"));

					break;
				}
				case "hasItem":
				{
					newCondition = new ConditionPlayerHasItem(quest, vars.getInteger("hasItem"), vars.getInteger("count"));

					break;
				}
				case "heart":
				{
					newCondition = new ConditionPlayerHeart(quest, vars.getInteger("heart"));

					break;
				}
				case "minLevel":
				{
					newCondition = new ConditionPlayerMinLevel(quest, vars.getInteger("minLevel"));

					break;
				}
				case "maxLevel":
				{
					newCondition = new ConditionPlayerMaxLevel(quest, vars.getInteger("maxLevel"));

					break;
				}
				case "learnedSkill":
				{
					newCondition = new ConditionPlayerLearnedSkill(quest, vars.getInteger("learnedSkill"));

					break;
				}
			}
		}

		if(newCondition == null)
			Loggers.warning(this, "unrecognized <player> condition " + vars + " in quest " + quest.getName());

		return newCondition;
	}

	private Condition parseQuestCondition(Node node, Quest quest)
	{
		Condition newCondition = null;

		// таблица атрибутов
		NamedNodeMap attrs = node.getAttributes();

		// парсим атрибуты
		VarTable vars = VarTable.newInstance(node);

		for(int i = 0, length = attrs.getLength(); i < length; i++)
		{
			// получаем первый итем
			Node item = attrs.item(i);

			// смотри какой первый атрибут
			switch(item.getNodeName())
			{
				case "state":
				{
					newCondition = new ConditionQuestState(quest, vars.getInteger("state"));

					break;
				}
				case "complete":
				{
					newCondition = new ConditionQuestComplete(quest, vars.getInteger("complete"));

					break;
				}
			}
		}

		if(newCondition == null)
			Loggers.warning(this, "unrecognized <quest> condition " + vars + " in quest " + quest.getName());

		return newCondition;
	}
}
