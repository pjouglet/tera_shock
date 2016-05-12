package tera.gameserver.model.quests.classes;

import org.w3c.dom.Node;

import rlib.util.VarTable;
import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.listeners.LevelUpListener;
import tera.gameserver.model.listeners.PlayerSpawnListener;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.quests.QuestEvent;
import tera.gameserver.model.quests.QuestList;
import tera.gameserver.model.quests.QuestType;
import tera.util.LocalObjects;

/**
 * Модель квеста, который автоматически выдается при достижении определенного уровня.
 *
 * @author Ronn
 */
public class LevelUpQuest extends AbstractQuest implements LevelUpListener, PlayerSpawnListener
{
	/** стартовый уровень */
	private int startLevel;
	/** предыдущий квест */
	private int prev;

	public LevelUpQuest(QuestType type, Node node)
	{
		super(type, node);

		VarTable vars = VarTable.newInstance(node);

		// получаем стартовый уровень
		this.startLevel = vars.getInteger("startLevel", -1);
		this.prev = vars.getInteger("prev", 0);

		// получаем менеджера событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// добавляемся на праслушку лвл апов
		eventManager.addLevelUpListener(this);

		// добавляемся на прослушку спавнов
		eventManager.addPlayerSpawnListener(this);
	}

	@Override
	public void onLevelUp(Player player)
	{
		// если игрока нет, выходим
		if(player == null || player.getLevel() < startLevel)
			return;

		// получаем квест лист игрока
		QuestList questList = player.getQuestList();

		// если его нет илоб этот квест уже выполнен/активен, то выходим
		if(questList == null || questList.isCompleted(this) || questList.getQuestState(this) != null)
			return;

		// если требуется предыдущий квест, а он не выполнен, выходим
		if(prev != 0 && !questList.isCompleted(prev))
			return;

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		// вынимаем с него ивент
		QuestEvent event = local.getNextQuestEvent();

		// запоминаем игрока
		event.setPlayer(player);
		// запоминаем квест
		event.setQuest(this);

		// запускаем квест
		start(event);
	}

	@Override
	public void onSpawn(Player player)
	{
		onLevelUp(player);
	}
}
