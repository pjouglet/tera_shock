package tera.gameserver.model.quests.actions;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.wraps.Wrap;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestPanelState;
import tera.gameserver.model.quests.QuestState;
import tera.gameserver.network.serverpackets.QuestUpdateCounter;

/**
 * Акшен обновления квестового счетчика.
 *
 * @author Ronn
 */
public class ActionUpdateCounter extends AbstractQuestAction
{
	/** набор переменных */
	private String[] vars;

	/** счетчики */
	private int[] counts;
	/** необходиоме кол-во */
	private int[] complete;

	public ActionUpdateCounter(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		// создаем промежуточный список
		Array<String> varArray = Arrays.toArray(String.class);

		// получаем атрибуты акшена
		NamedNodeMap attrs = node.getAttributes();

		// перебираем атрибуты
		for(int i = 0, length = attrs.getLength(); i < length; i++)
		{
			// получаем атрибут
			Node item = attrs.item(i);

			// если это название акшена, пропускаем
			if("name".equals(item.getNodeName()))
				continue;

			// добавляем в список
			varArray.add(item.getNodeValue());
		}

		this.vars = new String[varArray.size()];
		this.counts = new int[vars.length];
		this.complete = new int[vars.length];

		for(int i = 0, length = varArray.size(); i < length; i++)
		{
			String[] vals = varArray.get(i).split("[|]");

			vars[i] = vals[0];
			complete[i] = vals.length > 1? Integer.parseInt(vals[1]) : Integer.MAX_VALUE;
		}
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		// если игрока нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// получаем список квестов
		QuestList questList = player.getQuestList();

		// если списка нет, выходим
		if(questList == null)
		{
			log.warning(this, "not found quest list");
			return;
		}

		// получаем состояние квеста
		QuestState state = questList.getQuestState(quest);

		// если состояния, выходим
		if(state == null)
		{
			log.warning(this, "not found quest state");
			return;
		}

		boolean doned = true;

		// перебираем интересуемые переменные
		for(int i = 0, length = vars.length; i < length; i++)
		{
			// получаем название переменной
			String var = vars[i];

			// получаем значение переменной
			Wrap wrap = state.getVar(var);

			// если значения нету, записываем 0
			if(wrap == null)
				counts[i] = 0;
			else
				// иначе записываем значение
				counts[i] = wrap.getInt();

			if(doned && counts[i] < complete[i])
				doned = false;
		}

		// отправляем пакет
		player.sendPacket(QuestUpdateCounter.getInstance(state, counts, doned), true);
		// добавляем на панель
		player.updateQuestInPanel(state, QuestPanelState.UPDATE);
	}

	@Override
	public String toString()
	{
		return "ActionUpdateCounter vars = " + java.util.Arrays.toString(vars) + ", counts = " + java.util.Arrays.toString(counts);
	}
}
