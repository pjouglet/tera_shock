package tera.gameserver.network.clientpackets;

import tera.gameserver.model.Guild;
import tera.gameserver.model.playable.Player;

/**
 * Обнвление заметки игрока гильдии.
 *
 * @author Ronn
 * @created 26.04.2012
 */
public class RequestGuildUpdateNote extends ClientPacket
{
	/** новая замтека */
	private String note;
	/** член гильдии */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
		note = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readShort();

		note = readString();//61 00 75 00 73 00 74 00 00 00   ..AO..F.a.u.s.t.
	}

	@Override
	protected void runImpl()
	{
		if(player == null || note.length() > 34)
			return;

		Guild guild = player.getGuild();

		if(guild != null)
			guild.changeMemberNote(player, note);
	}
}
