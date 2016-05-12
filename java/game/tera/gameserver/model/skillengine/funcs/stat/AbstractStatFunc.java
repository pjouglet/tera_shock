package tera.gameserver.model.skillengine.funcs.stat;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.StatType;
import tera.gameserver.model.skillengine.funcs.StatFunc;
import tera.gameserver.model.skillengine.lambdas.Lambda;

/**
 * Фундаментальная модель функции.
 *
 * @author Ronn
 */
public abstract class AbstractStatFunc implements StatFunc
{
	public static final StatFunc[] EMPTY_FUNC = new StatFunc[0];

	/** тип стата */
	protected StatType stat;

	/** порядок применения */
	protected int order;

	/** условие для применения */
	protected Condition condition;
	/** значение для вычисления */
	protected Lambda lambda;

	/**
	 * @param stat тип параметра, на который влияет функция.
	 * @param order порядок в списке функции.
	 * @param condition условия для применения функции.
	 * @param lambda модификатор функции.
	 */
	public AbstractStatFunc(StatType stat, int order, Condition condition, Lambda lambda)
	{
		this.stat = stat;
		this.order = order;
		this.condition = condition;
		this.lambda = lambda;
	}

	@Override
	public void addFuncTo(Character owner)
	{
		if(owner == null)
			return;

		owner.addStatFunc(this);
	}

	@Override
	public int compareTo(StatFunc func)
	{
		if(func == null)
			return -1;

		return order - func.getOrder();
	}

	@Override
	public int getOrder()
	{
		return order;
	}

	@Override
	public StatType getStat()
	{
		return stat;
	}

	@Override
	public void removeFuncTo(Character owner)
	{
		if(owner == null)
			return;

		owner.removeStatFunc(this);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " stat = " + stat + ", order = " + order + ", condition = " + condition + ", lambda = " + lambda;
	}
}
