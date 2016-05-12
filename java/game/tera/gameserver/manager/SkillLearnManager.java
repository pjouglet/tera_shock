package tera.gameserver.manager;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.array.Search;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import tera.Config;
import tera.gameserver.document.DocumentSkillLearn;
import tera.gameserver.model.SkillLearn;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.tables.SkillTable;
import tera.gameserver.templates.SkillTemplate;

/**
 * Менеджер изучаемых скилов.
 * 
 * @author Ronn
 */
public final class SkillLearnManager {

	private static final Logger log = Loggers.getLogger(SkillLearnManager.class);

	/** метод поиска нового скила */
	private static final Search<SkillLearn> SEARCH_REPLACED_SKILL = new Search<SkillLearn>() {

		@Override
		public boolean compare(SkillLearn required, SkillLearn target) {
			return required.getId() == target.getReplaceId();
		}
	};

	/** метод поиска заменяемого скила */
	private static final Search<SkillLearn> SEARCH_REPLACEABLE_SKILL = new Search<SkillLearn>() {

		@Override
		public boolean compare(SkillLearn required, SkillLearn target) {
			return required.getReplaceId() == target.getId();
		}
	};

	private static SkillLearnManager instance;

	public static SkillLearnManager getInstance() {

		if(instance == null)
			instance = new SkillLearnManager();

		return instance;
	}

	/**
	 * Определят, выучен ли уже скил на уровень ниже.
	 * 
	 * @param learns список всех возможно ищучаемых скилов.
	 * @param learn проверяемый скил.
	 * @param skills список скилов игрока.
	 */
	public static boolean isAvailable(Array<SkillLearn> learns, SkillLearn learn, Table<Integer, Skill> skills) {

		for(int i = 0, length = learns.size(); i < length; i++) {

			SkillLearn replaceable = learns.search(learn, SEARCH_REPLACEABLE_SKILL);

			if(replaceable == null)
				return false;

			if(skills.containsKey(replaceable.getUseId()))
				return true;

			learn = replaceable;
		}

		return false;
	}

	/**
	 * Определят, был ли уже заменен данный learn.
	 * 
	 * @param learns список всех возможно ищучаемых скилов.
	 * @param learn проверяемый скил.
	 * @param skills список скилов игрока.
	 */
	private static boolean isReplaced(Array<SkillLearn> learns, SkillLearn learn, Table<IntKey, Skill> skills) {

		for(int i = 0, length = learns.size(); i < length; i++) {

			SkillLearn replaced = learns.search(learn, SEARCH_REPLACED_SKILL);

			if(replaced == null)
				return false;

			if(skills.containsKey(replaced.getUseId()))
				return true;

			learn = replaced;
		}

		return false;
	}

	/** таблица изучаемых скилов */
	private final Array<SkillLearn>[] learns;

	@SuppressWarnings("unchecked")
	private SkillLearnManager() {

		learns = new Array[8];

		for(int i = 0; i < 8; i++)
			learns[i] = Arrays.toArray(SkillLearn.class);

		Array<SkillLearn> result = new DocumentSkillLearn(new File(Config.SERVER_DIR + "/data/skill_learns.xml")).parse();

		SkillTable skillTable = SkillTable.getInstance();

		for(SkillLearn learn : result) {

			SkillTemplate[] skill = skillTable.getSkills(learn.getClassId(), learn.getUseId());

			if(skill == null || skill.length < 1 || (Config.WORLD_LEARN_ONLY_IMPLEMENTED_SKILLS && !skill[0].isImplemented()))
				continue;

			learns[learn.getClassId()].add(learn);
		}

		int counter = 0;

		for(Array<SkillLearn> list : learns) {

			if(list == null)
				continue;

			list.trimToSize();

			counter += list.size();
		}

		log.info("loaded " + counter + " skill learn's for " + learns.length + " clases.");
	}

	/**
	 * Получаем список доступных новых скилов для указанного игрока.
	 * 
	 * @param result список доступных скилов.
	 * @param player игрок, для которого нужно получить список доступных.
	 * @return список доступных скилов.
	 */
	public final void addAvailableSkills(Array<SkillLearn> result, Player player) {

		Array<SkillLearn> learnList = learns[player.getClassId()];

		if(learnList == null) {
			log.warning("classId " + player.getClassId() + "  not has been found available skills.");
			return;
		}

		Table<IntKey, Skill> currentSkills = player.getSkills();

		SkillLearn[] array = learnList.array();

		for(int i = 0, length = learnList.size(); i < length; i++) {

			SkillLearn temp = array[i];

			if(currentSkills.containsKey(temp.getUseId()) || isReplaced(learnList, temp, currentSkills))
				continue;

			result.add(temp);
		}
	}


	/**
	 * Проверка на изученность нужного скила.
	 * 
	 * @param skillId ид скила.
	 * @param player игрок.
	 * @return изучен ли у него этот скил.
	 */
	public boolean isLearned(int skillId, Player player) {

		Array<SkillLearn> learnList = learns[player.getClassId()];

		if(learnList == null)
			return false;

		SkillLearn[] array = learnList.array();

		Table<IntKey, Skill> currentSkills = player.getSkills();

		for(int i = 0, length = learnList.size(); i < length; i++) {

			SkillLearn learn = array[i];

			if(learn.getId() == skillId && (currentSkills.containsKey(learn.getUseId()) || isReplaced(learnList, learn, currentSkills)))
				return true;
		}

		return false;
	}
}
