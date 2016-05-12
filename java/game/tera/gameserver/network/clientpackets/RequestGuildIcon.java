package tera.gameserver.network.clientpackets;

import tera.gameserver.manager.GuildManager;
import tera.gameserver.model.GuildIcon;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.GuildIconInfo;

/**
 * Отмена трейда.
 *
 * @author Ronn
 */
public class RequestGuildIcon extends ClientPacket
{
	/** игрок */
	private Player player;

	/** зназвание иконки */
	private String name;

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

		readShort();//06 00 67 00
		name = readString();//75 00 69 00 6C 00 64 00    ..?¬..g.u.i.l.d.
	}

	@Override
	protected void runImpl()
	{
		if(player == null || name == null)
			return;

		// получаем менеджера гильдий
		GuildManager guildManager = GuildManager.getInstance();

		GuildIcon icon = guildManager.getIcon(name);

		if(icon == null || !icon.hasIcon())
			return;

		player.sendPacket(GuildIconInfo.getInstance(icon), true);
	}
}
