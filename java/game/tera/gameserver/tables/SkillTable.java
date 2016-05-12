package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.Strings;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentSkill;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.templates.SkillTemplate;

/**
 * Таблица шаблонов скилов.
 *
 * @author Ronn
 */
public final class SkillTable
{
	private static final Logger log = Loggers.getLogger(SkillTable.class);

	private static SkillTable instance;

	/**
	 * Создание массива скилов из массива темплейтов.
	 *
	 * @param templates массив темплейтов скилов.
	 * @return массив скилов.
	 */
	public static Skill[] create(SkillTemplate[] templates)
	{
		// создаем массив экземпляров
		Skill[] skills = new Skill[templates.length];

		// вносим экземпляры шаблонов
		for(int i = 0, length = templates.length; i < length; i++)
			skills[i] = templates[i].newInstance();

		return skills;
	}

	public static SkillTable getInstance()
	{
		if(instance == null)
			instance = new SkillTable();

		return instance;
	}

	/**
	 * Парсит строку в виде "classId-skillId;classId-skillId;classId-skillId".
	 *
	 * @param text строка с описанием набора склиов.
	 * @return массив скилов.
	 */
	public static Array<SkillTemplate> parseSkills(String text)
	{
		// если строки нет
		if(text == null || text.isEmpty())
			// возвращаем пустой массив
			return Arrays.toArray(SkillTemplate.class, 0);

		// разбиваем строку
		String[] strings = text.split(";");

		// если скилов нет
		if(strings.length < 1)
			// возвращаем путой массив
			return Arrays.toArray(SkillTemplate.class, 0);

		// созадем новый список
		Array<SkillTemplate> array = Arrays.toArray(SkillTemplate.class, strings.length);

		// получаем таблицу скилов
		SkillTable table = getInstance();

		// перебираем
		for(String string : strings)
		{
			String[] vals = null;

			// отрицательный ли класс ид
			boolean negative = false;

			// если класс ид отрицательный
			if(string.startsWith("-"))
			{
				// заменяем знак отрицания
				string = string.replaceFirst("-", ":");

				// ставим флаг наличия отрицательного ид класса
				negative = true;
			}

			// разбиваем строку
			vals = string.split("-");

			// если недостаточно параметров, пропускаем
			if(vals.length < 2)
				continue;

			try
			{
				// парсими ид класса скилов
				int classId = Integer.parseInt(negative? vals[0].replaceFirst(":", "-") : vals[0]);

				// парсим ид шаблона скила
				int templateId = Integer.parseInt(vals[1]);

				// получаем шаблон скила
				SkillTemplate skill = table.getSkill(classId, templateId);

				// вносим в список
				if(skill != null)
					array.add(skill);
			}
			catch(NumberFormatException | ArrayIndexOutOfBoundsException e)
			{
				log.warning(e);
			}
		}

		return array;
	}

	/**
	 * Парсит строку в ввиде "skillId;skillId;skillId".
	 *
	* @param text строка с описанием набора склиов.
	 * @return массив скилов.
	 */
	public static Array<SkillTemplate> parseSkills(String text, int classId)
	{
		// если строка пустая
		if(text == null || text == Strings.EMPTY || text.isEmpty())
			// возвращаем пустой массивы
			return Arrays.toArray(SkillTemplate.class, 0);

		// разбиваем строку
		String[] strings = text.split(";");

		// если в ней скилов нет
		if(strings.length < 1)
			// возвращаем пустой массив
			return Arrays.toArray(SkillTemplate.class, 0);

		// создаем новый массив скилов
		Array<SkillTemplate> array = Arrays.toArray(SkillTemplate.class, strings.length);

		// получаем таблицу скилов
		SkillTable table = getInstance();

		// перебираем строки
		for(String string : strings)
		{
			// если пуста, пропускаем
			if(string.isEmpty())
				continue;

			try
			{
				// парсим ид шаблона
				int templateId = Integer.parseInt(string);

				// получаем шаблон скила
				SkillTemplate skill = table.getSkill(classId, templateId);

				// если такой есть
				if(skill != null)
					// вносим в массив
					array.add(skill);
			}
			catch(NumberFormatException | ArrayIndexOutOfBoundsException e)
			{
				log.warning(e);
			}
		}

		array.trimToSize();

		return array;
	}

	/** таблица скилов по класс ид и скилл ид */
	private Table<IntKey, Table<IntKey, SkillTemplate>> skills;

	/** таблица комбо скилов по класс ид и скилл ид */
	private Table<IntKey, Table<IntKey, SkillTemplate[]>> allskills;

	private SkillTable()
	{
		// создаем таблицы
		skills = Tables.newIntegerTable();
		allskills =Tables.newIntegerTable();

		int counter = 0;

		// получаем файлы
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/skills"));

		// перебираем
		for(File file : files)
		{
			if(file == null)
				continue;

			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// получаем отпарсенные шаблоны
			Array<SkillTemplate[]> parsed = new DocumentSkill(file).parse();

			// перебираем массиы шаблонов
			for(SkillTemplate[] temps : parsed)
			{
				// пустые пропускаем
				if(temps.length == 0)
					continue;

				// получаем первый в массиве
				SkillTemplate first = temps[0];

				// вносим данные в первую таблицу
				{
					// получаем таблицу с шаблоного этого же класса
					Table<IntKey, SkillTemplate> subTable = skills.get(first.getClassId());

					// если нету
					if(subTable == null)
					{
						// создаем новую
						subTable = Tables.newIntegerTable();

						// вносим
						skills.put(first.getClassId(), subTable);
					}

					// перебираем шаблоны
					for(SkillTemplate temp : temps)
					{
						// увеличиваем счетчик
						counter++;

						// получаем старый шаблон
						SkillTemplate old = subTable.put(temp.getId(), temp);

						// если такой есть, сообщаем
						if(old != null)
							log.warning("found duplicate skill " + temp.getId() + ", old " + old.getId() + " in file " + file);
					}
				}

				// вносим данные во вторую таблицу
				{
					Table<IntKey, SkillTemplate[]> subTable = allskills.get(first.getClassId());

					// если тбалицы нет
					if(subTable == null)
					{
						// созадем новую
						subTable = Tables.newIntegerTable();

						// вносим
						allskills.put(first.getClassId(), subTable);
					}

					if(subTable.put(first.getId(), temps) != null)
						log.warning("found duplicate skill " + Arrays.toString(temps));
				}
			}
		}

		log.info("SkillTable", "loaded " + counter + " skills for " + skills.size() + " classes.");
	}

	/**
	 * Получение соотвествтующего скила по класс и скил ид.
	 *
	 * @param classId класс шаблона.
	 * @param templateId ид шаблона.
	 * @return соответствующий шаблон.
	 */
	public SkillTemplate getSkill(int classId, int templateId)
	{
		Table<IntKey, SkillTemplate> table = skills.get(classId);

		if(table == null)
			return null;

		return table.get(templateId);
	}

	/**
	 * Получение соотвествтующего комбо скила по класс и скил ид.
	 *
	 * @param classId класс шаблона.
	 * @param templateId ид шаблона.
	 * @return соответствующий массив шаблонов.
	 */
	public SkillTemplate[] getSkills(int classId, int templateId)
	{
		Table<IntKey, SkillTemplate[]> table = allskills.get(classId);

		if(table == null)
			return null;

		return table.get(templateId);
	}

	/**
	 * Перезагрузка шаблонов скилов.
	 */
	public synchronized void reload()
	{
		// получаем файлы
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/skills"));

		// перебираем
		for(File file : files)
		{
			if(file == null)
				continue;

			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// получаем отпарсенные шаблоны
			Array<SkillTemplate[]> parsed = new DocumentSkill(file).parse();

			// перебираем массиы шаблонов
			for(SkillTemplate[] temps : parsed)
			{
				// пустые пропускаем
				if(temps.length == 0)
					continue;

				// получаем первый в массиве
				SkillTemplate first = temps[0];

				// получаем таблицу шаблонов
				Table<IntKey, SkillTemplate[]> table = allskills.get(first.getClassId());

				// если ее нет, пропускаем
				if(table == null)
					continue;

				// получаем массив старых шаблонов
				SkillTemplate[] old = table.get(first.getId());

				// если их нету
				if(old == null)
					// вносим новые
					table.put(first.getId(), temps);
				else
				{
					// перебираем старые шаблоны
					for(int i = 0; i < old.length; i++)
					{
						try
						{
							SkillTemplate oldSkill = old[i];
							SkillTemplate newSkill = temps[i];

							oldSkill.reload(newSkill);
						}
						catch(Exception e)
						{
							log.warning(e);
						}
					}
				}
			}
		}

		log.info("skills reloaded.");
	}
}
