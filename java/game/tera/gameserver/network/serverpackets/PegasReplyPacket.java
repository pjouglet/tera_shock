package tera.gameserver.network.serverpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет для открытия карты, в которой нужно выбирать пункт назначения для полета
 *
 * @author Ronn
 * @created 25.02.2012
 */
public class PegasReplyPacket extends ServerPacket
{
	private static final ServerPacket instance = new PegasReplyPacket();
	
	public static PegasReplyPacket getInstance(Player player)
	{
		PegasReplyPacket packet = (PegasReplyPacket) instance.newInstance();
		
		packet.player = player;
		
		return packet;
	}
	
	/** игрок */
	private Player player;

	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.FLY_PEGAS_REPLY_PACKET;
	}

	@Override
	protected void writeImpl()
	{
		//writeOpcode();
		writeShort(ServerPacketType.PLAYER_WAITING_ACTION.getOpcode());
		writeShort(0x2c);
		writeShort(0x3e);	
		writeInt(0x40);
		writeInt(player.getObjectId());
		writeInt(player.getSubId());
		writeLong(0);//00 00 00 00  00 00 00 00
		writeInt(0x0F);
		writeInt(0x989b0300);
		writeLong(0);//00 00 00 00  00 00 00 00
		writeS(player.getName());
		writeShort(0);
		
	}
}
