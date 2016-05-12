package tera.gameserver.parser;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.Table;

import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.skillengine.funcs.chance.ChanceFunc;
import tera.gameserver.model.skillengine.funcs.task.TaskFunc;

/**
 * Парсер функций.
 *
 * @author Ronn
 */
public class FuncParser
{
	private static final Logger log = Loggers.getLogger(FuncParser.class);

	private static String[] FUNC_NAMES =
	{
		"task",
		"chance",
	};

	private static FuncParser instance;

	public static FuncParser getInstance()
	{
		if(instance == null)
			instance = new FuncParser();

		return instance;
	}

	/**
	 * Определние, является ли элемент с указанным имененм функцией.
	 *
	 * @param name имя элемента.
	 * @return является ли он функцией.
	 */
	public static boolean isFunc(String name)
	{
		return Arrays.contains(FUNC_NAMES, name) || StatFuncParser.isStatFunc(name);
	}

	/** список шансовых функций */
	private final Array<ChanceFunc> funcs;

	private FuncParser()
	{
		this.funcs = Arrays.toArray(ChanceFunc.class);
	}

	/**
	 * Добавление шансовой функции.
	 *
	 * @param func шансовая функция.
	 */
	public void addChanceFunc(ChanceFunc func)
	{
		funcs.add(func);
	}

	/**
	 * Парс функций из xml узла.
	 *
	 * @param node узел с функциями.
	 * @param container контейнер функций.
	 */
	public void parse(Node node, Array<Func> container, File file)
	{
		VarTable vars = VarTable.newInstance();

		// получаем парсер условий
		ConditionParser condParser = ConditionParser.getInstance();

		// получаем парсер функций статов
		StatFuncParser statFuncParser = StatFuncParser.getInstance();

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// получаем название функции
			String name = child.getNodeName();

			// если эта функция статов
			if(StatFuncParser.isStatFunc(name))
			{
				// парсим функцию
				Func func = statFuncParser.parseFunc(child, file);

				// если ее нет, пропускаем
				if(func == null)
				{
					log.warning("not found func stat to name " + name + " on file" + file + ".");
					continue;
				}

				// добавляем в активирующиеся
				container.add(func);
			}
			else if("task".equals(name))
			{
				// парсим атрибуты
				vars.parse(child);

				try
				{
					// создаем экземпляр функции
					Func func = (Func) Class.forName(TaskFunc.class.getPackage().getName() + "." + vars.getString("name")).getConstructor(VarTable.class).newInstance(vars);

					// добавляем в контейнер
					container.add(func);
				}
				catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | DOMException e)
				{
					log.warning(e);
				}
			}
			else if("chance".equals(name))
			{
				// парсим атрибуты
				vars.parse(child);

				// ссылка на условие
				Condition cond = null;

				// парсим условия
				for(Node condNode = child.getFirstChild(); condNode != null; condNode = condNode.getNextSibling())
				{
					if(condNode.getNodeType() != Node.ELEMENT_NODE)
						continue;

					// парсим условие
					Condition cnd = condParser.parseCondition(condNode, 0, file);

					if(cnd == null)
					{
						log.warning(new Exception("not found condition for " + condNode));
						continue;
					}

					// объеденяем
					cond = condParser.joinAnd(cond, cnd);
				}

				try
				{
					// создаем экземпляр функции
					Func func = (Func) Class.forName(ChanceFunc.class.getPackage().getName() + "." + vars.getString("name")).getConstructor(VarTable.class, Condition.class).newInstance(vars, cond);

					// добалвяем в контейнер
					container.add(func);
				}
				catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | DOMException e)
				{
					log.warning(e);
				}
			}
		}
	}

	/**
	 * Парс функций из xml узла.
	 *
	 * @param node узел с функциями.
	 * @param container контейнер функций.
	 */
	public void parse(Node node, Array<Func> container, Table<String, String[]> table, int order, int skillId, File file)
	{
		VarTable vars = VarTable.newInstance();

		// получаем парсер условий
		ConditionParser condParser = ConditionParser.getInstance();

		// получаем парсер функций статов
		StatFuncParser statFuncParser = StatFuncParser.getInstance();

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// получаем название функции
			String name = child.getNodeName();

			// если эта функция статов
			if(StatFuncParser.isStatFunc(name))
			{
				// парсим функцию
				Func func = statFuncParser.parseFunc(order, table, child, skillId, file);

				// если ее нет, пропускаем
				if(func == null)
				{
					log.warning("not found func stat to name " + name + " on file" + file + ".");
					continue;
				}

				// добавляем в активирующиеся
				container.add(func);
			}
			else if("task".equals(name))
			{
				// парсим атрибуты
				vars.parse(child);

				try
				{
					// создаем экземпляр функции
					Func func = (Func) Class.forName(TaskFunc.class.getPackage().getName() + "." + vars.getString("name")).getConstructor(VarTable.class).newInstance(vars);

					// добавляем в контейнер
					container.add(func);
				}
				catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | DOMException e)
				{
					log.warning(e);
				}
			}
			else if("chance".equals(name))
			{
				// парсим атрибуты
				vars.parse(child);

				// ссылка на условие
				Condition cond = null;

				// парсим условия
				for(Node condNode = child.getFirstChild(); condNode != null; condNode = condNode.getNextSibling())
				{
					if(condNode.getNodeType() != Node.ELEMENT_NODE)
						continue;

					// парсим условие
					Condition cnd = condParser.parseCondition(condNode, 0, file);

					if(cnd == null)
					{
						log.warning(new Exception("not found condition for " + condNode));
						continue;
					}

					// объеденяем
					cond = condParser.joinAnd(cond, cnd);
				}

				try
				{
					// создаем экземпляр функции
					Func func = (Func) Class.forName(ChanceFunc.class.getPackage().getName() + "." + vars.getString("name")).getConstructor(VarTable.class, Condition.class).newInstance(vars, cond);

					// добалвяем в контейнер
					container.add(func);
				}
				catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException | DOMException e)
				{
					log.warning(e);
				}
			}
		}
	}

	/**
	 * Подготовка к работе шансовых функций.
	 */
	public void prepareChanceFunc()
	{
		for(ChanceFunc func : funcs)
			func.prepare();
	}
}
