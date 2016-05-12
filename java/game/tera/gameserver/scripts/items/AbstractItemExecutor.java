package tera.gameserver.scripts.items;

import java.util.Arrays;

import rlib.logging.Logger;
import rlib.logging.Loggers;

/**
 * Фундамент для обработки активного итема.
 *
 * @author Ronn
 * @created 27.03.2012
 */
public abstract class AbstractItemExecutor implements ItemExecutor
{
	protected static final Logger log = Loggers.getLogger(ItemExecutor.class);

	/** массив итем ид, на которых распространяется обработчик */

	private int[] itemIds;
	/** уровень доступа игрок на выполнения обработчика */
	private int access;

	/**
	 * @param itemIds массив итем ид.
	 * @param access минимальный уровень прав для доступа.
	 */
	public AbstractItemExecutor(int[] itemIds, int access)
	{
		this.itemIds = itemIds;
		this.access = access;
	}

	@Override
	public int getAccess()
	{
		return access;
	}

	@Override
	public int[] getItemIds()
	{
		return itemIds;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " " + (itemIds != null ? "itemIds = " + Arrays.toString(itemIds) + ", " : "") + "access = " + access;
	}
}
