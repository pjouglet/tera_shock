package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.MinionData;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.spawn.MinionSpawn;
import tera.gameserver.tables.ConfigAITable;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.templates.NpcTemplate;

/**
 * Парсер спавна минионов с xml.
 *
 * @author Ronn
 * @created 14.03.2012
 */
public final class DocumentMinion extends AbstractDocument<Array<MinionData>>
{
	public DocumentMinion(File file)
	{
		super(file);
	}

	@Override
	protected Array<MinionData> create()
	{
		return Arrays.toArray(MinionData.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node npc = lst.getFirstChild(); npc != null; npc = npc.getNextSibling())
					if("npc".equals(npc.getNodeName()))
					{
						MinionData data = parseMinionData(npc);

						if(data == null)
							continue;

						result.add(data);
					}
	}

	/**
	 * Парс данных о минионах.
	 *
	 * @param nodes данные с хмл.
	 * @return данные о минионах.
	 */
	private final MinionData parseMinionData(Node nodes)
	{
		// получаем атрибуты минионов
		VarTable vals = VarTable.newInstance(nodes);

		// получаем нпс ид лидера минионов и время респавна их
		int leaderId = vals.getInteger("id");
		int leaderType = vals.getInteger("type");
		int respawn = vals.getInteger("respawn");

		// получаем таблицу НПС
		NpcTable npcTable = NpcTable.getInstance();

		// получаем таблицу настроек АИ
		ConfigAITable configTable = ConfigAITable.getInstance();
		
		// получаем темплейт лидера
		NpcTemplate leaderTemplate = npcTable.getTemplate(leaderId, leaderType);

		//если его нет, выходим
		if(leaderTemplate == null)
		{
			log.warning(this, "not found leader id " + leaderId + ", type " + leaderType);
			return null;
		}

		// создаем контейнер спавнов минионов
		Array<MinionSpawn> spawns = Arrays.toArray(MinionSpawn.class);

		// создаем парсер атрибутов
		VarTable vars = VarTable.newInstance();

		for(Node node = nodes.getFirstChild(); node != null; node = node.getNextSibling())
			if(node.getNodeType() == Node.ELEMENT_NODE && "minion".equals(node.getNodeName()))
			{
				// парсим атрибуты
				vars.parse(node);

				// получаем шаблон нпс
				NpcTemplate template = npcTable.getTemplate(vars.getInteger("id"), vars.getInteger("type"));

				// если не нашли, пропусакаем
				if(template == null)
					continue;

				// получаем конфиг АИ
				ConfigAI config = configTable.getConfig(vars.getString("aiConfig"));
				
				// если нет, пропускаем
				if(config == null)
				{
					log.warning(this, "not found ai config for " + vars.getString("aiConfig"));
					continue;
				}
				
				// получаем класс АИ
				NpcAIClass aiClass = vars.getEnum("aiClass", NpcAIClass.class);
				
				// создаем спавн
				MinionSpawn spawn = new MinionSpawn(template);
				
				// устанавливаем кол-во минионов этого типа
				spawn.setCount(vars.getInteger("count", 1));
				// устанавливаем радиус спавна от лидера
				spawn.setRadius(vars.getInteger("radius", 60));
				// устанавливаем АИ конфиг
				spawn.setConfig(config);
				//устанавливаем тип АИ
				spawn.setAiClass(aiClass);

				//добавляем в массив минионов
				spawns.add(spawn);
			}

		// если минионов не нашли, выходим
		if(spawns.isEmpty())
			return null;

		spawns.trimToSize();

		// создаем инфу о минионах по массиву минионах
		return new MinionData(spawns.array(), leaderId, leaderType, respawn);
	}
}
