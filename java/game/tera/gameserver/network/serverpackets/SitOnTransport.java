package tera.gameserver.network.serverpackets;

import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Класс для посадки игрока на траспорт
 *
 * @author Ronn
 * @created 12.04.2012
 */
public class SitOnTransport extends ServerPacket
{
	private static final ServerPacket instance = new SitOnTransport();
	
	public static SitOnTransport getInstance(Character player, Skill skill)
	{
		SitOnTransport packet = (SitOnTransport) instance.newInstance();
		
		packet.player = player;
		packet.skill = skill;
		
		return packet;
	}
	
	/** игрок, который садится */
	private Character player;
	
	/** скил, который садит */
	private Skill skill;
	
	@Override
	public void finalyze()
	{
		player = null;
		skill = null;
	}

	@Override
	public ServerPacketType getPacketType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void writeImpl()
	{
		writeShort(0x21E8);
		writeInt(player.getObjectId());//обжект айди
		writeInt(player.getSubId());//саб айди
		writeInt(1);
		writeInt(skill.getId() + 67108864);//скил айди
	}
}
