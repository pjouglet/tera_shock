package tera.gameserver.model.quests.actions;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import rlib.util.wraps.Wrap;
import rlib.util.wraps.Wraps;
import tera.gameserver.model.npc.interaction.Condition;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestActionType;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestState;

/**
 * Акшен отнимания числа к квестовой переменной.
 *
 * @author Ronn
 */
public class ActionSubVar extends AbstractQuestAction
{
	/** название переменной */
	private String name;

	/** прибавляемое значение */
	private int value;

	public ActionSubVar(QuestActionType type, Quest quest, Condition condition, Node node)
	{
		super(type, quest, condition, node);

		try
		{
			VarTable vars = VarTable.newInstance(node);

			this.name = vars.getString("var");
			this.value = vars.getInteger("value");
		}
		catch(Exception e)
		{
			log.warning(this, e);
		}
	}

	@Override
	public void apply(QuestEvent event)
	{
		// получаем игрока
		Player player = event.getPlayer();

		// если его нет, выходим
		if(player == null)
		{
			log.warning(this, "not found player");
			return;
		}

		// получаем квестовый лист
		QuestList questList = player.getQuestList();

		// если его нет, выходим
		if(questList == null)
		{
			log.warning(this, "not found questList");
			return;
		}

		// получаем состояние квеста
		QuestState state = questList.getQuestState(quest);

		// если его нет, выходим
		if(state == null)
		{
			log.warning(this, "not found quest state");
			return;
		}

		// получаем значение переменной
		Wrap wrap = state.getVar(name);

		// если оно есть
		if(wrap != null)
			// увеличиваем
			wrap.setInt(wrap.getInt() - value);
		else
			// иначе вставляем созданное
			state.setVar(name, Wraps.newIntegerWrap(-value, true));
	}

	@Override
	public String toString()
	{
		return "SubVar name = " + name + ", value = " + value;
	}
}
