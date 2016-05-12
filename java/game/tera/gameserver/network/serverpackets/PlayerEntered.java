package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;
import tera.gameserver.model.base.Experience;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.SlotType;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.playable.PlayerAppearance;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет с информаций о игроке, который входит в мир.
 * 
 * @author Ronn
 */
public class PlayerEntered extends ServerPacket
{
	private static final ServerPacket instance = new PlayerEntered();

	public static PlayerEntered getInstance(Player player)
	{
		PlayerEntered packet = (PlayerEntered) instance.newInstance();

		Equipment equipment = player.getEquipment();

		PlayerAppearance appearance = player.getAppearance();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			int n = 224; // 220 постоянная

			packet.writeShort(buffer, n);
			packet.writeShort(buffer, n + Strings.length(player.getName()));// длинна имени
			packet.writeShort(buffer, 32);// кол-во байт описания внешности

			packet.writeInt(buffer, player.getTemplateId());// индетификатор класса и расы
			packet.writeInt(buffer, player.getObjectId());// уник ид игрока
			packet.writeInt(buffer, player.getSubId());

			packet.writeInt(buffer, 0);// айди сервера SERVER ID
			packet.writeInt(buffer, player.getObjectId());// 100%
			packet.writeInt(buffer, 0);
			packet.writeByte(buffer, player.isDead() ? 0 : 1);// живой или мёртвый
			packet.writeInt(buffer, 0);
			packet.writeInt(buffer, 70);// ?
			packet.writeInt(buffer, 110);// ?

			packet.writeByte(buffer, 65);// temp[9]
			packet.writeByte(buffer, appearance.getFaceColor());
			packet.writeByte(buffer, appearance.getFaceSkin());
			packet.writeByte(buffer, appearance.getAdormentsSkin());
			packet.writeByte(buffer, appearance.getFeaturesSkin());
			packet.writeByte(buffer, appearance.getFeaturesColor());
			packet.writeByte(buffer, appearance.getVoice());
			packet.writeByte(buffer, 0); // temp[14]
			packet.writeByte(buffer, 1);
			packet.writeByte(buffer, 0);
			packet.writeShort(buffer, player.getLevel());// уровень игрока

			packet.writeShort(buffer, 0);
			packet.writeShort(buffer, 0);
			packet.writeShort(buffer, 0);
			packet.writeShort(buffer, 0);

			packet.writeInt(buffer, 1);
			packet.writeShort(buffer, 0);

			packet.writeInt(buffer, 0);

			packet.writeLong(buffer, player.getExp()); // опыт игрока
			packet.writeLong(buffer, player.getExp());
			packet.writeLong(buffer, Experience.LEVEL[player.getLevel() + 1]);// кол-во опыто необходимого для следующего лвла

			packet.writeLong(buffer, 0x0000000000000000);
			packet.writeInt(buffer, 0x000001A3);
			packet.writeLong(buffer, 0x0000000000000000);

			ItemInstance weapon = equipment.getItem(SlotType.SLOT_WEAPON);

			equipment.lock();
			try
			{
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

			packet.writeInt(buffer, 0);// бан чата // player.getObjectId());
			packet.writeInt(buffer, 0);

			packet.writeLong(buffer, 1);
			packet.writeByte(buffer, 0);
			packet.writeInt(buffer, 0);// 03 00 00 00 если 3, то надпись "Ангел смерти"
			packet.writeInt(buffer, 0);// 00 00 00 00

			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, weapon == null ? 0 : weapon.getEnchantLevel());// точка

			packet.writeByte(buffer, 0);// 00

			packet.writeInt(buffer, player.getKarma());// 78 00 00 00 .//карма
			packet.writeInt(buffer, 1);// 01 00 00 00

			packet.writeInt(buffer, 0);// 00 00 00 00//00 00 00 00

			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00

			packet.writeByte(buffer, 0);// 00

			packet.writeString(buffer, player.getName());

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

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	/** промежуточный буффер */
	private final ByteBuffer prepare;

	public PlayerEntered()
	{
		this.prepare = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_ENTERED;
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