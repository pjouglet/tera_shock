package tera.gameserver.parser;

import java.io.File;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.array.Arrays;
import rlib.util.table.Table;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.skillengine.funcs.stat.FuncFactory;

/**
 * Парсер функций статов.
 *
 * @author Ronn
 */
public final class StatFuncParser
{
	private static final String[] STAT_FUNC_NAMES =
	{
		"add",
		"sub",
		"mul",
		"set",
		"div",
	};

	private static StatFuncParser instance;

	public static StatFuncParser getInstance()
	{
		if(instance == null)
			instance = new StatFuncParser();

		return instance;
	}

	/**
	 * Определяет, является ли данная функция, функцией статов.
	 *
	 * @param name название функции.
	 * @return является ли финкцией статов.
	 */
	public static boolean isStatFunc(String name)
	{
		return Arrays.contains(STAT_FUNC_NAMES, name);
	}

	/**
	 * Получаем значние параметра.
	 *
	 * @param order номер в таблице.
	 * @param table таблица значений.
	 * @param value значение параметра.
	 * @param skill скил, для которого получаем значение.
	 * @param file фаил, в котором находится скил.
	 * @return значение параметра.
	 */
	private String getValue(int order, Table<String, String[]> table, String value, int skill, File file)
	{
		// итоговое значение
		String val = null;

		// если это не таблица, его же и принимаем
		if(!value.startsWith("#"))
			val = value;
		else
		{
			// иначе извлекаем массив из таблицы
			String[] array = table.get(value);

			// берем нужное значение
			value = array[Math.min(array.length -1, order)];
		}

		// возвращаем результат
		return val;
	}

	/**
	 * Парс функции стата с хмл.
	 *
	 * @param order номер в таблице.
	 * @param table таблица значений.
	 * @param node данные с хмл.
	 * @param skill скилл, для которого парсится функция.
	 * @param file фаил, в котором находится скилл.
	 * @return новая функция.
	 */
	public Func parseFunc(int order, Table<String, String[]> table, Node node, int skill, File file)
	{
		// получаем атрибуты функции
		VarTable vars = VarTable.newInstance(node);

		// определяем стат
		StatType stat = StatType.valueOfXml(vars.getString("stat"));

		// определяем порядок
		int ordinal = Integer.decode(vars.getString("order"));

		// получаем значение функции
		String value = getValue(order, table, vars.getString("val"), skill, file);

		// подготавливаем условие
		Condition cond = null;

		// получаем парсер условий
		ConditionParser parser = ConditionParser.getInstance();

		// перебираем возможные условия
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			// проопускаем ненужные элементы
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// парсим условие
			cond = parser.parseCondition(child, skill, file);

			// если кондишен был отпарсен, выходим из цикла
			if(cond != null)
				break;
		}

		// получаем фабрику функций статов
		FuncFactory funcFactory = FuncFactory.getInstance();

		// создаем новую функцию
		return funcFactory.createFunc(node.getNodeName(), stat, ordinal, cond, value);
	}

	/**
	 * Парс функции стата с хмл.
	 *
	 * @param node данные с хмл.
	 * @return новая функция.
	 */
	public Func parseFunc(Node node, File file)
	{
		// парсим атрибуты
		VarTable vars = VarTable.newInstance(node);

		// определяем стат
		StatType stat = StatType.valueOfXml(vars.getString("stat"));

		// определяем ордер
		int ordinal = Integer.decode(vars.getString("order"));

		// подготавливаем условие
		Condition cond = null;

		// получаем парсер условий
		ConditionParser parser = ConditionParser.getInstance();

		// перебираем возможные условия
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			// проопускаем ненужные элементы
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// парсим условие
			cond = parser.parseCondition(child, 0, file);

			// если кондишен был отпарсен, выходим из цикла
			if(cond != null)
				break;
		}

		// получаем фабрику функций статов
		FuncFactory funcFactory = FuncFactory.getInstance();

		// создаем новую функцию
		return funcFactory.createFunc(node.getNodeName(), stat, ordinal, cond, vars.getString("val"));
	}
}
