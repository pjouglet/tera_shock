package tera.gameserver.document;

import java.io.File;
import java.lang.reflect.Field;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.base.Race;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.parser.FuncParser;

/**
 * Парсер статов рас игроков с xml.
 *
 * @author Ronn
 * @created 16.03.2012
 */
public final class DocumentRaceStats extends AbstractDocument<Void>
{
	public DocumentRaceStats(File file)
	{
		super(file);
	}

	@Override
	protected Void create()
	{
		return null;
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node child = list.getFirstChild(); child != null; child = child.getNextSibling())
					if(child.getNodeType() == Node.ELEMENT_NODE && "race".equals(child.getNodeName()))
						parseRace(child);
	}

	private final void parseRace(Node node)
	{
		VarTable vars = VarTable.newInstance(node);

		// получаем расу
		Race race = vars.getEnum("type", Race.class);

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if("stats".equals(child.getNodeName()))
				parseStats(child, race);
			else if("funcs".equals(child.getNodeName()))
				parseFuncs(child, race);
		}
	}

	private void parseStats(Node node, Race race)
	{
		// парсим параметры
		VarTable vars = VarTable.newInstance(node, "stat", "name", "val");

		// получаем поля расы
		Field[] fields = race.getClass().getDeclaredFields();

		try
		{
			// перебираем поля
			for(Field field : fields)
			{
				// пропускаем ненужные
				if(field.getType() != float.class)
					continue;

				// запоминаем прошлый флаг
				boolean old = field.isAccessible();

				// разрешаем изменять
				field.setAccessible(true);

				// вносим новое значение стата
				field.setFloat(race, vars.getFloat(field.getName(), 1F));

				// возвращаем старый флаг
				field.setAccessible(old);
			}
		}
		catch(IllegalArgumentException | IllegalAccessException e)
		{
			log.warning(this, e);
		}
	}

	private void parseFuncs(Node node, Race race)
	{
		// создаем контейнер функций
		Array<Func> array = Arrays.toArray(Func.class);

		// получаем парсер функций
		FuncParser parser = FuncParser.getInstance();

		// парсим функции
		parser.parse(node, array, file);

		// вносим в расу
		race.setFuncs(array);
	}
}
