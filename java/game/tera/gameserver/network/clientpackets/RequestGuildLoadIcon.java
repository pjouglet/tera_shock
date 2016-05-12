package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.DialogType;
import tera.gameserver.model.npc.interaction.dialogs.LoadGuildIcon;
import tera.gameserver.model.playable.Player;

/**
 * Загрузка иконки гильдии.
 *
 * @author Ronn
 */
public class RequestGuildLoadIcon extends ClientPacket
{
	/** игрок */
	private Player player;

	/** загрузка картинки на сервер */
	private byte[] icon;

	@Override
	public void finalyze()
	{
		player  = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readShort();

		int size = readShort();

		if(size > buffer.remaining())
		{
			log.warning(this, "incorrect load guild icon, size " + size + ", remaining " + buffer.remaining());
			return;
		}

		icon = new byte[size];

		buffer.get(icon);
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		if(icon == null || icon.length < 5)
		{
			player.sendMessage("Загрузка произошла неудачно.");
			return;
		}

		Dialog dialog = player.getLastDialog();

		if(dialog == null || dialog.getType() != DialogType.GUILD_LOAD_ICON)
			return;

		LoadGuildIcon load = (LoadGuildIcon) dialog;

		load.setIcon(icon);
		load.apply();
	}
}
