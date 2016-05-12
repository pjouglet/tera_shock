package tera.gameserver.model.actions;

import rlib.logging.Loggers;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.actions.classes.BindItemAction;
import tera.gameserver.model.actions.classes.DuelStartAction;
import tera.gameserver.model.actions.classes.EnchantItemAction;
import tera.gameserver.model.actions.classes.GuildCreateAction;
import tera.gameserver.model.actions.classes.GuildInviteAction;
import tera.gameserver.model.actions.classes.PartyInviteAction;
import tera.gameserver.model.actions.classes.TradeStartAction;
import tera.gameserver.model.playable.Player;

/**
 * Перечисление типов акшенов.
 * 
 * @author Ronn
 * @created 07.03.2012
 */
public enum ActionType
{
	NONE(null),
	NONE1(null),
	NONE2(null),
	TRADE(TradeStartAction.class),
	PARTY(PartyInviteAction.class),
	JOIN_PARTY(null),
	NONE6(null),
	NONE7(null),
	NONE8(null),
	NONE9(null),
	CREATE_GUILD(GuildCreateAction.class),
	INVITE_GUILD(GuildInviteAction.class),
	DUEL(DuelStartAction.class),
	NONE13(null),
	NONE14(null),
	NONE15(null),
	NONE16(null),
	NONE17(null),
	NONE18(null),
	NONE19(null),
	NONE20(null),
	NONE21(null),
	NONE22(null),
	NONE23(null),
	NONE24(null),
	NONE25(null),
	NONE26(null),
	NONE27(null),
	NONE28(null),
	NONE29(null),
	NONE30(null),
	NONE31(null),
	BIND_ITEM(BindItemAction.class),
	NONE32(null),
	ENCHANT_ITEM(EnchantItemAction.class), ;

	/** массив типов */
	public static final ActionType[] ELEMENTS = values();

	/**
	 * Получение типа акшена по индексу.
	 * 
	 * @param index индекс типа акшена.
	 * @return тип акшена.
	 */
	public static ActionType valueOf(int index)
	{
		if (index < 0 || index >= ELEMENTS.length)
			return ActionType.NONE;

		return ELEMENTS[index];
	}

	/** пул акшенов */
	private FoldablePool<Action> pool;

	/** тип акшена */
	private Class<? extends Action> type;

	private ActionType(Class<? extends Action> type)
	{
		this.type = type;
		this.pool = Pools.newConcurrentFoldablePool(Action.class);
	}

	/**
	 * @return пул акшенов.
	 */
	public final FoldablePool<Action> getPool()
	{
		return pool;
	}

	/**
	 * @return реализован ли такой акшен.
	 */
	public boolean isImplemented()
	{
		return type != null;
	}

	/**
	 * Создание нового экземпляра акшена.
	 * 
	 * @param actor инициатор акшена.
	 * @param name имя цели.
	 * @return новый акшен.
	 */
	public Action newInstance(Player actor, String name)
	{
		Action action = pool.take();

		if (action == null)
			try
			{
				action = type.newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				Loggers.warning(this, e);
			}

		action.init(actor, name);

		return action;
	}
}
