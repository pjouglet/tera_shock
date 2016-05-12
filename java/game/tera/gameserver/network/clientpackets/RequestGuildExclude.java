package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.playable.Player;

/**
 * @author Ronn
 */
public class RequestGuildExclude extends ClientPacket
{
	/** игрок */
	private Player player;
	/** имя исключаемого игрока */
	private String name;

	@Override
	public void finalyze()
	{
		player = null;
		name = null;
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

		readShort(); //06 00
		name = readString();//64 00 66 00 67 00 64 00 61 00 66 00 00 00
	}

	@Override
	protected void runImpl()
	{
		if(player == null && name.equals(player.getName()))
			return;

		Guild guild = player.getGuild();

		if(guild == null)
			return;

		GuildRank rank = player.getGuildRank();

		if(rank == null || !rank.isChangeLineUp())
			return;

		guild.exclude(player, name);
	}
}
