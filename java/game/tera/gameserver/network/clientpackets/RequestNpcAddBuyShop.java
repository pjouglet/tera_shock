package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.DialogType;
import tera.gameserver.model.npc.interaction.dialogs.ShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ShopTradePacket;

/**
 * Запрос на добавление в покупаемые итемы итем.
 *
 * @author Ronn
 */
public class RequestNpcAddBuyShop extends ClientPacket
{
	/** ид итема который хотим купить */
	private int itemId;
	/** кол-во покупаемого итема */
	private int itemCount;

	/** игрок */
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

		if(trade.addBuyItem(itemId, itemCount))
			player.sendPacket(ShopTradePacket.getInstance(trade), true);
	}
}
