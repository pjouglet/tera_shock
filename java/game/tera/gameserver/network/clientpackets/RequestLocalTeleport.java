package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.DialogType;
import tera.gameserver.model.npc.interaction.dialogs.TeleportDialog;
import tera.gameserver.model.playable.Player;

/**
 * Запрос на локальный телепорт в выбранную точку.
 *
 * @author Ronn
 * @created 26.02.2012
 */
public class RequestLocalTeleport extends ClientPacket
{
	/** игрок */
	private Player player;

	/** номер маршрута */
	private int index;

	@Override
	public void finalyze()
	{
		player  = null;
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

		index = readInt();//08 00 00 00 покакому маршруту
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Dialog dialog = player.getLastDialog();

		if(dialog == null || dialog.getType() != DialogType.TELEPORT)
			return;

		TeleportDialog teleport = (TeleportDialog) dialog;

		teleport.teleport(index);
	}
}
