package tera.gameserver.templates;

import java.lang.reflect.Field;

import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.base.PlayerClass;
import tera.gameserver.model.base.Race;
import tera.gameserver.model.base.Sex;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.tables.ItemTable;

/**
 * Темплейт с базовыми характеристиками игрока.
 *
 * @author Ronn
 */
public final class PlayerTemplate extends CharTemplate
{
	/** плеер класс */
	protected PlayerClass playerClass;
	/** раса */
	protected Race race;
	/** пол */
	protected Sex sex;

	/** скилы, выдающиеся при создании перса */
	protected SkillTemplate[][] skills;

	/** ид модели персонажа */
	protected int id;
	/** ид опрокидывания */
	protected int owerturnId;

	/** итемы, выдающиеся при создании перса */
	protected int[][] items;

	/**
	 * @param vars таблица параметров.
	 * @param funcs набор функций.
	 * @param classType класс игрока.
	 * @param race раса игрока.
	 * @param id ид модкели игрока.
	 * @param items итемы, выдающиеся при создании перса.
	 * @param skills скилы, выдающиеся при создании перса.
	 * @param sex пол игрока.
	 */
	public PlayerTemplate(VarTable vars, Func[] funcs, PlayerClass classType, Race race, Sex sex, int id, int[][] items, SkillTemplate[][] skills)
	{
		super(vars, funcs);

		this.playerClass = classType;
		this.race = race;
		this.id = id;
		this.items = items;
		this.skills = skills;
		this.sex = sex;
		this.owerturnId = (id + 1000000) | 0x08000000;
		//applyRace();
	}

	/**
	 * Приминение модификаторов расы на шаблон.
	 */
	public void applyRace()
	{
		// получаем расу шаблона
		Race race = getRace();

		// получаем поля расы
		Field[] fields = race.getClass().getDeclaredFields();

		// получаем текущий класс
		Class<CharTemplate> cs = CharTemplate.class;

		try
		{
			// перебираем поля
			for(Field field : fields)
			{
				// пропускаем ненужные
				if(field.getType() != float.class)
					continue;

				// получаем название поля
				String name = field.getName();

				// пропускаем реген мп
				if("regMp".equals(name))
					continue;

				// запоминаем прошлый флаг
				boolean old = field.isAccessible();

				// разрешаем изменять
				field.setAccessible(true);

				// получаем изменяемое поле
				Field target = cs.getDeclaredField(name);

				// получаем текущее состояние доступа
				boolean targetOld = target.isAccessible();

				// разрешаем доступ
				target.setAccessible(true);

				// применяем модификацию
				target.setInt(this, (int) (target.getInt(this) * field.getFloat(race)));

				// возвращаем старое состояние
				target.setAccessible(targetOld);

				// возвращаем старый флаг
				field.setAccessible(old);
			}
		}
		catch(IllegalArgumentException | NoSuchFieldException | SecurityException | IllegalAccessException e)
		{
			log.warning(this, e);
		}

		if(regMp < 0)
			regMp *= (2F - race.getRegMp());
		else
			regMp *= race.getRegMp();

		if(race.getFuncs() == null)
			return;

		// получаем текущие функции
		Func[] funcs = getFuncs();

		// создаем список новых
		Array<Func> array = Arrays.toArray(Func.class);

		// вносим текущие
		array.addAll(funcs);

		// вносим функции расы
		array.addAll(race.getFuncs());

		// сжимаем массив
		array.trimToSize();

		// сохраняем
		this.funcs = array.array();
	}

	/**
	 * @return ид класса игрока.
	 */
	public final int getClassId()
	{
		return playerClass.getId();
	}

	/**
	 * @return класс игрока.
	 */
	public final PlayerClass getPlayerClass()
	{
		return playerClass;
	}

	/**
	 * @return начальные итемы игрока.
	 */
	public final int[][] getItems()
	{
		return items;
	}

	@Override
	public int getModelId()
	{
		return id;
	}

	/**
	 * @return ид опрокидывания.
	 */
	public final int getOwerturnId()
	{
		return owerturnId;
	}

	/**
	 * @return раса игрока.
	 */
	public final Race getRace()
	{
		return race;
	}

	/**
	 * @return ид рассы игрока.
	 */
	public final int getRaceId()
	{
		return race.getId();
	}

	/**
	 * @return пол игрока.
	 */
	public final Sex getSex()
	{
		return sex;
	}

	/**
	 * @return массив скилов игрока.
	 */
	public final SkillTemplate[][] getSkills()
	{
		return skills;
	}

	@Override
	public int getTemplateId()
	{
		return id;
	}

	/**
	 * Выдача стартовых итемов в инвентарь.
	 *
	 * @param inventory интвентарь игрока.
	 */
	public void giveItems(Inventory inventory)
	{
		// если инвенторя нет, выходим
		if(inventory == null)
			return;

		// получаем таблицу итемов
		ItemTable itemTable = ItemTable.getInstance();

		// перебираем начальные итемы
		for(int i = 0, length = items.length; i < length; i++)
		{
			// получаем итем
			int[] item = items[i];

			// пробуем получить темплейт итема
			ItemTemplate template = itemTable.getItem(item[0]);

			// если такого нет, пропускаем
			if(template == null)
				continue;

			if(template.isStackable())
				inventory.addItem(item[0], item[1], "CreatePlayer");
			else
			{
				for(int j = 0, size = item[1]; j < size; j++)
					inventory.addItem(item[0], 1, "CreatePlayer");
			}
		}
	}

	/**
	 * Выдает игроку его базовые скилы.
	 *
	 * @param player игрок, которому нужно выдать скилы.
	 */
	public void giveSkills(Player player)
	{
		for(SkillTemplate[] skill : skills)
			player.addSkills(skill, false);
	}
}

