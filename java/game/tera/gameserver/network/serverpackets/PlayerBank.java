package tera.gameserver.network.serverpackets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import tera.gameserver.model.inventory.Bank;
import tera.gameserver.model.inventory.Cell;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.playable.Player;
import tera.gameserver.network.ServerPacketType;

/**
 * Серверный пакет с описанием содержимого личного банка игрока.
 *
 * @author Ronn
 */
public class PlayerBank extends ServerPacket
{
	private static final ServerPacket instance = new PlayerBank();

	public static PlayerBank getInstance(Player player, int startCell)
	{
		PlayerBank packet = (PlayerBank) instance.newInstance();

		ByteBuffer buffer = packet.getPrepare();

		try
		{
			Bank bank = player.getBank();

			if(bank == null)
				return packet;

			// определяем стартовую ячейку
			startCell = Math.min(startCell, bank.getMaxSize());

			// определяем конечную ячейку
			int endCell = Math.min(startCell + bank.getTabSize(), bank.getMaxSize());

			int bytes = 44;

			int last = bank.getMaxSize() - 1;

			packet.writeShort(buffer, bank.getUsedCount());// 2A 00 кол-во итемовв банке
			packet.writeShort(buffer, bytes);// 2C 00 //44
			packet.writeInt(buffer, player.getObjectId());// 45 53 0D 00 обжект ид
			packet.writeInt(buffer, player.getSubId());// 00 80 00 01 Саб ид
			packet.writeLong(buffer, 1);// 01 00 00 00 00 00 00 00
			packet.writeInt(buffer, 0);// 00 00 00 00
			packet.writeInt(buffer, 0);// 4D 00 00 00 //77
			packet.writeInt(buffer, 48);// 30 00 00 00 //48
			packet.writeLong(buffer, bank.getMoney());// 00 00 00 00 00 00 00 00

			bank.lock();
			try
			{
				Cell[] cells = bank.getCells();

				// перебираем их
				for(int i = startCell; i < endCell; i++)
				{
					// получаем ячейку
					Cell cell = cells[i];

					if(cell.isEmpty())
						continue;

					// получаем итем в ячейки
					ItemInstance item = cell.getItem();

					packet.writeShort(buffer, bytes);// 2C 00

					if(i == last)
						bytes = 0;
					else
						bytes += 62;

					packet.writeShort(buffer, bytes);// 6A 00
					packet.writeInt(buffer, 0);//
					packet.writeInt(buffer, item.getItemId());// ИД итема
					packet.writeInt(buffer, item.getObjectId());// Обжект ИД итема
					packet.writeInt(buffer, item.getSubId());
					packet.writeInt(buffer, player.getObjectId());// Обжект ИД наш
					packet.writeInt(buffer, 0);//
					packet.writeInt(buffer, i - startCell);// Номер ячейки в которой лежит итем на складе
					packet.writeInt(buffer, 1);
					packet.writeInt(buffer, 1);// меняется
					packet.writeInt(buffer, (int) item.getItemCount());// меняется как правило одинакого с переменной выше воз-но кол-во
					packet.writeInt(buffer, 0);
					packet.writeInt(buffer, 0);// тут иногда 1
					packet.writeLong(buffer, 0);
					packet.writeShort(buffer, 0);
				}
			}
			finally
			{
				bank.unlock();
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

	public PlayerBank()
	{
		this.prepare = ByteBuffer.allocate(1024000).order(ByteOrder.LITTLE_ENDIAN);
	}

	@Override
	public void finalyze()
	{
		prepare.clear();
	}

	@Override
	public ServerPacketType getPacketType()
	{
		return ServerPacketType.PLAYER_BANK;
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