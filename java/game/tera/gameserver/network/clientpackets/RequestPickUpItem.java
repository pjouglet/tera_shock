package tera.gameserver.network.clientpackets;

import tera.gameserver.model.World;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет, запрашивающий поднятие итема
 *
 * @author Ronn
 */
public class RequestPickUpItem extends ClientPacket
{
	/** обджект ид итема */
	private int itemId;
	/** саб ид итема */
	private int itemSubId;

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

		// ид вещи которую хтим поднять с земли
		itemId = readInt();
		itemSubId = readInt();
	}

	@Override
	public void runImpl()
	{
		if(player == null)
			return;

		ItemInstance item = World.getAroundById(ItemInstance.class, player, itemId, itemSubId);

		if(item == null || item.isInvisible())
			return;

		player.getAI().startItemPickUp(item);
	}
}