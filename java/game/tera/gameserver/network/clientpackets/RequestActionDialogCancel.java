package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.playable.Player;

/**
 *  Клиентский пакет с отменой приглашения на акшен
 *
 * @author Ronn
 * @created 07.03.2012
 */
public class RequestActionDialogCancel extends ClientPacket
{
	/** потвердивший игрок */
	private Player player;

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();
	}

	@Override
	protected void runImpl()
	{
		ActionDialog dialog = player.getLastActionDialog();

		if(dialog != null)
			dialog.cancel(player);
	}
}
