package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.playable.Player;

/**
 * Отмена трейда.
 *
 * @author Ronn
 */
public class CancelTrade extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player  = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		ActionDialog dialog = player.getLastActionDialog();

		if(dialog == null)
			return;

		dialog.cancel(player);
	}
}
