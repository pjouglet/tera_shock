package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.Action;
import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, уведомляющий о закрытии диалогового окна
 *
 * @author Ronn
 * @created 24.02.2012
 */
public class RequestDialogCancel extends ClientPacket
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

		Action action = player.getLastAction();

		if(action != null)
			action.cancel(player);

		Dialog dialog = player.getLastDialog();

		if(dialog != null)
			dialog.close();

		ActionDialog actionDialog = player.getLastActionDialog();

		if(actionDialog != null)
			actionDialog.cancel(player);
	}
}
