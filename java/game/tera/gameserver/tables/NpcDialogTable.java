package tera.gameserver.tables;

import java.io.File;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.Files;
import rlib.util.Objects;
import rlib.util.array.Arrays;
import rlib.util.table.IntKey;
import rlib.util.table.Table;
import rlib.util.table.Tables;
import tera.Config;
import tera.gameserver.document.DocumentDialog;
import tera.gameserver.model.npc.interaction.DialogData;
import tera.gameserver.model.npc.interaction.Link;

/**
 * Таблица данных о диалогов нпс.
 *
 * @author Ronn
 */
public final class NpcDialogTable
{
	private static final Logger log = Loggers.getLogger(NpcDialogTable.class);

	private static NpcDialogTable instance;

	public static NpcDialogTable getInstance()
	{
		if(instance == null)
			instance=  new NpcDialogTable();

		return instance;
	}

	/** таблица всех диалогов */
	private Table<IntKey, Table<IntKey, DialogData>> dialogs;

	private NpcDialogTable()
	{
		// создаем таблицу
		dialogs = Tables.newIntegerTable();

		// получаем файлы иалогов
		File[] files = Files.getFiles(new File(Config.SERVER_DIR + "/data/dialogs"));

		// перебираем их
		for(File file : files)
		{
			// если это не хмл, пропускаем
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// если это пример, пропускаем
			if(file.getName().startsWith("example"))
				continue;

			// пармис фаил
			Table<IntKey, Table<IntKey, DialogData>> newDialogs = new DocumentDialog(file).parse();

			// если ничего не получили, пропускаем
			if(newDialogs == null)
				continue;

			// перебираем результат
			for(Table<IntKey, DialogData> table : newDialogs)
				for(DialogData dialog : table)
				{
					// получаем подтаблицу диалогов
					Table<IntKey, DialogData> current = dialogs.get(dialog.getNpcId());

					// если ее нет
					if(current == null)
					{
						// встовляем отпарсенную
						dialogs.put(dialog.getNpcId(), table);
						continue;
					}

					// получаем старый диалог
					DialogData old = current.get(dialog.getType());

					// если он есть
					if(old != null)
					{
						// добавляем к его ссылкам
						old.setLinks(Arrays.combine(old.getLinks(), dialog.getLinks(), Link.class));
						continue;
					}

					// вставляем новый диалог
					current.put(dialog.getType(), dialog);
				}

		}

		log.info("loaded " + dialogs.size() + " dialogs.");
	}

	/**
	 * Получение даных диалога по нпс ид.
	 *
	 * @param npcId нпс ид.
	 * @param type тип нпс.
	 * @return данные о диалоге для указанного нпс ид.
	 */
	public DialogData getDialog(int npcId, int type)
	{
		// получаем под таблицу диалогов
		Table<IntKey, DialogData> table = dialogs.get(npcId);

		// если ее нет, вохвращаем пустоту, иначе смотрим по подтаблице
		return table == null? null : table.get(type);
	}

	/**
	 * Перезагрузка таблицы.
	 */
	public synchronized void reload()
	{
		// получаем файлы иалогов
		File[] files = Files.getFiles(new File("./data/dialogs"));

		// перебираем их
		for(File file : files)
		{
			// если это не хмл, пропускаем
			if(!file.getName().endsWith(".xml"))
			{
				log.warning("detected once the file " + file.getAbsolutePath());
				continue;
			}

			// если это пример, пропускаем
			if(file.getName().startsWith("example"))
				continue;

			// пармис фаил
			Table<IntKey, Table<IntKey, DialogData>> newDialogs = new DocumentDialog(file).parse();

			// если ничего не получили, пропускаем
			if(newDialogs == null)
				continue;

			// перебираем результат
			for(Table<IntKey, DialogData> table : newDialogs)
				for(DialogData dialog : table)
				{
					// получаем подтаблицу диалогов
					Table<IntKey, DialogData> current = dialogs.get(dialog.getNpcId());

					// если ее нет
					if(current == null)
					{
						// встовляем отпарсенную
						dialogs.put(dialog.getNpcId(), table);
						continue;
					}

					// получаем старый диалог
					DialogData old = current.get(dialog.getType());

					// если он есть
					if(old != null)
						Objects.reload(old, dialog);
				}

		}

		log.info("reloaded.");
	}
}
