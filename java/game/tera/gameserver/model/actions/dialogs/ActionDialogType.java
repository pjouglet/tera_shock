package tera.gameserver.model.actions.dialogs;

import rlib.logging.Loggers;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

/**
 * Перечисление типов диалогов акшенов.
 * 
 * @author Ronn
 */
public enum ActionDialogType
{
	NULL(null),
	NULL1(null),
	NULL2(null),
	/** диалог трейда */
	TRADE_DIALOG(TradeDialog.class),
	/** диалог зачоравания вещей */
	ENCHANT_ITEM_DIALOG(EnchantItemDialog.class);

	/** пул диалогов */
	private final FoldablePool<ActionDialog> pool;

	/** тип диалога */
	private final Class<? extends ActionDialog> type;

	private ActionDialogType(Class<? extends ActionDialog> type)
	{
		this.pool = Pools.newConcurrentFoldablePool(ActionDialog.class);
		this.type = type;
	}

	/**
	 * @return пул диалогов.
	 */
	public FoldablePool<ActionDialog> getPool()
	{
		return pool;
	}

	/**
	 * @return новый инстанс диалога.
	 */
	public ActionDialog newInstance()
	{
		ActionDialog dialog = pool.take();

		if (dialog == null)
			try
			{
				dialog = type.newInstance();
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				Loggers.warning(this, e);
			}

		return dialog;
	}
}
