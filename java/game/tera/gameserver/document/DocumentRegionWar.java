package tera.gameserver.document;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.Config;
import tera.gameserver.events.global.regionwars.Region;
import tera.gameserver.events.global.regionwars.RegionWars;
import tera.gameserver.model.npc.spawn.Spawn;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.model.territory.RegionTerritory;
import tera.gameserver.parser.FuncParser;
import tera.gameserver.tables.TerritoryTable;

/**
 * Парсер территорий с xml.
 *
 * @author Ronn
 * @created 09.03.2012
 */
public final class DocumentRegionWar extends AbstractDocument<Array<Region>>
{
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

	/** ивент владелец регионов */
	private RegionWars event;

	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentRegionWar(File file, RegionWars event)
	{
		super(file);

		this.event = event;
	}

	@Override
	protected Array<Region> create()
	{
		return Arrays.toArray(Region.class);
	}

	@Override
	protected void parse(Document doc)
	{
		// получаем таблицу территорий
		TerritoryTable territoryTable = TerritoryTable.getInstance();

		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node node = list.getFirstChild(); node != null; node = node.getNextSibling())
					if("region".equals(node.getNodeName()))
					{
						// парси атрибуты
						VarTable vars = VarTable.newInstance(node);

						// парсим ид территории
						int id = vars.getInteger("id");

						// получаем территорию
						RegionTerritory territory = (RegionTerritory) territoryTable.getTerritory(id);

						// если территории нету, пропускаем регион
						if(territory == null)
						{
							log.warning(this, "not found territory for " + id);
							continue;
						}

						// создаем регион
						Region region = new Region(event, territory);

						// устанавливаем интервал
						region.setInterval(vars.getLong("interval") * 60 * 60 * 1000);

						// устанавливаем время битвы
						region.setBattleTime(vars.getLong("battleTime") * 60 * 1000);

						// устанавливаем точку отсчета
						try
						{
							region.setStartTime(DATE_FORMAT.parse(vars.getString("startTime")).getTime());
						}
						catch(ParseException e)
						{
							log.warning(this, e);
						}

						// устанавливаем налог
						region.setTax(vars.getInteger("tax"));

						for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
						{
							if(child.getNodeType() != Node.ELEMENT_NODE)
								continue;

							String name = child.getNodeName();

							switch(name)
							{
								case "spawns": parseSpawns(region, child); break;
							}
						}

						// парсим функции
						parseFuncs(region, node);

						// вносим в список
						result.add(region);
					}
	}

	private void parseFuncs(Region region, Node node)
	{
		Array<Func> positive = Arrays.toArray(Func.class);
		Array<Func> negative = Arrays.toArray(Func.class);

		// получаем парсер функций
		FuncParser parser = FuncParser.getInstance();

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if("positive".equals(child.getNodeName()))
				parser.parse(child, positive, file);
			else if("negative".equals(child.getNodeName()))
				parser.parse(child, negative, file);
		}

		positive.trimToSize();
		negative.trimToSize();

		region.setNegative(negative.array());
		region.setPositive(positive.array());
	}

	private void parseSpawns(Region region, Node node)
	{
		Array<Spawn> defense = Arrays.toArray(Spawn.class);
		Array<Spawn> barriers = Arrays.toArray(Spawn.class);
		Array<Spawn> control = Arrays.toArray(Spawn.class);
		Array<Spawn> manager = Arrays.toArray(Spawn.class);
		Array<Spawn> shops = Arrays.toArray(Spawn.class);

		VarTable vars = VarTable.newInstance();

		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE || !"spawn".equals(child.getNodeName()))
				continue;

			vars.parse(child);

			File file = new File(Config.SERVER_DIR + vars.getString("filepath"));

			switch(vars.getString("name"))
			{
				case "control": control.addAll(new DocumentNpcSpawn(file).parse()); break;
				case "defense": defense.addAll(new DocumentNpcSpawn(file).parse()); break;
				case "barriers": barriers.addAll(new DocumentNpcSpawn(file).parse()); break;
				case "manager": manager.addAll(new DocumentNpcSpawn(file).parse()); break;
				case "shops": shops.addAll(new DocumentNpcSpawn(file).parse()); break;
			}
		}

		defense.trimToSize();
		control.trimToSize();
		manager.trimToSize();
		barriers.trimToSize();
		shops.trimToSize();

		region.setControl(control.array());
		region.setDefense(defense.array());
		region.setManager(manager.array());
		region.setBarriers(barriers.array());
		region.setShops(shops.array());
	}
}