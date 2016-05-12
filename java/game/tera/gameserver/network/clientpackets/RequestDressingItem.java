package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;

/**
 * Запрос на одевание предмета.
 *
 * @author Ronn
 */
public class RequestDressingItem extends ClientPacket
{
	/** номер слота */
	private int slot;
	/** ид итема снимаемого */
	private int itemId;

	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		itemId = 0;
		slot = 0;
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

		readInt();
		readInt();

		//номер слота
		slot = readInt();

		if(slot < 20 && buffer.remaining() > 7)
		{
			readInt();
			itemId = readInt();
		}
	}

	@Override
	public void runImpl()
	{
		if(player != null)
			player.getAI().startDressItem(slot, itemId);
	}
}
