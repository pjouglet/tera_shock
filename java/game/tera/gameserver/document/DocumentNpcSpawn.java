package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.NpcAIClass;
import tera.gameserver.model.npc.NpcType;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.tables.ConfigAITable;
import tera.gameserver.tables.NpcTable;
import tera.gameserver.templates.NpcTemplate;
import tera.util.Location;

/**
 * Парсер спавнов нпс с xml.
 *
 * @author Ronn
 * @created 16.03.2012
 */
public final class DocumentNpcSpawn extends AbstractDocument<Array<Spawn>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentNpcSpawn(File file)
	{
		super(file);
	}

	@Override
	protected Array<Spawn> create()
	{
		return result = Arrays.toArray(Spawn.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node node = list.getFirstChild(); node != null; node = node.getNextSibling())
					if("npc".equals(node.getNodeName()))
						parseSpawns(node);
	}

	/**
	 * @param node данные с хмл.
	 * @param template темплейт нпс.
	 * @param continentId ид континента.
	 */
	private final void parseAiSpawns(Node node, NpcTemplate template, int continentId)
	{
		// получаем атрибуты спавна
		VarTable vars = VarTable.newInstance(node);

		// получаем таблицу конфигов АИ
		ConfigAITable configTable = ConfigAITable.getInstance();

		// получаем класс АИ
        NpcAIClass aiClass = vars.getEnum("class", NpcAIClass.class);

        // получаем конфиг АИ
        ConfigAI config = configTable.getConfig(vars.getString("config"));

        if(config == null)
        {
        	log.warning(this, "not found config AI " + vars.getString("config") + " in file " + file);
        	return;
        }

        // парсим сами спавны
        for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
        {
        	if(child.getNodeType() != Node.ELEMENT_NODE)
        		continue;

        	if("time".equals(child.getNodeName()))
        		parseTimeSpawns(child, template, aiClass, config, continentId);
        }
	}

	/**
	 * @param node узел данных с хмл.
	 */
	private final void parseSpawns(Node node)
	{
		// получаем атрибуты спавна
		VarTable vars = VarTable.newInstance(node);

		// получаем нпс ид спавна
		int npcId = vars.getInteger("id");
		int type = vars.getInteger("type");
		int continentId = vars.getInteger("continentId", 0);

		// получаем таблицу НПС
		NpcTable npcTable = NpcTable.getInstance();

		// получаем темплейт нпс
		NpcTemplate template = npcTable.getTemplate(npcId, type);

		// если его нет, выходим
		if(template == null)
		{
			log.warning(this, new Exception("not found npc template for id " + npcId + ", type " + type));
			return;
		}

		// перебираем внутренние элементы в поисках АИ
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			if(child.getNodeType() == Node.ELEMENT_NODE && "ai".equals(child.getNodeName()))
				parseAiSpawns(child, template, continentId);
	}

	/**
	 * @param node данные с хмл.
	 * @param template темплейт нпс.
	 * @param aiClass тип аи.
	 * @param continentId ид континента.
	 * @param config конфиг аи.
	 * @param respawn время респавна.
	 * @param randomRespawn рандоминайзер респавна.
	 */
	private final void parseSpawns(Node node, NpcTemplate template, NpcAIClass aiClass, ConfigAI config, int continentId, int respawn, int randomRespawn)
	{
		// получаем атрибуты спавна
		VarTable vars = VarTable.newInstance(node);

		// получаем координаты спавна
		float x = vars.getFloat("x");
		float y = vars.getFloat("y");
		float z = vars.getFloat("z");

		// получаем разворот нпс
		int heading =  vars.getInteger("heading", -1);
		// получаем минимальный радиус спавна от тояки
		int min = vars.getInteger("min", 0);
		// получаем максимальный радиус спавна от точки
		int max = vars.getInteger("max", 0);

		// получаем тип НПС
		NpcType type = template.getNpcType();

		// добавляем в массив спавнов новый спавн
		result.add(type.newSpawn(node, vars, template, new Location(x, y, z, heading, continentId), respawn, randomRespawn, min, max, config, aiClass));
	}

	/**
	 * @param node параметры с хмл.
	 * @param template темплейт нпс.
	 * @param aiClass класс аи.
	 * @param config конфиг АИ.
	 * @param continentId ид континента.
	 */
	private final void parseTimeSpawns(Node node, NpcTemplate template, NpcAIClass aiClass, ConfigAI config, int continentId)
	{
		// получаем таблицу атрибутов
		VarTable vars = VarTable.newInstance(node);

		// получаем время респа
		int respawn = vars.getInteger("respawn", -1);
		// рандомная прибавка к респу
		int random = vars.getInteger("random", -1);

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if("point".equals(child.getNodeName()))
				parseSpawns(child, template, aiClass, config, continentId, respawn, random);
		}
	}
}
