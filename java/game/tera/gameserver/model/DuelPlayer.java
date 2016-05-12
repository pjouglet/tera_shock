package tera.gameserver.model;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.playable.Player;

/**
 * Контейнер состояние игрока для дуэли.
 *
 * @author Ronn
 */
public class DuelPlayer
{
	/** список откатываемых скилов */
	private Array<ReuseSkill> reuses;

	/** состояние хп перед дуэлью */
	private int hp;
	/** состояние мп перед дуэлью */
	private int mp;
	/** состояние стамины перед дуэлью */
	private int stamina;

	public DuelPlayer()
	{
		this.reuses = Arrays.toArray(ReuseSkill.class);
	}

	/**
	 * @return список скилов игрока в откате.
	 */
	public Array<ReuseSkill> getReuses()
	{
		return reuses;
	}

	/**
	 * Восстановление состояния игрока.
	 *
	 * @param player восстанавливаемый игрок.
	 */
	public void restore(Player player)
	{
		// восстанавливаем стамину
		player.setStamina(stamina);

		// восстанавливаем состояние хп
		player.setCurrentHp(hp);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем об этом
		eventManager.notifyHpChanged(player);

		// восстанавливаем состояние мп
		player.setCurrentMp(mp);

		// уведомляем об этом
		eventManager.notifyMpChanged(player);

		// получаем таблицу откатов скилов
		Table<IntKey, ReuseSkill> reuseTable = player.getReuseSkills();

		// получаем список откатываемых скилов
		Array<ReuseSkill> reuses = getReuses();

		// получаем текущее время
		long current = System.currentTimeMillis();

		// перебираем текущую таблицу откатов
		for(ReuseSkill reuse : reuseTable)
			if(!reuse.isItemReuse() && reuse.getEndTime() > current && !reuses.contains(reuse))
				player.enableSkill(player.getSkill(reuse.getSkillId()));

		// очищаем список
		reuses.clear();
	}

	/**
	 * Сохранение состояния игрока перед дуэлью.
	 *
	 * @param player сохраняемый игрок.
	 */
	public void save(Player player)
	{
		// запоминаем статы
		this.hp = player.getCurrentHp();
		this.mp = player.getCurrentMp();
		this.stamina = player.getStamina();

		// получаем таблицу откатов скилов
		Table<IntKey, ReuseSkill> reuseTable = player.getReuseSkills();

		// получаем список откатываемых скилов
		Array<ReuseSkill> reuses = getReuses();

		// очищаем его
		reuses.clear();

		// получаем текущее время
		long current = System.currentTimeMillis();

		// перебираем текущую таблицу откатов
		for(ReuseSkill reuse : reuseTable)
		{
			// если скил в откате
			if(!reuse.isItemReuse() && reuse.getEndTime() > current)
				// вносим в список его
				reuses.add(reuse);
		}
	}
}
