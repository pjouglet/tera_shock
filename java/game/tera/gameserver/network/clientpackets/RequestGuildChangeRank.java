package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.GuildRankLaw;
import tera.gameserver.model.playable.Player;

/**
 * @author Ronn
 */
public class RequestGuildChangeRank extends ClientPacket
{
	/** игрок */
	private Player player;

	/** набор прав */
	private GuildRankLaw law;

	/** название ранга */
	private String name;

	/** номер ранга */
	private int index;

	@Override
	public void finalyze()
	{
		player = null;
		law = null;
		index = 0;
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

		readShort();//0E 00
		index = readInt();//03 00 00 00
		law = GuildRankLaw.valueOf(readInt());//17 00 00 00
		name = readString();//4D 00 65 00 6D 00 62 00 65 00 72 00 00 00
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

		guild.changeRank(player, index, name, law);
	}
}
