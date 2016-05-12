package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.playable.Player;

/**
 * @author Ronn
 */
public class RequestGuildCreateRank extends ClientPacket
{
	/** игрок */
	private Player player;

	/** название ранка */
	private String rankName;

	@Override
	public void finalyze()
	{
		player = null;
		rankName = null;
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

		readShort();
		rankName = readString();//66 00 66 00 66 00 66 00 66 00 00 00   ......f.f.f.f.f.
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Guild guild = player.getGuild();

		if(guild == null)
			return;

		GuildRank rank = player.getGuildRank();

		if(rank == null || !rank.isGuildMaster())
			return;

		guild.createRank(player, rankName);
	}
}
