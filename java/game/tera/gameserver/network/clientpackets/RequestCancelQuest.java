package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;
import tera.util.LocalObjects;

/**
 * Клиентский пакет обновление квеста на панели.
 *
 * @author Ronn
 */
public class RequestCancelQuest extends ClientPacket
{
	/** игрок */
	private Player player;

	/** уник ид квеста */
	private int objectId;


	@Override
	public void finalyze()
	{
		player = null;
	}

	@Override
	public void readImpl()
	{
		if(buffer.remaining() < 12)
			return;

		player = owner.getOwner();

		readInt();
		readInt();

		objectId = readInt();
	}

	@Override
	public void runImpl()
	{
		// если игрока нет, выходим
		if(player == null)
			return;

		// получаем его список квестов
		QuestList questList = player.getQuestList();

		// если списка нет, выходим
		if(questList == null)
		{
			log.warning(this, new Exception("not found quest list for player " + player.getName()));
			return;
		}

		// получаем нужный квест
		QuestState state = questList.getQuestState(objectId);

		// если его нету, выходим
		if(state == null)
			return;

		// получаем сам квест
		Quest quest = state.getQuest();

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// получаем ивент
		QuestEvent event = local.getNextQuestEvent();

		// вносим игрока
		event.setPlayer(player);
		// вносим квест
		event.setQuest(quest);

		// отменяем квест
		quest.cancel(event, false);
	}
}