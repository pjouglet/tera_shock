package tera.gameserver.network.serverpackets;

import rlib.util.Strings;
import tera.gameserver.model.items.CrystalInstance;
import tera.gameserver.model.items.CrystalList;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с дополнительной информацией об итеме.
 *
 * @author Ronn
 */
public class InventoryItemInfo extends ServerPacket
{
	private static final ServerPacket instance = new InventoryItemInfo();

	public static InventoryItemInfo getInstance(int index, ItemInstance item)
	{
		InventoryItemInfo packet = (InventoryItemInfo) instance.newInstance();

		packet.index = index;
		packet.item = item;

		return packet;
	}

	/** итем */
	private ItemInstance item;

	/** индекс ячейки */
	private int index;

	@Override
	public void finalyze()
	{
		item = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_INVENTORY_ITEM_INFO;
	}

	protected void writeImpl()
	{
		Player player = owner.getOwner();

		boolean hasCrystals = item.hasCrystals();

		writeOpcode();

		if(player == null)
			return;

		int bytes = item.isBinded() ? Strings.length(item.getOwnerName()) + 301 : 299; // (Если забиндено Длинна имена хозяина +2);

		if(!hasCrystals)
			writeInt(0);
		else
		{
			writeShort(4);
			writeShort(bytes);
		}

		if(!item.isBinded())
			writeInt(0);
		else
		{
			writeShort(299); // если есть бинд
			writeShort(299 + 2); // если есть бинд
		}

		writeInt(19); // 14 00 00 00
		writeInt(item.getObjectId()); // BE B4 4F 01
		writeInt(0); // 00 00 00 00
		writeInt(item.getItemId()); // D8 33 00 00
		writeInt(item.getObjectId()); // BE B4 4F 01
		writeInt(0); // 00 00 00 00
		writeInt(player.getObjectId()); // 48 38 00 00
		writeInt(0); // 00 00 00 00
		writeInt(index + 20); // 44 00 00 00
		writeInt(0); // 00 00 00 00
		writeInt(1); // 01 00 00 00
		writeInt(1); // 01 00 00 00
		writeInt(item.getEnchantLevel()); // 00 00 00 00 заточка

		writeInt(0); // 00 00 00 00 00 00 00 00
		writeInt(item.isBinded() ? 1 : 0); //
		/* бонусы от заточки включаеться если заточить на +1 а так нули */
		writeInt(10534660); // 00 00 00 00 флота атаки бонус от заточки
		writeInt(1); // 00 00 00 00 флот овертёрна бонус от заточки
		//+3 bonus
		writeInt(1); // 00 00 00 00 флот защиты бонус от заточки
		//+5 bonus
		writeInt(1); // 00 00 00 00 флот защиты от овертёрна бонус от заточки
		//+7 bonus
		writeByte(1);

		/*
		 * writeIntS(0xACBD0400); //00 00 00 00 writeIntS(0x3CBF0400); //00 00 00 00 writeIntS(0x80BC0400); //00 00 00 00 writeIntS(0x30C10400); //00 00 00 00
		 */
		//+9 bonus
		writeInt(0xACBD0400);
		writeInt(0x3CBF0400);
		writeInt(0x80BC0400);
		writeInt(0x30C10400);

		/* бонусы */
		//writeLong(0x0000000000000000);// 00 00 00 00 00 00 00 00
		//writeLong(0x0000000000000000);// 00 00 00 00 00 00 00 00
		writeLong(0x0000000000000000);// 00 00 00 00 00 00 00 00
		writeLong(0x0000000000000000);// 00 00 00 00 00 00 00 00
		writeLong(0x0000000000000000);// 00 00 00 00 00 00 00 00
		writeLong(0x0000000000000000);// 00 00 00 00 00 00 00 00
		// нужно распиливать на флоты на каждый параметр бонус отдельно

		//Masterwork
		writeInt(item.getMasterworked()); // 00 00 00 00
		writeShort(0); // 00 00


		writeLong(0x0000000000000000);// бонус к следующим заточкам
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);// бонус к следующим заточкам

		writeLong(0x0000000000000000);// видимо какието бафы
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);
		writeLong(0x0000000000000000);// какойто байтовый беспредел
		writeLong(0);// ?? иногда 3

		writeLong(0x0000000000000000);

		writeLong(item.getItemLevel());// Итем лвл

		writeLong(0xFFFFFFFFFFFFFFFFL);// не понятно иногда биндиться суда
		writeInt(0xFFFFFFFE);// не понятно иногда биндиться суда


		if(item.isBinded())
		{
			//1 is item is crafted
			writeShort(0);
			writeString(item.getOwnerName());
		}

		// если в итем вставлены кристаллы
		if(hasCrystals)
		{
			CrystalList crystals = item.getCrystals();

			CrystalInstance[] array = crystals.getArray();

			for(int i = 0, length = crystals.size(); i < length; i++)
			{
				CrystalInstance crystal = array[i];

				writeShort(bytes);

				if(i == length - 1)
					bytes = 0;
				else
					bytes += 8;// нулим если последний

				writeShort(bytes);
				writeInt(crystal.getItemId());// ид кристалла
			}
		}

	}
}
