package tera.gameserver.model.skillengine;

import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.funcs.StatFunc;

/**
 * Модель рассчет статовперсонажей.
 *
 * @author Ronn
 */
public final class Calculator
{
	/** список функций */
	private Array<StatFunc> funcs;

	public Calculator()
	{
		this.funcs = Arrays.toSortedArray(StatFunc.class, 1);
	}

	/**
	 * Добавить функцию в калькулятор.
	 *
	 * @param func добавляемая функция.
	 */
	public void addFunc(StatFunc func)
	{
		funcs.add(func);
	}

	/**
	 * Процесс расчета значения стата у персонажа.
	 *
	 * @param attacker тот, у кого находится эта функция.
	 * @param attacked целевой персонаж.
	 * @param skill кастуемый скил.
	 * @param value базовое значение.
	 * @return конечное значение.
	 */
	public float calc(Character attacker, Character attacked, Skill skill, float value)
	{
		StatFunc[] array = funcs.array();

		for(int i = 0, length = funcs.size(); i < length; i++)
			value = array[i].calc(attacker, attacked, skill, value);

		return value;
	}

	/**
	 * Процесс расчета значения стата у персонажа.
	 *
	 * @param attacker тот, у кого находится эта функция.
	 * @param attacked целевой персонаж.
	 * @param skill кастуемый скил.
	 * @param value базовое значение.
	 * @param order максимальный ордер для функции.
	 * @return конечное значение.
	 */
	public float calcToOrder(Character attacker, Character attacked, Skill skill, float value, int order)
	{
		StatFunc[] array = funcs.array();

		for(int i = 0, length = funcs.size(); i < length; i++)
		{
			StatFunc func = array[i];

			if(func.getOrder() > order)
				break;

			value = func.calc(attacker, attacked, skill, value);
		}

		return value;
	}

	/**
	 * @return список функций.
	 */
	public Array<StatFunc> getFuncs()
	{
		return funcs;
	}

	/**
	 * @return есть ли фукции в калькуляторе.
	 */
	public boolean isEmpty()
	{
		return funcs.isEmpty();
	}

	/**
	 * Удалить функцию с калькулятора.
	 *
	 * @param func удаляемая функция.
	 */
	public void removeFunc(StatFunc func)
	{
		funcs.slowRemove(func);
	}

	/**
	 * @param funcs список функций.
	 */
	public void setFuncs(Array<StatFunc> funcs)
	{
		this.funcs = funcs;
	}

	/**
	 * @return size кол-во функций.
	 */
	public int size()
	{
		return funcs.size();
	}
}
