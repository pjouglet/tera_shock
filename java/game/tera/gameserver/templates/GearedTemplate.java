package tera.gameserver.templates;

import rlib.util.VarTable;
import tera.gameserver.model.items.BindType;

/**
 * Модель шаблона экипееруемых итемов.
 *
 * @author Ronn
 */
public class GearedTemplate extends ItemTemplate
{
	/** минимальный уровень для экиперовки */
	protected int requiredLevel;
	/** модификатор атаки */
	protected int attack;
	/** модификатор опрокидывания */
	protected int impact;
	/** модификатор защиты */
	protected int defence;
	/** модификатор сопротивления к опрокидыванию */
	protected int balance;
	/** лимит кристалов */
	protected int sockets;
	/** необходимый уровень скила экстракта дл разбивки итема */
	protected int extractable;

	/** можно ли зачаровывать итем */
	protected boolean enchantable;
	/** можно ли его моделировать */
	protected boolean remodelable;
	/** можно ли красить итем */
	protected boolean dyeable;

	/** тип закрепления итема */
	protected BindType bindType;

	public GearedTemplate(Enum<?> type, VarTable vars)
	{
		super(type, vars);

		try
		{
			attack = vars.getInteger("attack", 0);
			impact = vars.getInteger("impact", 0);
			defence = vars.getInteger("defence", 0);
			balance = vars.getInteger("balance", 0);
			sockets = vars.getInteger("sockets", 0);
			extractable = vars.getInteger("extractable", 0);
			requiredLevel = vars.getInteger("requiredLevel", 0);
			itemLevel = vars.getInteger("itemLevel", requiredLevel);

			enchantable = vars.getBoolean("enchantable", true);
			remodelable = vars.getBoolean("remodelable", true);
			dyeable = vars.getBoolean("dyeable", true);
			stackable = false;

			bindType = BindType.valueOfXml(vars.getString("bindType", "none"));
		}
		catch(Exception e)
		{
			log.warning(this, e);
			throw e;
		}
	}

	@Override
	public final int getAttack()
	{
		return attack;
	}

	@Override
	public final int getBalance()
	{
		return balance;
	}

	@Override
	public final BindType getBindType()
	{
		return bindType;
	}

	@Override
	public final int getDefence()
	{
		return defence;
	}

	@Override
	public final int getExtractable()
	{
		return extractable;
	}

	@Override
	public final int getImpact()
	{
		return impact;
	}

	@Override
	public final int getRequiredLevel()
	{
		return requiredLevel;
	}

	@Override
	public final int getSockets()
	{
		return sockets;
	}

	@Override
	public final boolean isEnchantable()
	{
		return enchantable;
	}

	@Override
	public final boolean isRemodelable()
	{
		return remodelable;
	}
}
