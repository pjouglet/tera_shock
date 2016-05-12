package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.BankDialog;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.playable.Player;

/**
 * Запрос на получение итема из банка.
 *
 * @author Ronn
 * @created 07.03.2012
 */
public class RequestBankSub extends ClientPacket
{
	/** потвердивший игрок */
	private Player player;

	/** кол-во денег */
	private int gold;

	/** индекс в инвенторе */
	private int index;
	/** ид добавляемого итема */
	private int itemId;
	/** кол-во итемов */
	private int itemCount;

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
		readInt();//00 00 00 00

		gold = readInt();//40 0D 03 00

		readInt();//00 00 00 00

		if(gold > 0)
			return;

		index = readInt();//номер ячейки в банке

		readInt();//обжект ид
		readInt();//

		itemId = readInt();//итем ид
		itemCount = readInt();//кол-во

		readInt();//куда кладём
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

		if(gold > 0)
			bank.getMoney(gold);
		else
			bank.getItem(index, itemId, itemCount);
	}
}
