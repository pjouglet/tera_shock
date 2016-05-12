package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

public class RequestNpcInteractionSuccess extends ServerPacket
{
	/** успешное начало диалога */
	public static final byte SUCCEESS = 1;
	/** не удачное начало диалоги */
	public static final byte NOT_SUCCESS = 0;
	
	private static final ServerPacket instance = new RequestNpcInteractionSuccess();
	
	public static RequestNpcInteractionSuccess getInstance(int result)
	{
		RequestNpcInteractionSuccess packet = (RequestNpcInteractionSuccess) instance.newInstance();
		
		packet.result = result;
		
		return packet;
	}
	
	/** разрешен ли диалог */
	private int result;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_REQUEST_INTERACTION_SUCCESS;
	}

	@Override
	protected void writeImpl()
	{
		writeOpcode();
		writeByte(result);
	}
}