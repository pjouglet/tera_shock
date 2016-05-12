package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Обновление стамины у игрока.
 *
 * @author Ronn
 */
public class UpdateStamina extends ServerPacket
{
	private static final ServerPacket instance = new UpdateStamina();

	public static UpdateStamina getInstance(Player player)
	{
		UpdateStamina packet = (UpdateStamina) instance.newInstance();

		packet.currentHeart = player.getStamina();
		packet.maxHeart = player.getMaxStamina();

		return packet;
	}

	/** текущее кол-во стамины */
	private int currentHeart;
	/** максимальное кол-во стамины */
	private int maxHeart;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.UPDATE_STAMINA;
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
		writeInt(buffer, currentHeart);//Текущая стамина 71 00 00 00
		writeInt(buffer, maxHeart);//Размер стамины 78 00 00 00
		writeShort(buffer, 1);//03 00 на сколько повысилась
	}
}

