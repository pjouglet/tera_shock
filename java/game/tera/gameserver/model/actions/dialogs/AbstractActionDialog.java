package tera.gameserver.model.actions.dialogs;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ActionDialogCancel;

/**
 * Базовая модель диалога акшенов.
 *
 * @author Ronn
 */
public abstract class AbstractActionDialog implements ActionDialog
{
	protected static final Logger log = Loggers.getLogger(ActionDialog.class);

	/** инициатор диалога */
	protected Player actor;
	/** компоньен инициатора */
	protected Player enemy;

	/** тип диалога */
	protected ActionDialogType type;

	/** обджект ид окна */
	protected int objectId;

	public AbstractActionDialog()
	{
		this.type = getType();
	}

	@Override
	public synchronized void cancel(Player player)
	{
		// получаем инициатора
		Player actor = getActor();
		// получаем опонента
		Player enemy = getEnemy();

		// если кого-то из них нет, выодим
		if(actor == null || enemy == null)
			return;

		clear();

		actor.sendPacket(ActionDialogCancel.getInstance(actor, enemy, type.ordinal(), objectId), true);
		enemy.sendPacket(ActionDialogCancel.getInstance(enemy, actor, type.ordinal(), objectId), true);

		getType().getPool().put(this);
	}

	/**
	 * Очистка.
	 */
	protected void clear()
	{
		actor.setLastActionDialog(null);
		enemy.setLastActionDialog(null);
	}

	@Override
	public void finalyze()
	{
		actor = null;
		enemy = null;
	}

	@Override
	public Player getActor()
	{
		return actor;
	}

	@Override
	public Player getEnemy()
	{
		return enemy;
	}

	@Override
	public Player getEnemy(Player player)
	{
		if(player == actor)
			return enemy;
		else
			return actor;
	}

	@Override
	public synchronized boolean init()
	{
		actor.setLastActionDialog(this);
		enemy.setLastActionDialog(this);

		return true;
	}

	@Override
	public void reinit(){}
}
