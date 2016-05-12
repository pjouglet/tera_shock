package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.GuildRank;
import tera.gameserver.model.playable.Player;

/**
 * @author Ronn
 */
public class RequestGuildUpdateRank extends ClientPacket
{
	/** игрок */
	private Player player;

	/** ид игрока, которому меняем ранг */
	private int objectId;
	/** ид нового ранга */
	private int rankId;

	@Override
	public void finalyze()
	{
		player = null;
		objectId = 0;
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

		objectId = readInt();//04 00 00 10  Обжект ид педа
		rankId = readInt();//03 00 00 00  код ранга для нашей гильды
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

		guild.updateRank(player, objectId, rankId);
	}
}
