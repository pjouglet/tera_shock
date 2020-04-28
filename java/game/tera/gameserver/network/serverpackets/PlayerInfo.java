package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.SlotType;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.playable.PlayerAppearance;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет показывает информацию игроку об другом игроке
 * 
 * @author Ronn
 */
public class PlayerInfo extends ServerPacket
{
	private static final ServerPacket instance = new PlayerInfo();

	public static PlayerInfo getInstance(Player newPlayer, Player player)
	{
		PlayerInfo packet = (PlayerInfo) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		int n = 194;// 194

		String name = newPlayer.getName();
		String guildName = newPlayer.getGuildName();
		String title = newPlayer.getTitle();
		String guildTitle = newPlayer.getGuildTitle();
		String iconName = newPlayer.getGuildIconName();

		// внешность игрока
		PlayerAppearance appearance = newPlayer.getAppearance();

		// экиперовка игрока
		Equipment equipment = newPlayer.getEquipment();

		packet.writeShort(buffer, n); // байт начала ника
		packet.writeShort(buffer, n += Strings.length(name)); // байт начала описания класcа
		packet.writeShort(buffer, n += Strings.length(guildName));// клана
		packet.writeShort(buffer, n += Strings.length(title));// титла

		packet.writeShort(buffer, 32);// number of bytes to describe appearence
		packet.writeShort(buffer, n += 32);// Guild title description
		packet.writeShort(buffer, n += Strings.length(guildTitle));// следом за верхним что-то пока незнаю

		packet.writeInt(buffer, 0);// айди сервера
		packet.writeInt(buffer, newPlayer.getObjectId());
		packet.writeInt(buffer, newPlayer.getObjectId());// обджект ид объекта
		packet.writeInt(buffer, newPlayer.getSubId());
		packet.writeFloat(buffer, newPlayer.getX());// координаты объекта
		packet.writeFloat(buffer, newPlayer.getY());
		packet.writeFloat(buffer, newPlayer.getZ());
		packet.writeShort(buffer, newPlayer.getHeading());

		packet.writeInt(buffer, player.getColor(newPlayer)); // цвет ника
		packet.writeInt(buffer, newPlayer.getTemplateId());// индетификатор класса и расы объекта
		packet.writeShort(buffer, 0);
		packet.writeShort(buffer, 0x46);
		packet.writeShort(buffer, 0xAA);
		packet.writeShort(buffer, 0); // поза перса
		packet.writeShort(buffer, 0);
		packet.writeByte(buffer, 1);
		packet.writeByte(buffer, newPlayer.isDead() ? 0 : 1); // смерть

		// appearance
		packet.writeByte(buffer, 65); // temp[9]
		packet.writeByte(buffer, appearance.getFaceColor());
		packet.writeByte(buffer, appearance.getFaceSkin());
		packet.writeByte(buffer, appearance.getAdormentsSkin());
		packet.writeByte(buffer, appearance.getFeaturesSkin());
		packet.writeByte(buffer, appearance.getFeaturesColor());
		packet.writeByte(buffer, appearance.getVoice());
		packet.writeByte(buffer, 0); // temp[14]

		ItemInstance weapon = equipment.getItem(SlotType.SLOT_WEAPON);

		equipment.lock();
		try
		{
			// экиперованные итемы
			packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_WEAPON));
			packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_ARMOR));
			packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_BOOTS));
			packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_GLOVES));
			packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_MASK));
			packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_HAT));
		}
		finally
		{
			equipment.unlock();
		}

		packet.writeInt(buffer, newPlayer.isSpawned() ? 1 : 0); // вспышка
		packet.writeInt(buffer, newPlayer.getMountId()); // животное

		packet.writeInt(buffer, 0);//pose see C_PLAYER_LOCATION
		packet.writeInt(buffer, 0);//title
		packet.writeLong(buffer, 0);//shuttleID
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);

		packet.writeShort(buffer, 0);
		packet.writeByte(buffer, 0);

		packet.writeInt(buffer, weapon == null ? 0 : weapon.getEnchantLevel()); // Weapon enchant

		packet.writeByte(buffer, 1);//newbie
		packet.writeByte(buffer, newPlayer.isPvPMode() ? 1 : 0); // включен ли пвп режим

		packet.writeInt(buffer, newPlayer.getLevel());
		packet.writeInt(buffer, 0);

		packet.writeInt(buffer, 0);// что-то тоже нательное

		packet.writeInt(buffer, 1);

		packet.writeInt(buffer, 0);// лифчики
		packet.writeInt(buffer, 0);

		packet.writeInt(buffer, 0);

		packet.writeByte(buffer, 0);

		packet.writeString(buffer, name);// имя
		packet.writeString(buffer, guildName);// название клана
		packet.writeString(buffer, title); // титул

		packet.writeByte(buffer, appearance.getBoneStructureBrow());
		packet.writeByte(buffer, appearance.getBoneStructureCheekbones());
		packet.writeByte(buffer, appearance.getBoneStructureJaw());
		packet.writeByte(buffer, appearance.getBoneStructureJawJut());
		packet.writeByte(buffer, appearance.getEarsRotation());
		packet.writeByte(buffer, appearance.getEarsExtension());
		packet.writeByte(buffer, appearance.getEarsTrim());
		packet.writeByte(buffer, appearance.getEarsSize());
		packet.writeByte(buffer, appearance.getEyesWidth());
		packet.writeByte(buffer, appearance.getEyesHeight());
		packet.writeByte(buffer, appearance.getEyesSeparation());
		packet.writeByte(buffer, 0); // temp[17]
		packet.writeByte(buffer, appearance.getEyesAngle());
		packet.writeByte(buffer, appearance.getEyesInnerBrow());
		packet.writeByte(buffer, appearance.getEyesOuterBrow());
		packet.writeByte(buffer, 0); // temp[18]
		packet.writeByte(buffer, appearance.getNoseExtension());
		packet.writeByte(buffer, appearance.getNoseSize());
		packet.writeByte(buffer, appearance.getNoseBridge());
		packet.writeByte(buffer, appearance.getNoseNostrilWidth());
		packet.writeByte(buffer, appearance.getNoseTipWidth());
		packet.writeByte(buffer, appearance.getNoseTip());
		packet.writeByte(buffer, appearance.getNoseNostrilFlare());
		packet.writeByte(buffer, appearance.getMouthPucker());
		packet.writeByte(buffer, appearance.getMouthPosition());
		packet.writeByte(buffer, appearance.getMouthWidth());
		packet.writeByte(buffer, appearance.getMouthLipThickness());
		packet.writeByte(buffer, appearance.getMouthCorners());
		packet.writeByte(buffer, appearance.getEyesShape());
		packet.writeByte(buffer, appearance.getNoseBend());
		packet.writeByte(buffer, appearance.getBoneStructureJawWidth());
		packet.writeByte(buffer, appearance.getMothGape());

		packet.writeString(buffer, guildTitle);
		packet.writeString(buffer, iconName);

		buffer.flip();

		return packet;
	}

	/** подготавливаемый буффер */
	private final ByteBuffer prepare;

	public PlayerInfo()
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
		return ServerPacketType.PLAYER_INFO;
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
	 * @return подготавливаемый буффер.
	 */
	public ByteBuffer getPrepare()
	{
		return prepare;
	}
}