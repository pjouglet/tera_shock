package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.actions.dialogs.EnchantItemDialog;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет с полной информацией об диалоге зачоравания вещи.
 * 
 * @author Ronn
 */
public class EnchatItemInfo extends ServerPacket
{
	private static final ServerPacket instance = new EnchatItemInfo();

	public static EnchatItemInfo getInstance(EnchantItemDialog dialog)
	{
		EnchatItemInfo packet = (EnchatItemInfo) instance.newInstance();

		ByteBuffer buffer = packet.prepare;

		int n = 8;

		packet.writeShort(buffer, 3);// 03 00
		packet.writeShort(buffer, n);// 08 00

		for (int i = 0, length = EnchantItemDialog.ITEM_COUNTER; i <= length; i++)
		{
			packet.writeShort(buffer, n);// 08 00

			if (i != length)
				packet.writeShort(buffer, n += 126);
			else
				packet.writeShort(buffer, 0);

			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, i);// //номер ячейки с нуля 0,1,2
			packet.writeInt(buffer, dialog.getItemId(i));
			packet.writeLong(buffer, dialog.getObjectId(i));
			packet.writeLong(buffer, 14408);// 48 38 00 00 00 00 00 00
			packet.writeLong(buffer, 76);// 4C 00 00 00 00 00 00 00
			packet.writeInt(buffer, dialog.getNeedItemCount(i));
			packet.writeInt(buffer, dialog.getNeedItemCount(i));
			packet.writeInt(buffer, dialog.getEnchantLevel(i));// 02 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, dialog.isEnchantItem(i) ? 1 : 0);
			packet.writeInt(buffer, 0);// 00 80 BC 04 иды бонусов
			packet.writeInt(buffer, 0);// 00 B8 BB 04 иды бонусов
			packet.writeInt(buffer, 0);// 00 D8 BE 04 иды бонусов
			packet.writeInt(buffer, 0);// 00 10 BE 04 иды бонусов
			packet.writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
			packet.writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
			packet.writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
			packet.writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
			packet.writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
			packet.writeLong(buffer, 0);// 00 00 00 00 00 00 00 00
			packet.writeShort(buffer, 0);// 00 00
		}

		buffer.flip();

		return packet;
	}

	/** пати */
	private final ByteBuffer prepare;

	public EnchatItemInfo()
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
		return ServerPacketType.ENCHANT_ITEM_DIALOG_INFO;
	}

	@Override
	public boolean isSynchronized()
	{
		return false;
	}

	/**
	 * @return подготавливаемый буффер.
	 */
	private ByteBuffer getPrepare()
	{
		return prepare;
	}

	@Override
	protected void writeImpl(ByteBuffer buffer)
	{
		writeOpcode(buffer);

		ByteBuffer prepare = getPrepare();
		buffer.put(prepare.array(), 0, prepare.limit());
	}
}
