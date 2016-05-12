package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.Strings;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.playable.NpcAppearance;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет показывает информацию игроку об игрокоподобном НПС.
 *
 * @author Ronn
 */
public class NpcPlayableInfo extends ServerPacket
{
	private static final ServerPacket instance = new NpcPlayableInfo();

	public static NpcPlayableInfo getInstance(Npc npc)
	{
		NpcPlayableInfo packet = (NpcPlayableInfo) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		int n = 194;// 194

		String name = npc.getName();
		String guildName = npc.getFraction();
		String title = Strings.EMPTY;
		String guildTitle = npc.getTitle();
		String iconName = Strings.EMPTY;

		// внешность НПС
		NpcAppearance appearance = npc.getAppearance();

		if(appearance == null)
		{
			log.warning(packet, "not found npc appearance.");
			return packet;
		}

		packet.writeShort(buffer, n); // байт начала ника
		packet.writeShort(buffer, n += Strings.length(name)); // байт начала описания класcа
		packet.writeShort(buffer, n += Strings.length(guildName));// клана
		packet.writeShort(buffer, n += Strings.length(title));// титла

		packet.writeShort(buffer, 32);// кол-во байт описания внешности
		packet.writeShort(buffer, n += 32);// начала описания титулда гильдии
		packet.writeShort(buffer, n += Strings.length(guildTitle));// следом за верхним что-то пока незнаю

		packet.writeInt(buffer, 0);// айди сервера
		packet.writeInt(buffer, npc.getObjectId());
		packet.writeInt(buffer, npc.getObjectId());// обджект ид объекта
		packet.writeInt(buffer, npc.getSubId());
		packet.writeFloat(buffer, npc.getX());// координаты объекта
		packet.writeFloat(buffer, npc.getY());
		packet.writeFloat(buffer, npc.getZ());
		packet.writeShort(buffer, npc.getHeading());

		packet.writeInt(buffer, npc.getNameColor()); // цвет ника
		packet.writeInt(buffer, npc.getModelId());// индетификатор класса и расы объекта
		packet.writeShort(buffer, 0);
		packet.writeShort(buffer, 0x46);
		packet.writeShort(buffer, 0xAA);
		packet.writeShort(buffer, 0); // поза перса
		packet.writeShort(buffer, 0);
		packet.writeByte(buffer, 1);
		packet.writeByte(buffer, npc.isDead()? 0 : 1); // смерть

		// внешность
		packet.writeByte(buffer, 65); // temp[9]
		packet.writeByte(buffer, appearance.getFaceColor());
		packet.writeByte(buffer, appearance.getFaceSkin());
		packet.writeByte(buffer, appearance.getAdormentsSkin());
		packet.writeByte(buffer, appearance.getFeaturesSkin());
		packet.writeByte(buffer, appearance.getFeaturesColor());
		packet.writeByte(buffer, appearance.getVoice());
		packet.writeByte(buffer, 0); // temp[14]

    	packet.writeInt(buffer, appearance.getWeaponId());
    	packet.writeInt(buffer, appearance.getArmorId());
    	packet.writeInt(buffer, appearance.getBootsId());
    	packet.writeInt(buffer, appearance.getGlovesId());
    	packet.writeInt(buffer, appearance.getMaskId());
    	packet.writeInt(buffer, appearance.getHatId());

		packet.writeInt(buffer, npc.isSpawned() ? 1 : 0); // вспышка
		packet.writeInt(buffer, 0); // животное

		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);
		packet.writeLong(buffer, 0);

		packet.writeByte(buffer, 0); // включен ли пвп режим

		packet.writeInt(buffer, npc.getLevel());
		packet.writeLong(buffer, 0);
		packet.writeInt(buffer, 1);
		packet.writeLong(buffer, 0);
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
	private ByteBuffer prepare;

	public NpcPlayableInfo()
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
		return ServerPacketType.NPC_PLAYABLE_INFO;
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