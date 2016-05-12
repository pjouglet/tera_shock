package tera.gameserver.model.actions.classes;

import tera.gameserver.model.playable.Player;

/**
 * Базовый акшен между 2мя игроками.
 * 
 * @author Ronn
 */
public abstract class PlayerAction extends AbstractAction<Player>
{
	@Override
	protected final synchronized void clear()
	{
		// получаем цель акщена
		Player target = getTarget();
		
		// если она есть
		if(target != null)
			// зануляем ей акшен
			target.setLastAction(null);
		
		super.clear();
	}
}
