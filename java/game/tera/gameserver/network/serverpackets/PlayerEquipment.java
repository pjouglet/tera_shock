package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;

import tera.gameserver.model.Character;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.SlotType;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, описывающий экиперовку игрока.
 * 
 * @author Ronn
 */
public class PlayerEquipment extends ServerPacket
{
	private static final ServerPacket instance = new PlayerEquipment();

	public static PlayerEquipment getInstance(Character owner)
	{
		PlayerEquipment packet = (PlayerEquipment) instance.newInstance();

		packet.objectId = owner.getObjectId();
		packet.subId = owner.getSubId();

		Equipment equipment = owner.getEquipment();

		equipment.lock();
		try
		{
			ItemInstance item = equipment.getItem(SlotType.SLOT_WEAPON);

			packet.weaponId = item == null ? 0 : item.getItemId();
			packet.enchantLevel = item == null ? 0 : item.getEnchantLevel();

			item = equipment.getItem(SlotType.SLOT_ARMOR);
			packet.armorId = item == null ? 0 : item.getItemId();
			item = equipment.getItem(SlotType.SLOT_BOOTS);
			packet.bootsId = item == null ? 0 : item.getItemId();
			item = equipment.getItem(SlotType.SLOT_GLOVES);
			packet.glovesId = item == null ? 0 : item.getItemId();
			item = equipment.getItem(SlotType.SLOT_HAT);
			packet.hatId = item == null ? 0 : item.getItemId();
			item = equipment.getItem(SlotType.SLOT_MASK);
			packet.maskId = item == null ? 0 : item.getItemId();
		}
		finally
		{
			equipment.unlock();
		}

		return packet;
	}

	/** обджект ид персонажа */
	private int objectId;
	/** саб ид персонажа */
	private int subId;
	/** ид оружия */
	private int weaponId;
	/** ид армора */
	private int armorId;
	/** ид ботинок */
	private int bootsId;
	/** ид перчей */
	private int glovesId;
	/** ид шапки */
	private int hatId;
	/** ид маски */
	private int maskId;
	/** уровень заточки */
	private int enchantLevel;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_EQUIPMENT;
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
		writeInt(buffer, subId);
		writeInt(buffer, weaponId);
		writeInt(buffer, armorId);
		writeInt(buffer, bootsId);
		writeInt(buffer, glovesId);
		writeInt(buffer, hatId);
		writeInt(buffer, maskId);
		writeInt(buffer, 0);

		writeInt(buffer, 0);// лифчик
		writeInt(buffer, 0);

		writeInt(buffer, 0);

		writeInt(buffer, 0);
		writeInt(buffer, 0);
		writeInt(buffer, 0);
		writeInt(buffer, 0);
		writeInt(buffer, enchantLevel);// точка ствола
		writeInt(buffer, 0);
		writeInt(buffer, 0);
		writeInt(buffer, 0);
	}
}
