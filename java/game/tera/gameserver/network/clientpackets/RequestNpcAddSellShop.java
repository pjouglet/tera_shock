package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.DialogType;
import tera.gameserver.model.npc.interaction.dialogs.ShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ShopTradePacket;

/**
 * Клиентский пакет с информацией о том, какой итем мы хотим продать
 *
 * @author Ronn
 */
public class RequestNpcAddSellShop extends ClientPacket
{
	/** ид итема который хотим продать */
	private int itemId;
	/** кол-во итемов, который хотим продать */
	private int itemCount;
	/** иденкс ячейки в инвенторе, где итем лежит */
	private int index;

	/** игрок, который хочет продать итем */
	private Player player;

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

		readLong();
		readInt();

		itemId = readInt();
		itemCount = readInt();
		index = readInt() - 20;
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		Dialog dialog = player.getLastDialog();

		if(dialog == null || dialog.getType() != DialogType.SHOP_WINDOW)
			return;

		ShopDialog trade = (ShopDialog) dialog;

		if(trade.addSellItem(itemId, itemCount, index))
			player.sendPacket(ShopTradePacket.getInstance(trade), true);
	}
}
