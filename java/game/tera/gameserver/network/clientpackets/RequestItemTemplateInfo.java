package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.serverpackets.ItemTemplateInfo;
import tera.gameserver.tables.ItemTable;
import tera.gameserver.templates.ItemTemplate;

/**
 * Запрос на отображение информации темплейта итема.
 *
 * @author Ronn
 */
public class RequestItemTemplateInfo extends ClientPacket
{
	/** игрок, запросивший инфу */
	private Player player;

	/** ид запрашиваемого итема */
	private int itemId;

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

		itemId = readInt();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		ItemTemplate template = itemTable.getItem(itemId);

		if(template == null)
			return;

		//player.sendMessage("itemId = " + itemId + ", itemLevel " + template.getItemLevel());

		player.sendPacket(ItemTemplateInfo.getInstance(template.getItemId()), true);
	}
}
