package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.InventoryInfo;

/**
 * Клиентский пакет, запрасывающий пакет инвенторя.
 *
 * @author Ronns
 */
public class RequestInventoryInfo extends ClientPacket
{
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
		return false;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();
	}

	@Override
	public void runImpl()
	{
		if(player != null)
			player.sendPacket(InventoryInfo.getInstance(player), true);
	}
}