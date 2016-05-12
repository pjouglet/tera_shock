package tera.gameserver.network.clientpackets;

import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.actions.dialogs.ActionDialogType;
import tera.gameserver.model.actions.dialogs.TradeDialog;
import tera.gameserver.model.playable.Player;

/**
 * Запрос на добавление итема в трейд.
 *
 * @author Ronn
 */
public class RequestTradeAddItem extends ClientPacket
{
	/** игрок */
	private Player player;

	/** номер ячейки */
	private int index;
	/** кол-во итемов */
	private int count;

	/** кол-во денег */
	private long money;

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

		readLong();//1 ид
		readLong();//2 ид
		readLong();
		readInt();//

		index = readInt() - 20;
		count = readInt();
		money = readLong();
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

		if(money > 0)
			trade.addMoney(player, money);
		else
			trade.addItem(player, count, index);
	}
}
