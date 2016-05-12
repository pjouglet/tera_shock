package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.DeleteCharacter;
import tera.gameserver.network.serverpackets.Tp1;
import tera.gameserver.network.serverpackets.WorldZone;

/**
 * Запрос клиента на ид зоны.
 *
 * @author Ronn
 */
public class RequestWorldZone extends ClientPacket
{
	/** игрок */
	private Player player;

	/** ид зоны */
	private int zoneId;

	@Override
	public void finalyze()
	{
		player  = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readInt();//03 00 00 00

		zoneId = readInt();//59 1B 00 00 айди который нужно отправить в ворлд зон
	}

	@Override
	protected void runImpl()
	{
		if(player == null || player.isFlyingPegas() || player.getZoneId() == zoneId)
			return;

		//log.warning(this, "incorrect zone id " + zoneId + ", player " + player.getZoneId());

		// если игрон на маунте
		if(player.isOnMount())
			// слезаем с него
			player.getOffMount();

		// останавливаем движение
		player.stopMove();

		// убираем из мира
		player.decayMe(DeleteCharacter.DISAPPEARS);

		// отправляем пакет телепорта
		player.broadcastPacket(Tp1.getInstance(player));

		// обновляем ид зоны
		player.setZoneId(zoneId);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// обновляем ид зоны
		eventManager.notifyChangedZoneId(player);

		// отправялям пакет с зоной
		player.sendPacket(WorldZone.getInstance(player), true);
	}
}
