package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет с запросом информации о клане.
 *
 * @author Ronn
 */
public class RequestGuildLeave extends ClientPacket
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
		if(player == null)
			return;

		Guild guild = player.getGuild();

		if(guild != null)
			guild.leaveMember(player);
	}
}