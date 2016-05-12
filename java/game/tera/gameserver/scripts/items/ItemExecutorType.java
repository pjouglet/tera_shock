package tera.gameserver.scripts.items;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import rlib.logging.Loggers;

/**
 * Виды используемых итемов.
 *
 * @author Ronn
 * @created 04.03.2012
 */
public enum ItemExecutorType
{
	/** награда за ивент */
	EVENT_REWARD_BOX(EventRewardBox.class, 0, 408, 409, 410, 411),
	SKILL_LEARN_ITEM(SkillLearnItem.class, 0, 20, 21, 41, 166, 167, 168, 169, 170, 306, 307, 336, 350, 351, 384, 385, 412, 413, 414, 415, 416, 417, 425),
	BARBECUE_ITEMS(BarbecueItem.class, 0, 5027);

	/** конструктор обработчика */
	private Constructor<? extends ItemExecutor> constructor;
	/** уровень доступа для игрока */
	private int access;
	/** массив идов итемов */
	private int[] itemIds;

	/**
	 * @param type тип обработчика.
	 * @param access минимальный уровень доступа.
	 * @param itemIds массив итем ид.
	 */
	private ItemExecutorType(Class<? extends ItemExecutor> type, int access, int... itemIds)
	{
		this.access = access;
		this.itemIds = itemIds;

		try
		{
			constructor = type.getConstructor(int[].class, int.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			Loggers.warning(this, e);
		}
	}

	/**
	 * @return кол-во итем идов.
	 */
	public int getCount()
	{
		return itemIds.length;
	}

	/**
	 * @return новый экземпляр обработчика.
	 */
	public ItemExecutor newInstance()
	{
		try
		{
			return constructor.newInstance(itemIds, access);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, e);
		}

		return null;
	}
}
