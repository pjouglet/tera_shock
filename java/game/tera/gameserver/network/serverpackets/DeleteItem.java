package tera.gameserver.network.serverpackets;

import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для удаления итемов.
 * 
 * @author Ronn
 */
public class DeleteItem extends ServerPacket
{
	private static final ServerPacket instance = new DeleteItem();
	
	public static DeleteItem getInstance(ItemInstance item)
	{
		DeleteItem packet = (DeleteItem) instance.newInstance();
		
		packet.objectId = item.getObjectId();
		packet.subId = item.getSubId();
		
		return packet;
	}
	
	/** обджект ид */
	private int objectId;
	/** саб ид */
	private int subId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.ITEM_DELETE;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeInt(objectId);
		writeInt(subId);
	}
}
