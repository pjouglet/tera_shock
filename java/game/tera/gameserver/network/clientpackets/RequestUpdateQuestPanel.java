package tera.gameserver.network.clientpackets;

import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestPanelState;
import tera.gameserver.model.quests.QuestState;

/**
 * Клиентский пакет обновление квеста на панели.
 *
 * @author Ronn
 */
public class RequestUpdateQuestPanel extends ClientPacket
{
	/** игрок */
	private Player player;

	/** новое состояние квеста на панели */
	private QuestPanelState panelState;

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
		player = owner.getOwner();

		objectId = readInt();

		readByte();

		panelState = QuestPanelState.valueOf(readShort());
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
			return;

		// получаем нужный квест
		QuestState quest = questList.getQuestState(objectId);

		// если его нету, выходим
		if(quest == null)
			return;

		// если состояние не изменилось, выходим
		if(quest.getPanelState() == panelState)
			return;

		// обновляем квест на панели
		player.updateQuestInPanel(quest, panelState);
	}
}