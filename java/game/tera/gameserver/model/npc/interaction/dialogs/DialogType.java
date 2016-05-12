package tera.gameserver.model.npc.interaction.dialogs;

import rlib.logging.Loggers;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;

/**
 * Перечисление типов диалоговых окон.
 *
 * @author Ronn
 * @created 25.02.2012
 */
public enum DialogType
{
	/** окно магазина */
	SHOP_WINDOW(ShopDialog.class),
	/** окно мульти магазина */
	MULTI_SHOP(MultiShopDialog.class),
	/** окно изучения скилов */
	SKILL_SHOP(SkillShopDialog.class),
	/** окно выбора места назначения */
	PEGAS(PegasDialog.class),
	/** окно телепорта */
	TELEPORT(TeleportDialog.class),
	/** окно диалога банка */
	PLAYER_BANK(PlayerBankDialog.class),
	/** создание гильдии */
	GUILD_CREATE(CreateGuildDialog.class),
	/** окно для загрузки картинок */
	GUILD_LOAD_ICON(LoadGuildIcon.class),
	/** диалог банка гильдии */
	GUILD_BANK(GuildBankDialog.class),
	;

	/** массив енумов */
	public static final DialogType[] ELEMENTS = values();

	/** пул диалогов */
	private final FoldablePool<Dialog> pool;
	/** класс окна */
	private final Class<? extends Dialog> type;

	private DialogType(Class<? extends Dialog> type)
	{
		this.type = type;
		this.pool = Pools.newConcurrentFoldablePool(type);
	}

	/**
	 * @return пул диалогов соответсвующего типа.
	 */
	public final FoldablePool<Dialog> getPool()
	{
		return pool;
	}

	/**
	 * Сравнение типа с диалогом.
	 *
	 * @param dialog экземпляр диалога.
	 * @return является ли этого же типа.
	 */
	public boolean isInstance(Dialog dialog)
	{
		return type.isInstance(dialog);
	}

	/**
	 * Получение нового экземпляра диалога.
	 *
	 * @param type класс диалога.
	 * @return новый экземпляр.
	 */
	public final Dialog newInstance()
	{
		// получаем экземпляр с пула
		Dialog dialog = pool.take();

		// если такого нету
		if(dialog == null)
			try
			{
				// создаем новый
				dialog = type.newInstance();
			}
			catch(InstantiationException | IllegalAccessException e)
			{
				Loggers.warning(this, e);
			}

		return dialog;
	}
}
