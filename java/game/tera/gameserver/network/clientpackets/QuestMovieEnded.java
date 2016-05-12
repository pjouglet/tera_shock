package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет об завершении просмотра квестового мувика.
 *
 * @author Ronn
 */
public class QuestMovieEnded extends ClientPacket
{
	/** игрок */
	private Player player;

	/** ид мувика */
	private int id;

	/** принудительно ли завершен */
	private boolean force;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		id = readInt();
		force = readByte() != 0;
	}

	@Override
	public void runImpl()
	{
		// если игрока нет, выходим
		if(player == null)
			return;

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		eventManager.notifyQuestMovieEnded(player, id, force);
	}
}