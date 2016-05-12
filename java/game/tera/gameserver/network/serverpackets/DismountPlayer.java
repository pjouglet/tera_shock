package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет для слезания с транспортной трансформы
 *
 * @author Ronn
 * @created 12.04.2012
 */
public class DismountPlayer extends ServerPacket
{
	private static final ServerPacket instance = new DismountPlayer();
	
	public static DismountPlayer getInstance(Character character, Skill skill)
	{
		DismountPlayer packet = (DismountPlayer) instance.newInstance();
		
		packet.character = character;
		packet.skill = skill;
		
		return packet;
	}
	
	/** слезаемый персонаж */
	private Character character;
	/** скил */
	private Skill skill;
	
	@Override
	public void finalyze()
	{
		character = null;
		skill = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return null;
	}

	@Override
	protected void writeImpl()
	{
		writeInt(character.getObjectId());//обжект айди
		writeInt(character.getSubId());//саб айди
		writeInt(skill.getId() + 67108864);//скил айди
	}
}
