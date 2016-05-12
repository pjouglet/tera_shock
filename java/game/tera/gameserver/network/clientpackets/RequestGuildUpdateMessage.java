package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.playable.Player;

/**
 * Обнвление титула гильдии.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class RequestGuildUpdateMessage extends ClientPacket
{
	/** имя передоваемого */
	private String message;
	/** мастер гильдии */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
		message = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readShort();

		message = readString();//61 00 75 00 73 00 74 00 00 00   ..AO..F.a.u.s.t.
	}

	@Override
	protected void runImpl()
	{
		try
		{
			if(player == null || message.length() > 254)
				return;

			GuildRank rank = player.getGuildRank();

			if(rank == null || !rank.isChangeTitle())
				return;

			Guild guild = player.getGuild();

			if(guild != null)
				guild.setMessage(message);
		}
		finally
		{
			player.updateGuild();
		}
	}
}
