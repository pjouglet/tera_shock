package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.TeleportRegion;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.territory.LocalTerritory;
import tera.gameserver.network.ServerPacketType;
import tera.util.Location;

/**
 * Серверный пакет с доступными точками для телепорта.
 *
 * @author Ronn
 * @created 26.02.2012
 */
public class TeleportPoints extends ServerPacket
{
	private static final ServerPacket instance = new TeleportPoints();

	public static TeleportPoints getInstance(Npc npc, Player player, TeleportRegion[] regions)
	{
		TeleportPoints packet = (TeleportPoints) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			packet.writeShort(buffer, regions.length);// 02 00 кол-во точек телепорта

			int bytes = 24;

			packet.writeShort(buffer, bytes);// 18 00

			packet.writeFloat(buffer, npc.getX());// 00 29 83 47
			packet.writeFloat(buffer, npc.getY());// 80 33 9B C7
			packet.writeFloat(buffer, npc.getZ());// 00 B0 39 C5

			packet.writeInt(buffer, 1);// 01 00 00 00

			for(int i = 0, length = regions.length; i < length; i++)
			{
				TeleportRegion region = regions[i];

				LocalTerritory local = region.getRegion();

				Location loc = local.getTeleportLoc();

				packet.writeShort(buffer, bytes);// 18 00

				if(i == length -1)
					bytes = 0;
				else
					bytes += 29;

				packet.writeShort(buffer, bytes);// 35 00

				packet.writeInt(buffer, region.getIndex());// индекс маршрута region.getIndex()

				packet.writeFloat(buffer, loc.getX());// 00 29 83 47
				packet.writeFloat(buffer, loc.getY());// 80 33 9B C7
				packet.writeFloat(buffer, loc.getZ());// 00 B0 39 C5

				packet.writeInt(buffer, region.getPrice());// цена
				packet.writeInt(buffer, 0);//

				packet.writeByte(buffer, player.isWhetherIn(local)? 1 : 0); // можно ли использовать
			}

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	private ByteBuffer prepare;

	public TeleportPoints()
	{
		this.prepare = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.TELEPORT_POINTS;
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

		// получаем промежуточный буффер
		ByteBuffer prepare = getPrepare();

		// переносим данные
		buffer.put(prepare.array(), 0, prepare.limit());
	}

	/**
	 * @return подготовленный буфер.
	 */
	public ByteBuffer getPrepare()
	{
		return prepare;
	}
}
