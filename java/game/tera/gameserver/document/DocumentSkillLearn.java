package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.SkillLearn;

/**
 * Парсер изучений скилов с xml.
 *
 * @author Ronn
 */
public class DocumentSkillLearn extends AbstractDocument<Array<SkillLearn>>
{
	public DocumentSkillLearn(File file)
	{
		super(file);
	}

	@Override
	protected Array<SkillLearn> create()
	{
		return Arrays.toArray(SkillLearn.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node playerClass = list.getFirstChild(); playerClass != null; playerClass = playerClass.getNextSibling())
					if("class".equals(playerClass.getNodeName()))
						parseClass(playerClass);
	}

	/**
	 * @param node структура скилов класса.
	 */
	private void parseClass(Node node)
	{
		// получаем атрибуты класса
		VarTable vars = VarTable.newInstance(node);

		// получаем ид класса
		int classId = vars.getInteger("id");

		// перебираем доступные скилы классу
		for(Node skills = node.getFirstChild(); skills != null; skills = skills.getNextSibling())
			if("skill".equals(skills.getNodeName()))
			{
				// получаем атрибуты скила
				vars.parse(skills);

				// получаем ид скила
				int id = vars.getInteger("id");
				// получаем минимальный ур. скила
				int minLevel = vars.getInteger("minLevel");
				// получаем цену скила
				int price = vars.getInteger("price");

				// флаг пассивности/активности
				boolean passive = vars.getBoolean("passive", false);

				// создаем изучающийся скил
				SkillLearn current = new SkillLearn(id, price, 0, minLevel, classId, passive);

				// добавляем в результат
				result.add(current);

				// перебираем прокачку скила
				for(Node next = skills.getFirstChild(); next != null; next = next.getNextSibling())
					if("next".equals(next.getNodeName()))
					{
						// получаем атрибуты след. уровня скила
						vars.parse(next);

						// получаем ид скила
						id = vars.getInteger("id");

						// мин ур.
						minLevel = vars.getInteger("minLevel");

						// цену
						price = vars.getInteger("price");

						// создаем новый ур. скила
						current = new SkillLearn(id, price, current.getId(), minLevel, classId, passive);

						// добавляем в результат
						result.add(current);
					}
			}
	}
}
