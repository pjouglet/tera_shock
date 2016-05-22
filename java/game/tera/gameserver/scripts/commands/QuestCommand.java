package tera.gameserver.scripts.commands;

import rlib.util.array.Array;
import tera.gameserver.manager.DataBaseManager;
import tera.gameserver.manager.QuestManager;
import tera.gameserver.model.World;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;
import tera.gameserver.network.serverpackets.QuestInfo;
import tera.gameserver.network.serverpackets.QuestMoveToPanel;
import tera.gameserver.network.serverpackets.QuestStarted;
import tera.gameserver.network.serverpackets.QuestVideo;
import tera.util.LocalObjects;

/**
 * @author Ronn
 */
public class QuestCommand extends AbstractCommand
{
	public QuestCommand(int access, String[] commands)
	{
		super(access, commands);
	}

	@Override
	public void execution(String command, Player player, String values)
	{
		// получаем менеджера квестов
		QuestManager questManager = QuestManager.getInstance();

		switch(command)
		{
			case "quest_reload":
			{
				questManager.reload();

				player.sendMessage("quests reloaded.");

				break;
			}
			case "quest_remove":
			{
				QuestList questList = player.getQuestList();

				questList.removeQuestComplete(Integer.parseInt(values));

				// получаем менеджера БД
				DataBaseManager dbManager = DataBaseManager.getInstance();

				dbManager.removeQuest(player, questManager.getQuest(Integer.parseInt(values)));

				player.sendMessage("Quest have been deleted.");

				break;
			}
			case "quest_state":
			{
				String[] vals = values.split(" ");

				QuestList questList = player.getQuestList();

				Quest quest = questManager.getQuest(Integer.parseInt(vals[0]));

				QuestState questState = questList.getQuestState(quest);

				questState.setState(Integer.parseInt(vals[1]));

				player.sendPacket(QuestStarted.getInstance(questState, 0, 0, 0, 0, 0), true);
				player.sendPacket(QuestMoveToPanel.getInstance(questState), true);

				player.sendMessage("Квест \"" + quest.getName() + "\" перевен на " + questState.getState() + " стади.");

				break;
			}
			case "quest_cond":
			{
				String text = "<FONT FACE=\"$ChatFont\" SIZE=\"18\" COLOR=\"#F6DA21\" KERNING=\"0\"><A HREF=\"asfunction:chatLinkAction,2#####" + values + "`15\">&lt;quest info&gt;</A></FONT><FONT>";

				player.sendMessage(text);

				//System.out.println(player.getName() + ": " + text);
				break;
			}
			case "quest_cancel":
			{
				// получаем нужный квест
				Quest quest = questManager.getQuest(Integer.parseInt(values));

				// если квеста нет, выходим
				if(quest == null)
				{
					player.sendMessage("такого квеста нету.");
					return;
				}

				// получаем локальные объекты
				LocalObjects local = LocalObjects.get();

				// получаем ивент
				QuestEvent event = local.getNextQuestEvent();

				// вносим квест
				event.setQuest(quest);
				// вносим игрока
				event.setPlayer(player);

				// отменяем квест
				quest.cancel(event, true);

				break;
			}
			case "quest_movie":
			{
				player.sendPacket(QuestVideo.getInstance(Integer.parseInt(values)), true);

				break;
			}
			case "quest_accept":
			{
				// получаем нужный квест
				Quest quest = questManager.getQuest(Integer.parseInt(values));

				// если квеста нет, выходим
				if(quest == null)
				{
					player.sendMessage("такого квеста нету.");
					return;
				}

				// получаем локальные объекты
				LocalObjects local = LocalObjects.get();

				// получаем ивент
				QuestEvent event = local.getNextQuestEvent();

				// вносим квест
				event.setQuest(quest);
				// вносим игрока
				event.setPlayer(player);

				// запускаем квест
				quest.start(event);

				player.sendMessage("quest " + quest.getName() + "accepted.");

				break;
			}
			case "quest_start":
			{
				String[] vals = values.split(" ");

				int id = Integer.parseInt(vals[0]);
				int state = Integer.parseInt(vals[1]);

				// получаем список квестов
				QuestList questList = player.getQuestList();

				QuestState qs = questList.newQuestState(player, questManager.getQuest(id), state);

				player.sendPacket(QuestStarted.getInstance(qs, 0, 0, 0, 0, 0), true);
				player.sendPacket(QuestMoveToPanel.getInstance(qs), true);

				break;
			}
			case "quest_info":
			{
				// получаем локальные объекты
				LocalObjects local = LocalObjects.get();

				// получаем окружающих нпс
				Array<Npc> npcs = World.getAround(Npc.class, local.getNextNpcList(), player);

				if(npcs.isEmpty())
					return;

				// разбираем на параметры
				String[] vals = values.split(" ");

				// получаем квест
				Quest quest = questManager.getQuest(Integer.parseInt(vals[0]));

				// отправляем пакет
				player.sendPacket(QuestInfo.getInstance(npcs.first(), player, quest, Integer.parseInt(vals[2]), Integer.parseInt(vals[1]), "quest_info"), true);

				break;
			}
		}
	}

}
