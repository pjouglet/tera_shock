package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.playable.Player;

/**
 * Передача мастера гильдии.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class RequestGuildMakeLeader extends ClientPacket
{
	/** имя передоваемого */
	private String name;
	/** мастер гильдии */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
		name = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readShort();

		name = readString();//61 00 75 00 73 00 74 00 00 00   ..AO..F.a.u.s.t.
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Guild guild = player.getGuild();

		if(guild != null)
			guild.makeGuildMaster(player, name);
	}
}
