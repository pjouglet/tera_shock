package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import rlib.util.table.Table;
import rlib.util.table.Tables;

import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.parser.ConditionParser;
import tera.gameserver.parser.FuncParser;
import tera.gameserver.parser.EffectParser;
import tera.gameserver.templates.EffectTemplate;
import tera.gameserver.templates.SkillTemplate;

/**
 * Парсер скилов c хмл.
 *
 * @author Ronn
 */
public final class DocumentSkill extends AbstractDocument<Array<SkillTemplate[]>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentSkill(File file)
	{
		super(file);
	}

	@Override
	protected Array<SkillTemplate[]> create()
	{
		return result = Arrays.toArray(SkillTemplate[].class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node skill = list.getFirstChild(); skill != null; skill = skill.getNextSibling())
					if("skill".equals(skill.getNodeName()))
						try
						{
							result.add(parseSkill(skill));
						}
						catch(Exception e)
						{
							log.warning(this, "incorrect file " + file + ", and skill " + skill.getAttributes().getNamedItem("id"));
							log.warning(this, e);
						}
	}

	/**
	 * @param node данные с хмл.
	 * @return новый скил.
	 */
	private SkillTemplate[] parseSkill(Node node)
	{
		// заготавливаем массив if,kjyjd
		SkillTemplate[] skills;

		// создаем таблицу таблиц статов
		Table<String, String[]> table = Tables.newObjectTable();

		// соpдаем массив эффетков
		Array<EffectTemplate> effects = Arrays.toArray(EffectTemplate.class);

		// создаем массив пассивных функций
		Array<Func> passiveFuncs = Arrays.toArray(Func.class);

		// создаепм массив активных функций
		Array<Func> castFuncs = Arrays.toArray(Func.class);

		// подготавливаем условие
		Condition condition = null;

		// получаем атрибуты скила
		VarTable attrs = VarTable.newInstance(node);

		// получаем ид, уровни и класс скила
		int id = attrs.getInteger("id");
		int levels = attrs.getInteger("levels");
		int classId = attrs.getInteger("class");

		// получаем имя скила
		String skillName = attrs.getString("name");

		// вынимаем табличные параметры
		for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
		{
			if(child.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// ищем таблицу
			if("table".equals(child.getNodeName()))
			{
				Node item = child.getAttributes().getNamedItem("name");

				if(item == null)
					continue;

				// получаем название таблицы
				String name = item.getNodeValue();

				if(name == null)
					continue;

				// вынимаем значения
				Node values = child.getFirstChild();

				if(values == null)
					continue;

				String value = values.getNodeValue();

				if(value == null)
					continue;

				// вставляем в карту
				table.put(name, value.split(" "));
			}
		}

		// создаем массив скилов в соотвтествии с уровнями
		skills = new SkillTemplate[levels];

		VarTable vars = VarTable.newInstance();

		// получаем парсер условий
		ConditionParser condParser = ConditionParser.getInstance();

		// получаем парсер эффектов
		EffectParser effectParser = EffectParser.getInstance();

		// получаем парсер функций статов
		FuncParser funcParser = FuncParser.getInstance();

		// идем по массиву
		for(int order = 0; order < levels; order++)
		{
			// создаем таблицу статов
			VarTable stats = VarTable.newInstance();

			// вставяем туда ид, левел, классИд, имя
			stats.set("id", (id + order));
			stats.set("level", 1 + order);
			stats.set("classId", classId);
			stats.set("name", skillName);

			// очищаем функции и эффекты
			passiveFuncs.clear();
			castFuncs.clear();
			effects.clear();

			// нулим условие
			condition = null;

			for(Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
			{
				// если это не узел, пропускаем
				if(child.getNodeType() != Node.ELEMENT_NODE)
					continue;

				// находим параметр скила
				if("set".equals(child.getNodeName()))
				{
					// парсим атрибуты
					vars.parse(child);

					// получаем имя параметра
					String name = vars.getString("name");
					// получаем значение параметра
					String value = vars.getString("value");

					// если значение табличное, вытягиваем с таблицы
					if(value.startsWith("#"))
					{
						// получаем массив значений
						String[] array = table.get(value);
						// применяем значение из таблицы
						value = array[Math.min(array.length -1, order)];
					}

					// вставляем в таблицу статов параметр
					stats.set(name, value);
				}
				// находим условия скила
				else if("cond".equals(child.getNodeName()))
				{
					// перебираем условия
					for(Node condNode = child.getFirstChild(); condNode != null; condNode = condNode.getNextSibling())
					{
						// если это не узел, пропускаем
						if(condNode.getNodeType() != Node.ELEMENT_NODE)
							continue;

						// пробуем отпарсить
						Condition cond = condParser.parseCondition(condNode, id + order, file);

						// если не получилось, сообщаем
						if(cond == null)
						{
							log.warning(this, new Exception("not found condition"));
							continue;
						}

						// объеденяем условия
						condition = condParser.joinAnd(condition, cond);
					}
				}
				else if("cast".equals(child.getNodeName()))
					// парсим активные функции
					funcParser.parse(child, castFuncs, table, order, id + order, file);
				// находим добавление к скилу
				else if("for".equals(child.getNodeName()))
				{
					// парсим пассивные функции
					funcParser.parse(child, passiveFuncs, table, order, id + order, file);

					// перебираем добавки
					for(Node added = child.getFirstChild(); added != null; added = added.getNextSibling())
					{
						// если это не узел, пропускаем
						if(added.getNodeType() != Node.ELEMENT_NODE)
							continue;

						// если это является эффектом
						if("effect".equals(added.getNodeName()))
						{
							EffectTemplate effect = effectParser.paraseEffects(order, added, table, id + order, file);

							// если его не пропарсили, пропускаем
							if(effect == null)
							{
								log.warning(this, "not found effect to name " + added.getNodeName() + " on file" + file + ".");
								continue;
							}

							// добавляем в список
							effects.add(effect);
						}
					}
				}
			}

			effects.trimToSize();
			passiveFuncs.trimToSize();
			castFuncs.trimToSize();

			// создаем шаблон и кладем в массив
			skills[order] = new SkillTemplate(stats, Arrays.copyOf(effects.array(), 0), condition, Arrays.copyOf(passiveFuncs.array(), 0), Arrays.copyOf(castFuncs.array(), 0));
		}

		return skills;
	}
}
