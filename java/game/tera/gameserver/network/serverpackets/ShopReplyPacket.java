package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;
import tera.gameserver.templates.ItemTemplate;


/**
 * Серверный пакет с предложением магазина.
 * 
 * @author Ronn
 */
public class ShopReplyPacket extends ServerPacket
{
	private static final ServerPacket instance = new ShopReplyPacket();
	
	public static ShopReplyPacket getInstance(ItemTemplate[][] sections, Player player, int sectionId)
	{
		ShopReplyPacket packet = (ShopReplyPacket) instance.newInstance();
		
		packet.buffer = packet.prepare;
		
		packet.writeInt(0x00200002); //8
		packet.writeInt(player.getObjectId()); //12
		packet.writeInt(player.getSubId()); //16
		packet.writeInt(0x0009AFC2); //20
		packet.writeInt(0x000000D3); //24
		packet.writeLong(0x3FA99999999999AL); //32
		
		int orderSection = 32;
		
		int beginItem = 44;
		
		for(int i = 0, sectionLength = sections.length - 1; i < sections.length; i++)
		{
			packet.writeShort(orderSection);
			
			ItemTemplate[] items = sections[i];
			
			if(items.length < 1)
				break;
			
			beginItem = orderSection + 12;
			
			if(i == sectionLength)
				orderSection = 0;
			else
				orderSection = orderSection + 12 + items.length * 8;
			
			packet.writeShort(orderSection);
			packet.writeShort(items.length);
			
			packet.writeShort(beginItem);
			packet.writeInt(sectionId++); // ид секции
			
			for(int g = 0, length = items.length - 1; g <= length; g++)
			{
				packet.writeShort(beginItem);
				
				beginItem += 8;
				
				if(g == length)
					packet.writeShort(0);
				else
					packet.writeShort(beginItem);
				
				packet.writeInt(items[g].getItemId());
			}
		}
		
		return packet;
	}
	
	/** подготовленный буффер */
	private ByteBuffer prepare;

	public ShopReplyPacket()
	{
		super();
		
		this.prepare = ByteBuffer.allocate(16384).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_DIALOG_REPLY_SHOP;
	}

	@Override
	protected void writeImpl()
	{		
		writeOpcode();
		
		prepare.flip();
		
		buffer.put(prepare);
	}
}
