package tera.gameserver.network.clientpackets;

import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.npc.interaction.Link;
import tera.gameserver.model.playable.Player;

/**
 * Клиентский пакет для получение инфы о выбранном подменю из диалогового окна
 *
 * @author Ronn
 */
public class RequestNpcLink extends ClientPacket
{
	/** игрок */
	private Player player;

	/** номер ссылки */
	private int index;

	@Override
	public void finalyze()
	{
		player  = null;
	}

	@Override
	public boolean isSynchronized()
	{
		return true;
	}

	@Override
	protected void readImpl()
	{
		player = owner.getOwner();

		readInt();

		index = readByte();
	}

	@Override
	protected void runImpl()
	{
		if(player == null)
			return;

		// получаем последнего НПС
		Npc npc = player.getLastNpc();

		// получаем нажатый линк
		Link link = player.getLink(--index);

		// если такой есть и условия выполнены
		if(link != null && npc != null && link.test(npc, player))
		{
			// запоминаем линк
			player.setLastLink(link);
			// очищаем список линков
			player.clearLinks();

			// вызываем ответ на нажатие
			link.reply(npc, player);
		}
		// если нпс есть
		else if(npc != null)
		{
			// получаем последнюю нажатую ссылку
			link = player.getLastLink();

			// забываем про нее
			player.setLastLink(null);

			// если ссылка есть и условия выполнены
			if(link != null && link.test(npc, player))
				// вызываем ответ на нажатие ее
				link.reply(npc, player);
		}
	}
}
