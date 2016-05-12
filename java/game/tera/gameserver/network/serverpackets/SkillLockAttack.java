package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Пакет со списком атакуемых целей лок он скилом.
 *
 * @author Ronn
 */
public class SkillLockAttack extends ServerPacket
{
	private static final ServerPacket instance = new SkillLockAttack();

	public static SkillLockAttack getInstance(Character caster, Array<Character> targets, Skill skill, int castId)
	{
		SkillLockAttack packet = (SkillLockAttack) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			int n = 28;

			packet.writeShort(buffer, targets.size()); //кол-во захваченных
			packet.writeShort(buffer, n);
			packet.writeInt(buffer, caster.getObjectId()); //12 0B 0D 00 00 80 00 01    X.A?.........?..
			packet.writeInt(buffer, caster.getSubId()); //0A 29 00 00
			packet.writeInt(buffer, caster.getModelId());//0A 29 00 00
			packet.writeInt(buffer, skill.getIconId());//F2 4E 00 04
			packet.writeInt(buffer, castId);//9F FC 2A 07

			targets.readLock();
			try
			{
				Character[] array = targets.array();

				for(int i = 0, length = targets.size(); i < length; i++)
				{
					Character target = array[i];

					packet.writeShort(buffer, n);

					if(i == length - 1)
						n = 0;
					else
						n += 12;

					packet.writeShort(buffer, n);
					packet.writeInt(buffer, target.getObjectId());
					packet.writeInt(buffer, target.getSubId());
				}
			}
			finally
			{
				targets.readUnlock();
			}

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	/** подготовленный буффер */
	private ByteBuffer prepare;

	public SkillLockAttack()
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
		return ServerPacketType.SKILL_LOCK_ATTACK;
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
