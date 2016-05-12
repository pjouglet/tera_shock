package tera.gameserver.model.regenerations;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.playable.Player;

/**
 * Модель позитивной регенерации мп у игроков.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public class PlayerPositiveRegenMp extends AbstractRegen<Player>
{
	private static final int BATTLE_MOD = 2;

	public PlayerPositiveRegenMp(Player actor)
	{
		super(actor);
	}

	@Override
	public boolean checkCondition()
	{
		// получаем игрока
		Player actor = getActor();

		// можно активировать реген
		return actor.getCurrentMp() < actor.getMaxMp();
	}

	@Override
	public void doRegen()
	{
		//получаем игрока
		Player actor = getActor();

		// если игрок в боевй стойке
		if(actor.isBattleStanced())
			// регенирируем с усилением
			actor.setCurrentMp(actor.getCurrentMp() + actor.getRegenMp() * BATTLE_MOD);
		else
			// иначе простой реген
			actor.setCurrentMp(actor.getCurrentMp() + actor.getRegenMp());

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем отображение мп
		eventManager.notifyMpChanged(actor);
	}
}
