package tera.gameserver.events;

import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.model.playable.Player;
import tera.util.Location;

/**
 * Обертка для игрока для участия в ивентах.
 * 
 * @author Ronn
 */
public final class EventPlayer implements Foldable {

	public static final FoldablePool<EventPlayer> pool = Pools.newConcurrentFoldablePool(EventPlayer.class);

	/**
	 * Создание новой ивент обертки под указанного игрока.
	 * 
	 * @param player целевой игрок.
	 * @return новая обертка.
	 */
	public static EventPlayer newInstance(Player player) {

		EventPlayer eventPlayer = pool.take();

		if(eventPlayer == null) {
			eventPlayer = new EventPlayer();
		}

		eventPlayer.player = player;

		return eventPlayer;
	}

	/** целевой игрок */
	private Player player;

	/** сохраненная позиция */
	private final Location saveLoc;

	/** сохраненные мп/хп */
	private int mp;
	private int hp;
	private int stamina;

	/** счетчик игрока */
	private int counter;

	private EventPlayer() {
		this.saveLoc = new Location();
	}

	@Override
	public void finalyze() {
		player = null;
	}

	/**
	 * Складировать в пул.
	 */
	public void fold() {
		pool.put(this);
	}

	/**
	 * @return игрок.
	 */
	public final Player getPlayer() {
		return player;
	}

	@Override
	public void reinit() {
		mp = 0;
		hp = 0;
		stamina = 0;
		counter = 0;
	}

	/**
	 * @return счетчик игрока.
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * Увеличение счетчика игрока на 1.
	 */
	public void incrementCounter() {
		counter += 1;
	}

	/**
	 * Вернуть на сохраненную позицию.
	 */
	public void restoreLoc() {
		player.teleToLocation(saveLoc);
	}

	/**
	 * Восстановить состояние.
	 */
	public void restoreState() {
		player.setCurrentHp(hp);
		player.setCurrentMp(mp);
		player.setStamina(stamina);
		player.updateInfo();
	}

	/**
	 * Сохранение позиции игрока.
	 */
	public void saveLoc() {
		saveLoc.setXYZ(player.getX(), player.getY(), player.getZ());
		saveLoc.setContinentId(player.getContinentId());
	}

	/**
	 * Сохранение состояния игрока.
	 */
	public void saveState() {
		hp = player.getCurrentHp();
		mp = player.getCurrentMp();
		stamina = player.getStamina();
	}
}
