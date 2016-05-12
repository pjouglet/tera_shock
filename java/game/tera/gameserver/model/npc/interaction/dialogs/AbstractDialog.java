package tera.gameserver.model.npc.interaction.dialogs;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.pools.Foldable;
import rlib.util.pools.FoldablePool;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;

/**
 * Базовая модель диалога с нпс.
 *
 * @author Ronn
 * @created 24.02.2012
 */
public abstract class AbstractDialog implements Dialog, Foldable
{
	protected static final Logger log = Loggers.getLogger(AbstractDialog.class);

	/** тип диалогового окна */
	protected DialogType type;

	/** нпс, с которым заговорили */
	protected Npc npc;

	/** игрок с которым говорим */
	protected Player player;

	public AbstractDialog()
	{
		this.type = getType();
	}

	@Override
	public boolean apply()
	{
		return false;
	}

	@Override
	public synchronized boolean close()
	{
		// игрок, ведущий диалог
		Player player = getPlayer();

		// получаем нпс этого диалога
		Npc npc = getNpc();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		if(npc == null)
			log.warning(this, new Exception("not found npc"));
		else
		{
			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляем о закрытии диалога
			eventManager.notifyStopDialog(npc, player);
		}

		// зануляем диалог игроку
		player.setLastDialog(null);

		// получаем пул диалогов
		FoldablePool<Dialog> pool = type.getPool();

		// ложим в пул
		pool.put(this);

		return true;
	}

	@Override
	public void finalyze()
	{
		npc = null;
		player = null;
	}

	@Override
	public final Npc getNpc()
	{
		return npc;
	}

	@Override
	public final Player getPlayer()
	{
		return player;
	}

	@Override
	public abstract DialogType getType();

	@Override
	public boolean init()
	{
		// получаем игрока
		Player player = getPlayer();

		// если его нет, выходим
		if(player == null)
		{
			log.warning(this, new Exception("not found player"));
			return false;
		}

		// получаем последний диалог
		Dialog old = player.getLastDialog();

		// если он есть
		if(old != null)
			// закрываем его
			old.close();

		// получаем нпс
		Npc npc = getNpc();

		if(npc == null)
			log.warning(this, new Exception("not found npc"));
		else
		{
			// получаем менеджера событий
			ObjectEventManager eventManager = ObjectEventManager.getInstance();

			// уведомляем о открытии диалога
			eventManager.notifyStartDialog(npc, player);
		}

		// запоминаем диалог у игрока
		player.setLastDialog(this);

		return true;
	}

	@Override
	public void reinit(){}

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}
