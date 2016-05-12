package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.equipment.SlotType;
import tera.gameserver.model.playable.PlayerAppearance;
import tera.gameserver.model.playable.PlayerPreview;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет со списком игроков на аккаунте
 *
 * @author Ronn
 */
public class PlayerList extends ServerPacket
{
	private static final ServerPacket instance = new PlayerList();

	public static PlayerList getInstance(String accountName)
	{
		PlayerList packet = (PlayerList) instance.newInstance();

		// получаем подготовительный буффер
		ByteBuffer buffer = packet.getPrepare();

		// получаем менеджера БД
		DataBaseManager dbManager = DataBaseManager.getInstance();

		// получаем список персонажей на аккаунте
		Array<PlayerPreview> playerList = packet.getPlayerList();

		// загружаем персонажей
		dbManager.restorePlayerList(playerList, accountName);

		int size = playerList.size();
		int sizeTo = 0;

		packet.writeShort(buffer, size); // кол-во персонажей

		if(size > 0)
		{
			packet.writeShort(buffer, 35);
			packet.writeLong(buffer, 1);
			packet.writeByte(buffer, 0);
			packet.writeInt(buffer, 8);
			packet.writeInt(buffer, 1);
			packet.writeShort(buffer, 0);
			packet.writeInt(buffer, 5);
			packet.writeInt(buffer, 168);

			// получаем массив превью
			PlayerPreview[] array = playerList.array();

			// получаем последнее превью
			PlayerPreview last = playerList.last();

			for(int i = 0; i < size; i++)
			{
				sizeTo = buffer.position() + 4; // reading amount of already written bytes...

				packet.writeShort(buffer, sizeTo); // amount of bytes which was written before this byte..

				PlayerPreview current = array[i];

				if(current == last) // last character sends 0x00
					packet.writeShort(buffer, 0);
				else
					packet.writeShort(buffer, sizeTo + 283 + Strings.length(current.getName())); //

				packet.writeShort(buffer, sizeTo += 251); // amount of bytes before char name starts...
				packet.writeShort(buffer, sizeTo + Strings.length(current.getName())); //
				packet.writeShort(buffer, 32); //

				// ------------------------------------------------------------------

				packet.writeInt(buffer, current.getObjectId());
				packet.writeInt(buffer, current.getSex());
				packet.writeInt(buffer, current.getRaceId());
				packet.writeInt(buffer, current.getClassId());
				packet.writeInt(buffer, current.getLevel());

				packet.writeInt(buffer, 0x000186A0); // AD040000
				packet.writeInt(buffer, 0x000186A0); // AD040000
				packet.writeInt(buffer, 0);// 1
				packet.writeInt(buffer, 0);// 2
				packet.writeInt(buffer, 0);// 7

				packet.writeInt(buffer, 0x00007E8F);
				packet.writeInt(buffer, 0);
				packet.writeByte(buffer, 0);

				packet.writeInt(buffer, 0xB060614E);
				packet.writeInt(buffer, 0);
				packet.writeInt(buffer, 0xB060614E);

				// =========================== Экиперовка ================================//

				// получаем экиперовку игрока
				Equipment equipment = current.getEquipment();

				packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_WEAPON));
				packet.writeInt(buffer, 0);
				packet.writeInt(buffer, 0);
				packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_ARMOR));
				packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_GLOVES));
				packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_BOOTS));
				packet.writeInt(buffer, 0);
				packet.writeInt(buffer, 0);
				packet.writeInt(buffer, 0);
				packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_HAT));
				packet.writeInt(buffer, equipment.getItemId(SlotType.SLOT_MASK));

				// ============================================================\\

				// получаем внешность игрока
				PlayerAppearance appearance = current.getAppearance();

				packet.writeByte(buffer, 65); // temp[9]
				packet.writeByte(buffer, appearance.getFaceColor());
				packet.writeByte(buffer, appearance.getFaceSkin());
				packet.writeByte(buffer, appearance.getAdormentsSkin());
				packet.writeByte(buffer, appearance.getFeaturesSkin());
				packet.writeByte(buffer, appearance.getFeaturesColor());
				packet.writeByte(buffer, appearance.getVoice());
				packet.writeByte(buffer, 0); // temp[14]

				// ============================================================\\

				packet.writeLong(buffer, 0);
				packet.writeInt(buffer, 0);
				packet.writeShort(buffer, 0);

				packet.writeInt(buffer, 0xB05718BF); // BF1857B0
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeLong(buffer, 0);
				packet.writeInt(buffer, 0x000001A3);
				packet.writeInt(buffer, 0x000001A3);
				packet.writeByte(buffer, 1);

				if(current.getOnlineTime() > 15000)
					packet.writeByte(buffer, 0);// 01 показываем промо 00 нет
				else
					packet.writeByte(buffer, 1);

				packet.writeByte(buffer, 0x00);
				packet.writeShort(buffer, 0);

				// ============================================================\\
				packet.writeByte(buffer, 0);

				packet.writeString(buffer, current.getName());

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
			}

			// скадируем превьюхи
			for(int i = 0; i < size; i++)
				array[i].fold();
		}
		else
		{
			// если персов нету шлём это
			packet.writeInt(buffer, 0);
			packet.writeInt(buffer, 0);
			packet.writeInt(buffer, 0);
			packet.writeInt(buffer, 1);
			packet.writeShort(buffer, 0);
		}

		return packet;
	}

	/** список игроков на аккаунте */
	private final Array<PlayerPreview> playerList;

	/** промежуточный буффер пакета */
	private final ByteBuffer prepare;

	public PlayerList()
	{
		this.playerList = Arrays.toArray(PlayerPreview.class);
		this.prepare = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		playerList.clear();

		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_LIST;
	}

	/**
	 * @return список игроков.
	 */
	public Array<PlayerPreview> getPlayerList()
	{
		return playerList;
	}

	/**
	 * @return подготовительный буффер.
	 */
	public ByteBuffer getPrepare()
	{
		return prepare;
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

		prepare.flip();

		buffer.put(prepare);
	}
}