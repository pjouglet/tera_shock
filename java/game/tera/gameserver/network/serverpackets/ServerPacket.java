package tera.gameserver.network.serverpackets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import rlib.network.packets.AbstractSendablePacket;
import rlib.util.Strings;
import rlib.util.Util;
import rlib.util.pools.FoldablePool;
import rlib.util.pools.Pools;
import tera.gameserver.network.ServerPacketType;
import tera.gameserver.network.model.UserClient;

/**
 * Базовая модель серверного пакета.
 *
 * @author Ronn
 * @created 24.03.2012
 */
@SuppressWarnings("unchecked")
public abstract class ServerPacket extends AbstractSendablePacket<UserClient>
{
	private static final StringBuilder EMPTY_STRING_BUILDER = new StringBuilder();
	
	/** массив пулов под все виды серверных пакетов */
	private static FoldablePool<ServerPacket>[] pools = new FoldablePool[ServerPacketType.LENGTH];

	/** тип серверного пакета */
	private final ServerPacketType type;

	/** получаем конструктор пакета */
	private Constructor<? extends ServerPacket> constructor;

	/** индекс массива пулов */
	private final int index;

	public ServerPacket()
	{
		try
		{
			this.type = getPacketType();
			this.index = type.ordinal();
			this.constructor = getClass().getConstructor();
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			log.warning(this, e);
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void complete()
	{
		// уменьшаем счетчик
		decreaseSends();

		// если это была последняя отправка этого пакета
		if(counter == 0)
			// синхронизируемся
			synchronized(this)
			{
				// если точно была последняя отправка
				if(counter == 0)
				{
					// уменьшаем еще дальше счетчик
					counter -= 1;
					// ложим в пул
					getPool().put(this);
				}
			}
	}

	/**
	 * @return тип серверного пакета.
	 */
	public abstract ServerPacketType getPacketType();

	/**
	 * @return получить пул нужного типа пакетов.
	 */
	protected final FoldablePool<ServerPacket> getPool()
	{
		// получаем пул
		FoldablePool<ServerPacket> pool = pools[index];

		// если его нет
		if(pool == null)
		{
			// синхронизируемся
			synchronized(pools)
			{
				// получаем еще раз
				pool = pools[index];

				// если его нет
				if(pool == null)
				{
					// создаем новый
					pool = Pools.newConcurrentFoldablePool(ServerPacket.class);
					// вносим
					pools[index] = pool;
				}
			}
		}

		// возвращаем нужный пул.
		return pool;
	}

	/**
	 * @return новый экземпляр пакета.
	 */
	public final ServerPacket newInstance()
	{
		// получаем пакет из пула
		ServerPacket packet = getPool().take();

		// если его нету
		if(packet == null)
			try
			{
				// создаем новый
				packet = constructor.newInstance();
			}
			catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				log.warning(this, e);
			}

		// возвращаем пакет
		return packet;
	}

	@Override
	public void write(ByteBuffer buffer)
	{
		// если пакет уже запулизирован
		if(counter < 0)
		{
			log.warning(this, "write pooled packet");
			return;
		}

		try
		{
			writeImpl(buffer);
		}
		catch(Exception e)
		{
			// помечаем как пакет с ошибкой
			counter = Integer.MIN_VALUE;

			// выводим ошибку
			log.warning(this, e);
			log.warning(this, "Buffer " + buffer + "\n" + Util.hexdump(buffer.array(), buffer.position()));
		}
	}

	@Override
	public final void writeHeader(ByteBuffer buffer, int length)
	{
		buffer.putShort(0, (short) length);
	}

	/**
	 * Запись опкода.
	 */
	protected final void writeOpcode()
	{
		writeOpcode(buffer);
	}

	/**
	 * Запись опкода.
	 */
	protected final void writeOpcode(ByteBuffer buffer)
	{
		writeShort(buffer, type.getOpcode());
	}

	@Override
	public final void writePosition(ByteBuffer buffer)
	{
		buffer.position(2);
	}

	/**
     * Запись массив чарактеров
     *
     * @param charSequence
     */
	protected final void writeS(CharSequence charSequence)
	{
		writeS(charSequence, true);
	}

	/**
	 * @param charSequence
	 * @param isNull
	 */
	protected final void writeS(CharSequence charSequence, boolean isNull)
	{
		if(charSequence == null)
			charSequence = Strings.EMPTY;

		buffer.order(ByteOrder.BIG_ENDIAN);

		for(int i = 0, length = charSequence.length(); i < length; i++)
			buffer.putChar(charSequence.charAt(i));

		if(isNull)
			buffer.putShort((short) 0x0000);

		buffer.order(ByteOrder.LITTLE_ENDIAN);
	}

	/**
	 * @param string строка.
	 */
	@Override
	protected final void writeString(ByteBuffer buffer, String string)
	{
		if(string == null)
			string = Strings.EMPTY;

		for(int i = 0, length = string.length(); i < length; i++)
			buffer.putChar(string.charAt(i));

		buffer.putShort((short) 0x0000);
	}
	
	/**
	 * Запись билдера строки в буффер.
	 * 
	 * @param buffer байтовый буффер.
	 * @param builder билдер строки.
	 */
	protected final void writeStringBuilder(ByteBuffer buffer, StringBuilder builder)
	{
		if(builder == null)
			builder = EMPTY_STRING_BUILDER;

		for(int i = 0, length = builder.length(); i < length; i++)
			buffer.putChar(builder.charAt(i));

		buffer.putShort((short) 0x0000);
	}
}