package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.actions.dialogs.ActionDialogType;
import tera.gameserver.model.actions.dialogs.TradeDialog;
import tera.gameserver.model.playable.Player;

/**
 * Запрос на блокировку трейда.
 *
 * @author Ronn
 */
public class RequestTradeLock extends ClientPacket
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

		if(dialog == null || dialog.getType() != ActionDialogType.TRADE_DIALOG)
			return;

		TradeDialog trade = (TradeDialog) dialog;

		trade.lock(player);
	}
}
