package tera.gameserver.network.model;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

import rlib.network.server.ServerNetwork;
import rlib.network.server.client.AbstractClientConnection;
import rlib.util.Util;
import tera.Config;
import tera.gameserver.network.ClientPacketType;
import tera.gameserver.network.clientpackets.ClientPacket;
import tera.gameserver.network.crypt.CryptorState;
import tera.gameserver.network.serverpackets.ServerPacket;

/**
 * Модель конекта пользователя к серверу.
 *
 * @author Ronn
 */
public final class UserAsynConnection extends AbstractClientConnection<UserClient, ClientPacket, ServerPacket>
{
	/** пакет дял получение клиенсткого крипт ключа */
	private static final ClientPacket CLIENT_KEY = ClientPacketType.CLIENT_KEY.getPacket();

	/** ожидающий буффер */
	private final ByteBuffer waitBuffer;

	/** размер ожидаемого пакета */
	private int waitSize;

	/** юзать ли декриптование */
	private boolean canDecrypt;

	public UserAsynConnection(ServerNetwork network, AsynchronousSocketChannel channel)
	{
		super(network, channel, ServerPacket.class);

		this.waitBuffer = network.getReadByteBuffer();
		this.waitBuffer.clear();
		this.waitSize = -1;
		this.canDecrypt = true;
	}

	@Override
	public void close()
	{
		super.close();

		lock.lock();
		try
		{
			if(isClosed())
				return;

			network.putReadByteBuffer(waitBuffer);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Получение пакета из буфера.
	 */
	private ClientPacket getPacket(ByteBuffer buffer, UserClient client)
	{
		int opcode = -1;

		// если в буфере нет байтов для извлечения опкода
		if(buffer.remaining() < 2)
			return null;

		// читаем опкод
		opcode = buffer.getShort() & 0xFFFF;

		// получаем пакет с таким опкодом
		ClientPacket packet = ClientPacketType.createPacket(opcode);
		//System.out.println(packet);
		/*if(packet == null)
			log.warning("not found packet for opcode " + Integer.toHexString(opcode).toUpperCase() + "\n" + Util.hexdump(buffer.array(), buffer.limit()));*/

		return packet;
	}

	/**
	 * @return ожидающий буффер.
	 */
	protected ByteBuffer getWaitBuffer()
	{
		return waitBuffer;
	}

	/**
	 * @return использовать ли криптор.
	 */
	private boolean isCanDecrypt()
	{
		return canDecrypt;
	}

	@Override
	protected boolean isReady(ByteBuffer buffer)
	{
		// получаем ожидаемый буффер
		ByteBuffer waitBuffer = getWaitBuffer();

		if(waitBuffer.position() == 0 && buffer.limit() < 1000)
			return true;

		// получаем клиент
		UserClient client = getClient();

		// декриптуем пакет
		client.decrypt(buffer, 0, buffer.limit());

		// если размер большого пакета определен
		if(waitSize != -1)
			// вставляем новый кусок пакета
			waitBuffer.put(buffer);
		else
		{
			// вставляем кусок в буффер
			waitBuffer.put(buffer);
			// возвращаемся вначало пакета
			waitBuffer.position(0);

			// получаем размер буффера
			waitSize = waitBuffer.getShort() & 0xFFFF;

			// очищаем ожидающий буфер
			waitBuffer.position(buffer.limit());
		}

		// если уже все куски собрались
		if(waitSize <= waitBuffer.position())
		{
			// подготавливаем к переносу ожидающего буфера
			waitBuffer.flip();
			// очищаем буфер с куском
			buffer.clear();
			// вносим туда целый пакет
			buffer.put(waitBuffer);
			// очищаем ожидающий буффер
			waitBuffer.clear();
			// подготавливам буфер к обработке
			buffer.flip();

			// обнуляем размер
			waitSize = -1;

			// ставим флаг не юза декриптовки
			setCanDecrypt(false);

			return true;
		}

		return false;
	}

	@Override
	protected void movePacketToBuffer(ServerPacket packet, ByteBuffer buffer)
	{
		//System.out.println("write packet " + packet.getClass().getSimpleName());

		// получаем клиент
		UserClient client = getClient();

		// очищаем буффер для записи
		buffer.clear();

		// если это отправляемый пакет, ставим позицию 2
		if(client.getCryptorState() == CryptorState.READY_TO_WORK)
			buffer.position(2);

		if(packet.isSynchronized())
		{
			synchronized(packet)
			{
				// устанавливаем буффер
				packet.setBuffer(buffer);
				// устанавливаем клиент
				packet.setOwner(client);
				// записываем
				packet.writeLocal();
				// зануляем буффер
				packet.setBuffer(null);
			}
		}
		else
		{
			// устанавливаем клиент
			packet.setOwner(client);
			// записываем данные
			packet.write(buffer);
		}

		// подготовка буффера
		buffer.flip();

		// если это пакет
		if(client.getCryptorState() == CryptorState.READY_TO_WORK)
			// записываеSм длинну пакета
			packet.writeHeader(buffer, buffer.limit());

		if(Config.DEVELOPER_DEBUG_SERVER_PACKETS)
		{
			System.out.println("Server packet " + packet.getName() + ", dump(size: " + buffer.limit() + "):");
			System.out.println(Util.hexdump(buffer.array(), buffer.limit()));
		}

		// защифровка пакета
		client.encrypt(buffer, 0, buffer.limit());
	}

	@Override
	protected void readPacket(ByteBuffer buffer)
	{
		// получаем клиент
		UserClient client = getClient();

		if(!isCanDecrypt())
			setCanDecrypt(true);
		else
		{
			// расшифровываем
			client.decrypt(buffer, 0, buffer.limit());
		}

		if(Config.DEVELOPER_DEBUG_CLIENT_PACKETS)
			System.out.println("Client dump(size: " + buffer.limit() + "):\n" + Util.hexdump(buffer.array(), buffer.limit()));

		//if(client.getOwner() != null && client.getOwner().getName().equals("Slayer"))
		//	System.out.println("Client dump(size: " + buffer.limit() + "):\n" + Util.hexdump(buffer.array(), buffer.limit()));

		// если сейчас стадия чтения клиентского пакета
		if(client.getCryptorState() == CryptorState.READY_TO_WORK && buffer.remaining() > 1)
		{
			try
			{
				// определяем длинну первого пакета
				int length = buffer.getShort() & 0xFFFF;

				// если тут только 1 пакет, то его сразу парсим
				if(length >= buffer.remaining())
					client.readPacket(getPacket(buffer, client), buffer);
				else
				{
					// иначе читаем первый
					client.readPacket(getPacket(buffer, client), buffer);

					// устанавливаем позицию в конце первого
					buffer.position(length);

					// определяем есть ли еще
					for(int i = 0; buffer.remaining() > 2 && i < Config.NETWORK_MAXIMUM_PACKET_CUT; i++)
					{
						// читаем длинну след. пакета
						length += buffer.getShort() & 0xFFFF;

						// парчис и читаем след. пакет
						client.readPacket(getPacket(buffer, client), buffer);

						// если больше нету, выходим
						if(length < 4 || length > buffer.limit())
							break;

						// ставим позицию в конце последнего прочитанного пакета
						buffer.position(length);
					}
				}
			}
			catch(Exception e)
			{
				log.warning(e);
			}
		}
		else if(client.getCryptorState() != CryptorState.READY_TO_WORK)
			client.readPacket(CLIENT_KEY.newInstance(), buffer);
	}

	/**
	 * @param canDecrypt использовать ли криптор.
	 */
	private void setCanDecrypt(boolean canDecrypt)
	{
		this.canDecrypt = canDecrypt;
	}
}
