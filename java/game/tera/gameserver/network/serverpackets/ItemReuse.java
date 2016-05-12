package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с откатами итемов.
 * 
 * @author Ronn
 */
public class ItemReuse extends ServerPacket
{
	private static final ServerPacket instance = new ItemReuse();
	
	public static ItemReuse getInstance(int itemId, int reuse)
	{
		ItemReuse packet = (ItemReuse) instance.newInstance();
		
		packet.itemId = itemId;
		packet.reuse = reuse;
		
		return packet;
	}
	
	/** ид итема */
	private int itemId;
	/** откат */
	private int reuse;
	
	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ITEM_REUSE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(itemId);//47 1F 00 00 Итем ид который откатывается
		writeInt(reuse);//1E 00 00 00 кол-во секунд на откат
	}
}
