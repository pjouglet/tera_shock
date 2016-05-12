package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.Action;
import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет с инфой об отмене акшена.
 *
 * @author Ronn
 */
public class RequestActionCancel extends ClientPacket
{
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
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

		ActionDialog dialog = player.getLastActionDialog();

		if(dialog != null)
			dialog.cancel(player);
	}
}
