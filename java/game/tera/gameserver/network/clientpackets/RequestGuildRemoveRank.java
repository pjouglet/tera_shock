package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.playable.Player;

/**
 * @author Ronn
 */
public class RequestGuildRemoveRank extends ClientPacket
{
	/** игрок */
	private Player player;

	/** ид удаляемого ранка */
	private int rankId;

	@Override
	public void finalyze()
	{
		player = null;
		rankId = 0;
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

		rankId = readInt();//03 00 00 00
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

		guild.removeRank(player, rankId);
	}
}
