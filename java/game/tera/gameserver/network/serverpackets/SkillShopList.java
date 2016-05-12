package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.array.Array;
import rlib.util.table.IntKey;
import rlib.util.table.Table;

import tera.gameserver.model.SkillLearn;
import tera.gameserver.model.npc.interaction.dialogs.SkillShopDialog;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет, описывающий список изучаемых скилов.
 *
 * @author Ronn
 * @created 25.02.2012
 */
public class SkillShopList extends ServerPacket
{
	private static final ServerPacket instance = new SkillShopList();

	public static SkillShopList getInstance(Array<SkillLearn> skills, Player player)
	{
		SkillShopList packet = (SkillShopList) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			// получаем последний изучаемый скил
			SkillLearn last = skills.last();

			packet.writeInt(buffer, 524422);

			// если изучаемых скилов нет, выходим
			if(player == null || skills.isEmpty() || last == null)
				return packet;

			int beginByte = 8;

			// получаем массив изучаемыхскилов
			SkillLearn[] array = skills.array();

			// получаем таблицу изученных скилов
			Table<IntKey, Skill> currentSkills = player.getSkills();

			// перебираем изучаемые скилы
			for(int i = 0, length = skills.size() - 1; i <= length; i++)
			{
				// получаем изучаемый скил
				SkillLearn skill = array[i];

				// если его нет, пропускаем
				if(skill == null)
					continue;

				packet.writeShort(buffer, beginByte);

				if(skill == last)
					beginByte = 0;
				else
					beginByte += skill.getReplaceId() == 0? 26 : 35;

				packet.writeShort(buffer, beginByte);// если последний нуллим

				if(skill.getReplaceId() == 0)
					packet.writeInt(buffer, 0);
				else
				{
					packet.writeShort(buffer, 1);
					packet.writeShort(buffer, beginByte - 9);
				}

				packet.writeInt(buffer, 0);

				if(skill.getClassId() > 0)
					packet.writeInt(buffer, skill.getId());
				else
					packet.writeInt(buffer, skill.getId());// ид скила

				packet.writeByte(buffer, skill.isPassive()? 0 : 1);// ативный 01, пассивный 00

				packet.writeInt(buffer, skill.getPrice());// цена за изучение скила
				packet.writeInt(buffer, skill.getMinLevel());// минимальный лвл на изучение
				packet.writeByte(buffer, SkillShopDialog.isLearneableSkill(player, currentSkills, skill, false)? 1 : 0); //может изучать 1 или неможет 0

				if(skill.getReplaceId() != 0)
				{
					packet.writeShort(buffer, beginByte - 9);
					packet.writeShort(buffer, 0);
					packet.writeInt(buffer, skill.getReplaceId());// ид заменяемого скила
					packet.writeByte(buffer, 1);// ативный 01, пассивный 00 заменяемый скилл
				}
			}

			return packet;
		}
		finally
		{
			buffer.flip();
		}
	}

	/** промежуточный буффер */
	private ByteBuffer prepare;

	public SkillShopList()
	{
		// создаем промежуточный буфер
		this.prepare = ByteBuffer.allocate(8192).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.NPC_DIALOG_SKILL_LEARN;
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
