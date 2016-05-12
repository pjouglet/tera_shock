package tera.gameserver.model.skillengine.funcs.stat;

import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.skillengine.lambdas.FloatAdd;
import tera.gameserver.model.skillengine.lambdas.FloatDiv;
import tera.gameserver.model.skillengine.lambdas.FloatMul;
import tera.gameserver.model.skillengine.lambdas.FloatSet;
import tera.gameserver.model.skillengine.lambdas.FloatSub;

/**
 * Фабрика функций статов.
 *
 * @author Ronn
 */
public class FuncFactory
{
	private final static FuncFactory instance = new FuncFactory();

	public static FuncFactory getInstance()
	{
		return instance;
	}

	/**
	 * @param type тип функции.
	 * @param stat тип парамерта.
	 * @param order порядок в списке функций.
	 * @param cond условие на приминение.
	 * @param value значение модификатора.
	 * @return новая функция.
	 */
	public Func createFunc(String type, StatType stat, int order, Condition cond, String value)
	{
		Func func = null;

		switch(type)
		{
			case "add": func = new MathFunc(stat, order, cond, new FloatAdd(Float.parseFloat(value))); break;
			case "sub": func = new MathFunc(stat, order, cond, new FloatSub(Float.parseFloat(value))); break;
			case "mul": func = new MathFunc(stat, order, cond, new FloatMul(Float.parseFloat(value))); break;
			case "div": func = new MathFunc(stat, order, cond, new FloatDiv(Float.parseFloat(value))); break;
			case "set": func = new MathFunc(stat, order, cond, new FloatSet(Float.parseFloat(value))); break;
		}

		return func;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}
