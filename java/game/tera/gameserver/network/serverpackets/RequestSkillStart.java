package tera.gameserver.network.serverpackets;

import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с запросом на старт скила клиентом.
 *
 * @author Ronn
 * @created 31.03.2012
 */
public class RequestSkillStart extends ServerPacket
{
	private static final ServerPacket instance = new RequestSkillStart();

	public static RequestSkillStart getInstance(int skillId)
	{
		RequestSkillStart packet = (RequestSkillStart) instance.newInstance();

		packet.skillId = skillId;

		return packet;
	}

	/** ид скила */
	private int skillId;

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.CHARGE_SKILL_START;
	}

	@Override
	protected final void writeImpl()
	{
		writeOpcode();
		writeInt(skillId);//суда скид ид новый
	}
}