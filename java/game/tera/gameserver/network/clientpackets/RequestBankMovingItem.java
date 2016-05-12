package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.BankDialog;
import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, указывающий какой итем хотим переместить в инвенторе
 *
 * @author Ronn
 */
public class RequestBankMovingItem extends ClientPacket
{
	/** индекс старой ячейки */
	private int oldСell;
	/** индекс новой ячейки */
	private int newСell;

	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	public void readImpl()
	{
		player = owner.getOwner();

		readLong();
		readLong();

		readInt();

		oldСell = readInt();
		newСell = readInt();
	}

	@Override
	public void runImpl()
	{
		if(player == null)
			return;

		// получаем диалог банка
		Dialog dialog = player.getLastDialog();

		// если его нет, выходим
		if(dialog == null || !(dialog instanceof BankDialog))
			return;

		BankDialog bank = (BankDialog) dialog;

		bank.movingItem(oldСell, newСell);
	}
}