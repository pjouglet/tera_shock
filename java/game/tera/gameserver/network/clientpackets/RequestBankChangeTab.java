package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.BankDialog;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.playable.Player;

/**
 * Запрос на обновление вкладки в банке.
 *
 * @author Ronn
 * @created 07.03.2012
 */
public class RequestBankChangeTab extends ClientPacket
{
	/** потвердивший игрок */
	private Player player;

	/** кол-во денег */
	private int startCell;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readInt();//01 00 00 10
		readInt();//E8 03 00 00
		readInt();//01 00 00 00

		startCell = readInt();//00 00 00 00
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Dialog dialog = player.getLastDialog();

		if(dialog == null || !(dialog instanceof BankDialog))
			return;

		BankDialog bank = (BankDialog) dialog;

		bank.setStartCell(startCell);
	}
}
