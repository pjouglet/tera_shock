package tera.gameserver.parser;

import java.io.File;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.Table;

import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.templates.EffectTemplate;

/**
 * Парсер эффектов.
 *
 * @author Ronn
 */
public final class EffectParser
{
	private static final Logger log = Loggers.getLogger(EffectParser.class);

	private static EffectParser instance;

	public static EffectParser getInstance()
	{
		if(instance == null)
			instance = new EffectParser();

		return instance;
	}

	private EffectParser()
	{
		log.info("initialized.");
	}

	/**
	 * Парс эффекта с хмл.
	 *
	 * @param order номер в таблице.
	 * @param node данные с хмл.
	 * @param tables таблица значений.
	 * @param skillId скилл.
	 * @param file файл, в котором описан эффект.
	 * @return новый темплейт эффекта.
	 */
	public EffectTemplate paraseEffects(int order, Node node, Table<String, String[]> table, int skillId, File file)
	{
		// создаем таблицу параметов эффектов
		VarTable vars = VarTable.newInstance();

		// создаем массив функций эффектов
		Array<Func> funcs = Arrays.toArray(Func.class);

		// получаем все атрибуты эффекта
		NamedNodeMap vals = node.getAttributes();

		// обновляем атрибуты шаблона
		for(int i = 0; i < vals.getLength(); i++)
		{
			Node item = vals.item(i);

			String name = item.getNodeName();
			String value = item.getNodeValue();

			if(value.startsWith("#"))
			{
				String[] array = table.get(value);
				value = array[Math.min(array.length -1, order)];
			}

			vars.set(name, value);
		}

		// получаем парсер функципй
		FuncParser funcManager = FuncParser.getInstance();

		// парсим функции
		funcManager.parse(node, funcs, table, order, skillId, file);

		// сжимаем список
		funcs.trimToSize();

		// создаем шаблон эффекта
		return new EffectTemplate(vars, funcs.array());
	}
}
