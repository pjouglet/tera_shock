package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

public class Test35 extends ServerPacket
{
	private static final ServerPacket instance = new Test35();

	public static Test35 getInstance(Player player)
	{
		Test35 packet = (Test35) instance.newInstance();

		packet.objectId = player.getObjectId();

		return packet;
	}

	private int objectId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TEST_35;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);
		writeInt(buffer, objectId);
		writeInt(buffer, 0x13008000);
		writeInt(buffer, 0x0000C47C);
		writeInt(buffer, 0xFFFFFF01);
		writeByte(buffer, 0x7F);
	}
}