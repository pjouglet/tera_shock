package tera.gameserver.model.regenerations;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.playable.Player;

/**
 * Модель негативной регенерации мп у игрока.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public class PlayerNegativeRegenMp extends AbstractRegen<Player>
{
	/** время посленего накопления до отказа */
	private long lastRestored;

	/** запущен ли реген после полной очистки мп */
	private boolean postEmpty;

	public PlayerNegativeRegenMp(Player actor)
	{
		super(actor);
	}

	@Override
	public boolean checkCondition()
	{
		// получаем игрока
		Player actor = getActor();

		// если он в боевой стойки, то нельзя
		if(actor.isBattleStanced())
			return false;

		// получаем текущее сзначение мп
		int currentMp = actor.getCurrentMp();

		// если мп нулевое, то нельзя
		if(currentMp < 1)
		{
			// запоминаем факт достижения пустого мп
			postEmpty = true;
			return false;
		}

		// если это после опустошения мп и его меньше 30, то нельзя
		if(postEmpty && currentMp < 30)
			return false;

		// убераем флаг опусташонного мп
		postEmpty = false;

		// получаем текущее время
		long current = System.currentTimeMillis();

		// если время от последнего каста скила меньше 6 сек ,то нельзя
		if(current - actor.getLastCast() < 6000L)
			return false;

		// если время от последнего полного восстановления меньше 10 сек, то нельзя
		if(current - lastRestored < 10000L)
			return false;

		// можно активировать
		return true;
	}

	@Override
	public void doRegen()
	{
		// получаем игрока
		Player actor = getActor();

		// если на данный момент фул мп, запоминаем это время
		if(actor.getCurrentMp() >= actor.getMaxMp())
			lastRestored = System.currentTimeMillis();

		// применяем реген
		actor.setCurrentMp(actor.getCurrentMp() + actor.getRegenMp());

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем отображение мп
		eventManager.notifyMpChanged(actor);
	}
}
