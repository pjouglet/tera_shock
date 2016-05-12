package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.util.array.Array;
import tera.gameserver.model.Character;
import tera.gameserver.model.EffectList;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с информацией о эффектах члена группы.
 *
 * @author Ronn
 */
public class PartyMemberEffectList extends ServerPacket
{
	private static final ServerPacket instance = new PartyMemberEffectList();

	public static PartyMemberEffectList getInstance(Character character)
	{
		PartyMemberEffectList packet = (PartyMemberEffectList) instance.newInstance();

		// получаем эффект лист игрока
		EffectList effectList = character.getEffectList();

		// если его нет, выходим
		if(effectList == null)
			return packet;

		ByteBuffer buffer = packet.getPrepare();

		effectList.lock();
		try
		{
			int bytes = 20;

			int lenght = ((effectList.size() - 1) * 16 + bytes);

			packet.writeShort(buffer, 5);
			packet.writeShort(buffer, bytes);
			packet.writeShort(buffer, 1);
			packet.writeShort(buffer, lenght);
			packet.writeInt(buffer, 0); // SERVER ID
			packet.writeInt(buffer, character.getObjectId());

			// получаем список эффектов
			Array<Effect> effects = effectList.getEffects();

			// получаем массив эффектов
			Effect[] array = effects.array();

			// перебираем эффекты
			for(int i = 0, length = effects.size(); i < length; i++)
			{
				// получаем эффект
				Effect effect = array[i];

				// если его нет либо он завершился, пропускаем
				if(effect == null || effect.isEnded())
					continue;

				packet.writeShort(buffer, bytes);

				bytes += 16;

				if(lenght < bytes)
					bytes = 0;

				packet.writeShort(buffer, bytes);
				packet.writeInt(buffer, effect.getEffectId());
				packet.writeInt(buffer, effect.getTimeEnd() * 1000);
				packet.writeInt(buffer, 1);
			}
		}
		finally
		{
			effectList.unlock();
		}

		buffer.flip();

		return packet;
	}

	/** подготовленный буффер */
	private final ByteBuffer prepare;

	public PartyMemberEffectList()
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
		return ServerPacketType.PARTY_MEMBER_EFFECT;
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
