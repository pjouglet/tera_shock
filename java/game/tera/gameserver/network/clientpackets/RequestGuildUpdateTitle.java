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
public class RequestGuildUpdateTitle extends ClientPacket
{
	/** имя передоваемого */
	private String title;
	/** мастер гильдии */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
		title = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readShort();

		title = readString();//61 00 75 00 73 00 74 00 00 00   ..AO..F.a.u.s.t.
	}

	@Override
	protected void runImpl()
	{
		try
		{
			if(player == null || title.length() > 44)
				return;

			GuildRank rank = player.getGuildRank();

			if(rank == null || !rank.isChangeTitle())
				return;

			Guild guild = player.getGuild();

			if(guild != null)
				guild.setTitle(title);
		}
		finally
		{
			player.updateGuild();
		}
	}
}
