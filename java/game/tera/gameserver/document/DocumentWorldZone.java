package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.WorldZone;
import tera.util.Location;

/**
 * Парсер world zone с xml.
 *
 * @author Ronn
 * @created 08.03.2012
 */
public final class DocumentWorldZone extends AbstractDocument<Array<WorldZone>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentWorldZone(File file)
	{
		super(file);
	}

	@Override
	protected Array<WorldZone> create()
	{
		return Arrays.toArray(WorldZone.class);
	}

	@Override
	protected void parse(Document arg0)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node npc = lst.getFirstChild(); npc != null; npc = npc.getNextSibling())
					if("zone".equals(npc.getNodeName()))
					{
						// парсим зону
						WorldZone zone = parseZone(npc);

						// если зоны нет, пропускаем
						if(zone == null)
						{
							log.warning(this, new Exception("not found zone"));
							continue;
						}

						// добавляем зону
						result.add(zone);
					}
	}

	/**
	 * Парс точек описывающих территорию зоны.
	 *
	 * @param node узел с хмл.
	 * @param zone зона, которой точки парсим.
	 */
	private final void parsePoints(Node node, WorldZone zone)
	{
		// создаем парсер
		VarTable vars = VarTable.newInstance();

		// перебираем точки
		for(Node point = node.getFirstChild(); point != null; point = point.getNextSibling())
			if(point.getNodeType() == Node.ELEMENT_NODE && "point".equals(point.getNodeName()))
			{
				// парсим атрибуты
				vars.parse(point);

				// добавляем точку
				zone.addPoint(vars.getInteger("x"), vars.getInteger("y"));
			}
	}

	/**
	 * Парс точек респавна убитых игроков в указанной зоне.
	 *
	 * @param node узел с хмл.
	 * @param zone зона, которой точки парсим.
	 */
	private final void parseRespawns(Node node, WorldZone zone)
	{
		// создаем парсер
		VarTable vars = VarTable.newInstance();

		// перебираем точки
		for(Node respawn = node.getFirstChild(); respawn != null; respawn = respawn.getNextSibling())
			if(respawn.getNodeType() == Node.ELEMENT_NODE && "respawn".equals(respawn.getNodeName()))
			{
				// парсим атрибуты
				vars.parse(respawn);

				// добавляем точку респа
				zone.addRespawnPoint(new Location(vars.getFloat("x"), vars.getFloat("y"), vars.getFloat("z"), 0, zone.getContinentId()));
			}
	}

	/**
	 * Парс зоны
	 *
	 * @param node узел с хмл.
	 * @return новая зона.
	 */
	private final WorldZone parseZone(Node node)
	{
		// парсим атрибуты
		VarTable vars = VarTable.newInstance(node);

		// создаем зону
		WorldZone zone = new WorldZone(vars.getInteger("maxZ"), vars.getInteger("minZ"), vars.getInteger("zoneId"), vars.getInteger("continentId", 0));

		// заполняем точками респа и зоны
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;
			else if("points".equals(child.getNodeName()))
				parsePoints(child, zone);
			else if("respawns".equals(child.getNodeName()))
				parseRespawns(child, zone);

		return zone;
	}
}
