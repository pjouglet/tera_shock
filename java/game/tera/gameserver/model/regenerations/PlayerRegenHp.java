package tera.gameserver.model.regenerations;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.playable.Player;

/**
 * Модель регенерации хп у игрока.
 *
 * @author Ronn
 * @created 11.04.2012
 */
public class PlayerRegenHp extends AbstractRegen<Player>
{
	public PlayerRegenHp(Player actor)
	{
		super(actor);
	}

	@Override
	public boolean checkCondition()
	{
		// получаем игрока
		Player actor = getActor();

		// можно только не в боевой стойки и если не фул хп
		return !actor.isBattleStanced() && actor.getCurrentHp() < actor.getMaxHp();
	}

	@Override
	public void doRegen()
	{
		// получаем игрока
		Player actor = getActor();

		// применяем реген
		actor.setCurrentHp(actor.getCurrentHp() + actor.getRegenHp());

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем отображение хп
		eventManager.notifyHpChanged(actor);
	}
}
