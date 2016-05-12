package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.interaction.dialogs.Dialog;
import tera.gameserver.model.npc.interaction.dialogs.DialogType;
import tera.gameserver.model.npc.interaction.dialogs.ShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ShopTradePacket;

/**
 * Клиентский пакет указывающий на удаляемый продаваемый итем
 *
 * @author Ronn
 * @created 25.02.2012
 */
public class RequestNpcSubSellShop extends ClientPacket
{
	/** игрок */
	private Player player;

	/** обджект ид итема */
	private int objectId;
	/** ид итема */
	private int itemId;
	/** кол-во итемов */
	private int count;

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

		readLong();  //наш обжест ид с саб идом
		readInt(); //обжект ид вещи
		itemId = readInt(); //итем ид вещи
		count = readInt(); //кол-во вещей
		objectId = readInt(); //обжект ид вещи
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

		if(trade.subSellItem(itemId, count, objectId))
			player.sendPacket(ShopTradePacket.getInstance(trade), true);
	}
}
