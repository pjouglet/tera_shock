package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет с запросом информации о клане.
 *
 * @author Ronn
 */
public class RequestGuildInfo extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
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
			player.updateGuild();
	}
}