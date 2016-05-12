package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.resourse.ResourseSpawn;
import tera.gameserver.tables.ResourseTable;
import tera.gameserver.templates.ResourseTemplate;
import tera.util.Location;

/**
 * Парсер спавнов с xml.
 *
 * @author Ronn
 * @created 16.03.2012
 */
public final class DocumentResourseSpawn extends AbstractDocument<Array<ResourseSpawn>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentResourseSpawn(File file)
	{
		super(file);
	}

	@Override
	protected Array<ResourseSpawn> create()
	{
		return result = Arrays.toArray(ResourseSpawn.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node spawns = list.getFirstChild(); spawns != null; spawns = spawns.getNextSibling())
					if("resourse".equals(spawns.getNodeName()))
						parseSpawns(spawns);
	}

	/**
	 * @param node узел данных с хмл.
	 */
	private final void parseSpawns(Node node)
	{
		// получаем атрибуты спавна
		VarTable vars = VarTable.newInstance(node);

		// получаем ид темплейта ресурса
		int templateId = vars.getInteger("templateId");

		// получаем таблицу ресурсов
		ResourseTable resourseTable = ResourseTable.getInstance();

		// получаем темплейт ресурса
		ResourseTemplate template = resourseTable.getTemplate(templateId);

		// если его нет, выходим
		if(template == null)
		{
			log.warning(this, new Exception("not found resourse template for templateId " + templateId));
			return;
		}

		// перебираем внутренние элементы в поисках времени спавна
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if("time".equals(child.getNodeName()))
				parseTimeSpawns(child, template);
		}
	}

	/**
	 * @param node данные с хмл.
	 * @param template темплейт нпс.
	 * @param continentId ид континента.
	 * @param respawn время респавна.
	 * @param randomRespawn рандоминайзер респавна.
	 */
	private final void parseSpawns(Node node, ResourseTemplate template, int continentId, int respawn, int randomRespawn)
	{
		// получаем атрибуты спавна
		VarTable vars = VarTable.newInstance(node);

		// получаем координаты спавна
		float x = vars.getFloat("x");
		float y = vars.getFloat("y");
		float z = vars.getFloat("z");

		// получаем минимальный радиус спавна от тояки
		int min = vars.getInteger("min", 0);
		// получаем максимальный радиус спавна от точки
		int max = vars.getInteger("max", 0);

		// добавляем в массив спавнов новый спавн
		result.add(new ResourseSpawn(template, new Location(x, y, z, 0, continentId), respawn, randomRespawn, min, max));
	}

	/**
	 * @param node параметры с хмл.
	 * @param template темплейт ресурса.
	 */
	private final void parseTimeSpawns(Node node, ResourseTemplate template)
	{
		// получаем таблицу атрибутов
		VarTable vars = VarTable.newInstance(node);

		// получаем время респа
		int respawn = vars.getInteger("respawn", -1);
		// рандомная прибавка к респу
		int random = vars.getInteger("random", -1);
		// ид континента
		int continentId = vars.getInteger("continentId", 0);

		// перебираем все точки
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			if("point".equals(child.getNodeName()))
				parseSpawns(child, template, continentId, respawn, random);
		}
	}
}
