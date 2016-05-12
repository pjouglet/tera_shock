package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.Config;
import tera.gameserver.document.DocumentNpc;
import tera.gameserver.model.drop.NpcDrop;
import tera.gameserver.templates.NpcTemplate;

/**
 * Таблица шаблонов НПС.
 *
 * @author Ronn
 * @created 05.03.2012
 */
public final class NpcTable
{
	private static final Logger log = Loggers.getLogger(NpcTable.class);

	/** класс ид для скилов нпс */
	public static final byte NPC_SKILL_CLASS_ID = -8;

	private static NpcTable instance;

	public static NpcTable getInstance()
	{
		if(instance == null)
			 instance = new NpcTable();

		return instance;
	}

	/** таблица шаблонов */
	private Table<IntKey, Table<IntKey, NpcTemplate>> templates;

	private NpcTable()
	{
		templates = Tables.newIntegerTable();

		// получаем таблицу дропа
		DropTable dropTable = DropTable.getInstance();

		// получаем таблицу диалогов
		NpcDialogTable dialogTable = NpcDialogTable.getInstance();

		Array<NpcTemplate> array = Arrays.toArray(NpcTemplate.class);

		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/npcs"));

		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// парсим файл
			Array<NpcTemplate> parsed = new DocumentNpc(file).parse();

			// перебираем отпарсенные шаблоны
			for(NpcTemplate template : parsed)
			{
				// получаем дроп лист этого НПС
				NpcDrop drop = dropTable.getNpcDrop(template.getTemplateId(), template.getTemplateType());

				// применяем дроп лист
				template.setCanDrop(drop != null);
				template.setDrop(drop);

				// применяем диалог этого НПС
				template.setDialog(dialogTable.getDialog(template.getTemplateId(), template.getTemplateType()));

				// получаем подтаблицу для этого шаблона
				Table<IntKey, NpcTemplate> table = templates.get(template.getTemplateId());

				// если ее нет
				if(table == null)
				{
					// создаем новую
					table = Tables.newIntegerTable();

					// вносим в главную таблицу
					templates.put(template.getTemplateId(), table);
				}

				// проверяем на дублирование
				if(table.containsKey(template.getTemplateType()))
				{
					log.warning("found duplicate npc template " + template + " in file " + file);
					continue;
				}

				// вносим в таблицу
				table.put(template.getTemplateType(), template);

				// добавляем в список
				array.add(template);
			}
		}

		log.info("loaded  " + templates.size() + " npcs.");

		/*try(PrintWriter out = new PrintWriter(new File("./data/npcs.xml")))
		{
			out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			out.println("<list>");

			for(NpcTemplate template : array)
			{
				out.println("	<template id=\"" + template.getTemplateId() + "\" name=\"" + template.getTemplateId() +
						"\" class=\"" + template.getNpcType() + "\" type=\"" + template.getTemplateType() + "\" level=\"1\" exp=\"" +
						template.getExp() + "\" attack=\"1\" defense=\"1\" impact=\"1\" balance=\"1\" maxHp=\"1\" maxMp=\"1\" atkSpd=\"120\" runSpd=\"100\" skills=\"\" />");
			}

			out.println("</list>");
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}*/
	}

	/**
	 * Получение нпс шаблона по ид.
	 *
	 * @param templateId ид шаблона.
	 * @param templateType тип шаблона.
	 * @return шаблона.
	 */
	public NpcTemplate getTemplate(int templateId, int templateType)
	{
		Table<IntKey, NpcTemplate> table = templates.get(templateId);

		return table == null? null : table.get(templateType);
	}

	/**
	 * Перезагрузка нпс темплейтов.
	 */
	public synchronized void reload()
	{
		// получаем таблицу дропа
		DropTable dropTable = DropTable.getInstance();

		// получаем таблицу диалогов
		NpcDialogTable dialogTable = NpcDialogTable.getInstance();

		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/npcs"));

		for(File file : files)
		{
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// парсим файл
			Array<NpcTemplate> parsed = new DocumentNpc(file).parse();

			// перебираем отпарсенные шаблоны
			for(NpcTemplate template : parsed)
			{
				// получаем дроп лист этого НПС
				NpcDrop drop = dropTable.getNpcDrop(template.getTemplateId(), template.getTemplateType());

				// применяем дроп лист
				template.setCanDrop(drop != null);
				template.setDrop(drop);

				// применяем диалог этого НПС
				template.setDialog(dialogTable.getDialog(template.getTemplateId(), template.getTemplateType()));

				// получаем подтаблицу для этого шаблона
				Table<IntKey, NpcTemplate> table = templates.get(template.getTemplateId());

				// если ее нет
				if(table == null)
				{
					// создаем новую
					table = Tables.newIntegerTable();

					// вносим в главную таблицу
					templates.put(template.getTemplateId(), table);
				}

				// получаем старый шаблон
				NpcTemplate old = table.get(template.getTemplateType());

				// если он есть
				if(old != null)
					// обновляем
					old.reload(template);
				else
					// вносим новый
					table.put(template.getTemplateType(), template);
			}
		}

		log.info("npcs reloaded.");
	}
}