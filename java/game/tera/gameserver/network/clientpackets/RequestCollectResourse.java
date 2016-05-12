package tera.gameserver.network.clientpackets;

import tera.gameserver.model.World;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;

/**
 * Клиентский пакет, запрашивающий сбор ресурса.
 *
 * @author Ronn
 */
public class RequestCollectResourse extends ClientPacket
{
	/** обджект ид ресурса */
	private int objectId;
	/** саб ид ресурса */
	private int subId;

	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return true;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		objectId = readInt();
		subId = readInt();
	}

	@Override
	public void runImpl()
	{
		if(player == null)
			return;

		// ищим вокруг ресурс
		ResourseInstance resourse = World.getAroundById(ResourseInstance.class, player, objectId, subId);

		// если его нет или он невидим, выходим
		if(resourse == null || resourse.isInvisible())
			return;

		// запускаем попытку сбора
		player.getAI().startCollectResourse(resourse);
	}
}