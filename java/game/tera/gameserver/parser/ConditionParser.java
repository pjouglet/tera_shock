package tera.gameserver.parser;

import java.io.File;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.items.ArmorType;
import tera.gameserver.model.items.WeaponType;
import tera.gameserver.model.npc.NpcType;
import tera.gameserver.model.skillengine.Condition;
import tera.gameserver.model.skillengine.SkillName;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerAggroMe;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerEffectorEffectId;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerNpcRage;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerNpcType;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerNpcTypes;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerOwerturned;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerPvP;
import tera.gameserver.model.skillengine.conditions.ConditionAttackerSide;
import tera.gameserver.model.skillengine.conditions.ConditionHasEffectId;
import tera.gameserver.model.skillengine.conditions.ConditionLogicAnd;
import tera.gameserver.model.skillengine.conditions.ConditionLogicNot;
import tera.gameserver.model.skillengine.conditions.ConditionLogicOr;
import tera.gameserver.model.skillengine.conditions.ConditionPlayerBattleStance;
import tera.gameserver.model.skillengine.conditions.ConditionPlayerCastSkillName;
import tera.gameserver.model.skillengine.conditions.ConditionPlayerNotBarrier;
import tera.gameserver.model.skillengine.conditions.ConditionPlayerOnCast;
import tera.gameserver.model.skillengine.conditions.ConditionPlayerOwerturned;
import tera.gameserver.model.skillengine.conditions.ConditionPlayerPercentHP;
import tera.gameserver.model.skillengine.conditions.ConditionPlayerStamina;
import tera.gameserver.model.skillengine.conditions.ConditionTargetAggroMe;
import tera.gameserver.model.skillengine.conditions.ConditionTargetNpcRage;
import tera.gameserver.model.skillengine.conditions.ConditionTargetNpcType;
import tera.gameserver.model.skillengine.conditions.ConditionTargetNpcTypes;
import tera.gameserver.model.skillengine.conditions.ConditionTargetOwerturned;
import tera.gameserver.model.skillengine.conditions.ConditionTargetPlayer;
import tera.gameserver.model.skillengine.conditions.ConditionTargetSide;
import tera.gameserver.model.skillengine.conditions.ConditionUsingItem;

/**
 * Парсер условий.
 *
 * @author Ronn
 */
public final class ConditionParser
{
	private static final Logger log = Loggers.getLogger(ConditionParser.class);

	private static ConditionParser instance;

	public static ConditionParser getInstance()
	{
		if(instance == null)
			instance = new ConditionParser();

		return instance;
	}

	private ConditionParser()
	{
		log.info("initializable.");
	}

	/**
	 * Объединение 2х условий в 1.
	 */
	public Condition joinAnd(Condition first, Condition second)
	{
		// если объединятеля нету, создаем
		if(first == null)
			first = new ConditionLogicAnd();

		// добавляем новый кондишен
		((ConditionLogicAnd) first).add(second);

		return first;
	}

	/**
	 * Парс условия по атакующему персонажу.
	 */
	private Condition parseAttackerCondition(Node node, int skillId, File file)
	{
		// новое условие
		Condition newCondition = null;

		// таблица атрибутов
		NamedNodeMap attrs = node.getAttributes();

		for(int index = 0; index < attrs.getLength(); index++)
		{
			// получаем текущий итем
			Node item = attrs.item(index);

			// получаем его название
			String nodeName = item.getNodeName();

			// получаем значение атрибута
			String nodeValue = item.getNodeValue();

			// получаем сообщение кондишена
			Node msg = attrs.getNamedItem("msg");

			// создаем соответсвующий кондишен
			switch(nodeName)
			{
				case "pvp":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionAttackerPvP(value).setMsg(msg));

					break;
				}
				case "player":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionAttackerPvP(value).setMsg(msg));

					break;
				}
				case "npcType":
				{
					newCondition = joinAnd(newCondition, new ConditionAttackerNpcType(NpcType.valueOf(nodeValue)).setMsg(msg));

					break;
				}
				case "npcRage":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionAttackerNpcRage(value).setMsg(msg));

					break;
				}
				case "npcTypes":
				{
					String[] strTypes = nodeValue.split(",");

					NpcType[] types = new NpcType[strTypes.length];

					for(int i = 0; i < strTypes.length; i++)
						types[i] = NpcType.valueOf(strTypes[i]);

					newCondition = joinAnd(newCondition, new ConditionAttackerNpcTypes(types)).setMsg(msg);

					break;
				}
				case "owerturned":
				{
					newCondition = joinAnd(newCondition, new ConditionAttackerOwerturned(Boolean.parseBoolean(nodeValue)).setMsg(msg));

					break;
				}
				case "effectorEffectId":
				{
					newCondition = joinAnd(newCondition, new ConditionAttackerEffectorEffectId(Integer.parseInt(nodeValue)).setMsg(msg));

					break;
				}
				case "aggroMe":
				{
					newCondition = joinAnd(newCondition, new ConditionAttackerAggroMe(Boolean.parseBoolean(nodeValue)).setMsg(msg));

					break;
				}
				case "side":
				{
					newCondition = joinAnd(newCondition, new ConditionAttackerSide(nodeValue).setMsg(msg));

					break;
				}
				default :
					log.warning("not found condition name " + nodeName);
			}
		}

		if(newCondition == null)
			log.warning("unrecognized <attacker> condition in skill " + skillId + " in file " + file);

		return newCondition;
	}

	/**
	 * Парс условия.
	 */
	public Condition parseCondition(Node node, int skill, File file)
	{
		if(node == null)
			return null;

		// итоговое условие
		Condition condition = null;

		switch(node.getNodeName())
		{
			case "and": condition = parseLogicAnd(node, skill, file); break;
			case "or": condition = parseLogicOr(node, skill, file); break;
			case "not": condition = parseLogicNot(node, skill, file); break;
			case "player": condition = parsePlayerCondition(node, skill, file); break;
			case "target": condition = parseTargetCondition(node, skill, file); break;
			case "has": condition = parseHasCondition(node, skill, file); break;
			case "using": condition = parseUsingCondition(node, skill, file); break;
			case "attacker": condition = parseAttackerCondition(node, skill, file); break;
		}

		return condition;
	}

	/**
	 * Парс условий на наличие чего-либо у персонажа.
	 */
	private Condition parseHasCondition(Node node, int skill, File file)
	{
		// новое условие
		Condition newCondition = null;

		// таблица атрибутов
		NamedNodeMap attrs = node.getAttributes();

		// сообщение кондишена
		Node msg = attrs.getNamedItem("msg");

		for(int index = 0; index < attrs.getLength(); index++)
		{
			// итем кондишена
			Node item = attrs.item(index);

			// тип условия
			String nodeName = item.getNodeName();
			// значение условия
			String nodeValue = item.getNodeValue();

			switch(nodeName)
			{
				case "effectId":
				{
					newCondition = joinAnd(newCondition, new ConditionHasEffectId(Integer.parseInt(nodeValue)).setMsg(msg));
					break;
				}
			}
		}

		if(newCondition == null)
			log.warning("unrecognized <has> condition in skill " + skill + " in file " + file);

		return newCondition;
	}

	/**
	 * Парс условий в теге <and>
	 */
	private Condition parseLogicAnd(Node node, int skillId, File file)
	{
		// объеденяющее условие
		ConditionLogicAnd condition = new ConditionLogicAnd();

		// перебираем все условия внутри тэга
		for(node = node.getFirstChild(); node != null; node = node.getNextSibling())
			if(node.getNodeType() == Node.ELEMENT_NODE)
				condition.add(parseCondition(node, skillId, file));

		// если тэг пуст на условия
		if(condition.isEmpty())
			log.warning("unrecognized <and> condition in skill " + skillId + " in file " + file);

		return condition;
	}

	/**
	 * Парс отрицания условия.
	 */
	private Condition parseLogicNot(Node node, int skill, File file)
	{
		for(node = node.getFirstChild(); node != null; node = node.getNextSibling())
			if(node.getNodeType() == Node.ELEMENT_NODE)
				return new ConditionLogicNot(parseCondition(node, skill, file));

		log.warning("empty <not> condition in skill " + skill + " in file " + file);

		return null;
	}

	/**
	 * Парс условий в теге <or>
	 */
	private Condition parseLogicOr(Node node, int skillId, File file)
	{
		// создаем объеденяющее условие
		ConditionLogicOr condition = new ConditionLogicOr();

		// перебираем все условия внутри тэга
		for(node = node.getFirstChild(); node != null; node = node.getNextSibling())
			if(node.getNodeType() == Node.ELEMENT_NODE)
				condition.add(parseCondition(node, skillId, file));

		// если тэг оказался пуст
		if(condition.isEmpty())
			log.warning("empty <or> condition in skill " + skillId + " in file " + file);

		return condition;
	}

	/**
	 * Парс условий владельца.
	 */
	private Condition parsePlayerCondition(Node node, int skillId, File file)
	{
		// новое условие
		Condition newCondition = null;

		// получаем таблицу атрибутов
		NamedNodeMap attrs = node.getAttributes();

		// получаем сообщение кондишена
		Node msg = attrs.getNamedItem("msg");

		for(int index = 0; index < attrs.getLength(); index++)
		{
			Node item = attrs.item(index);

			// тип условия
			String nodeName = item.getNodeName();
			// значение условия
			String nodeValue = item.getNodeValue();

			switch(nodeName)
			{
				case "percentHp":
				{
					int percent = Integer.parseInt(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionPlayerPercentHP(percent).setMsg(msg));

					break;
				}
				case "battle":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionPlayerBattleStance(value).setMsg(msg));

					break;
				}
				case "notBarrier":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionPlayerNotBarrier(value).setMsg(msg));

					break;
				}
				case "owerturned":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionPlayerOwerturned(value).setMsg(msg));

					break;
				}
				case "onCast":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionPlayerOnCast(value).setMsg(msg));

					break;
				}
				case "castSkillName":
				{
					newCondition = joinAnd(newCondition, new ConditionPlayerCastSkillName(SkillName.valueOf(nodeValue)).setMsg(msg));

					break;
				}
				case "stamina":
				{
					newCondition = joinAnd(newCondition, new ConditionPlayerStamina(Integer.parseInt(nodeValue)).setMsg(msg));

					break;
				}
			}
		}

		if(newCondition == null)
			log.warning("unrecognized <player> condition in skill " + skillId + " in file " + file);

		return newCondition;
	}

	/**
	 * Парс условия по цели.
	 */
	private Condition parseTargetCondition(Node node, int skillId, File file)
	{
		// новое условие
		Condition newCondition = null;

		// таблица атрибутов
		NamedNodeMap attrs = node.getAttributes();

		// сообщение кондишена
		Node msg = attrs.getNamedItem("msg");

		for(int index = 0; index < attrs.getLength(); index++)
		{
			Node item = attrs.item(index);

			// тип условия
			String nodeName = item.getNodeName();
			// значение условия
			String nodeValue = item.getNodeValue();

			switch(nodeName)
			{
				case "side":
				{
					newCondition = joinAnd(newCondition, new ConditionTargetSide(nodeValue).setMsg(msg));

					break;
				}
				case "npcType":
				{
					newCondition = joinAnd(newCondition, new ConditionTargetNpcType(NpcType.valueOf(nodeValue)).setMsg(msg));

					break;
				}
				case "npcTypes":
				{
					String[] strTypes = nodeValue.split(",");

					NpcType[] types = new NpcType[strTypes.length];

					for(int i = 0; i < strTypes.length; i++)
						types[i] = NpcType.valueOf(strTypes[i]);

					newCondition = joinAnd(newCondition, new ConditionTargetNpcTypes(types).setMsg(msg));

					break;
				}
				case "owerturned":
				{
					newCondition = joinAnd(newCondition, new ConditionTargetOwerturned(Boolean.parseBoolean(nodeValue)).setMsg(msg));

					break;
				}
				case "player":
				{
					newCondition = joinAnd(newCondition, new ConditionTargetPlayer(Boolean.parseBoolean(nodeValue)).setMsg(msg));

					break;
				}
				case "aggroMe":
				{
					newCondition = joinAnd(newCondition, new ConditionTargetAggroMe(Boolean.parseBoolean(nodeValue)).setMsg(msg));

					break;
				}
				case "npcRage":
				{
					boolean value = Boolean.parseBoolean(nodeValue);

					newCondition = joinAnd(newCondition, new ConditionTargetNpcRage(value).setMsg(msg));

					break;
				}
				default :
					log.warning("not found condition " + nodeName);
			}
		}

		if(newCondition == null)
			log.warning("unrecognized <target> condition in skill " + skillId + " in file " + file);

		return newCondition;
	}

	/**
	 * Парсер условия на использование.
	 */
	private Condition parseUsingCondition(Node atrr, int skill, File file)
	{
		// новое условие
		Condition newCondition = null;

		// таблица атрибутов
		NamedNodeMap attrs = atrr.getAttributes();

		// сообщение условия
		Node msg = attrs.getNamedItem("msg");

		for(int index = 0; index < attrs.getLength(); index++)
		{
			Node item = attrs.item(index);

			// тип условия
			String nodeName = item.getNodeName();

			// значение условия
			String nodeValue = item.getNodeValue();

			switch(nodeName)
			{
				case "weapon":
				{
					String[] values = nodeValue.split(", ");

					Array<Enum<?>> types = Arrays.toArray(Enum.class, 1);

					for(String val : values)
					{
						try
						{
							types.add(WeaponType.valueOf(val));
						}
						catch(IllegalArgumentException e)
						{
							e.printStackTrace();
						}
					}

					types.trimToSize();

					newCondition = joinAnd(newCondition, new ConditionUsingItem(types.array()).setMsg(msg));

					break;
				}
				case "armor":
				{
					String[] values = nodeValue.split(", ");

					Array<Enum<?>> types = Arrays.toArray(Enum.class, 1);

					for(String val : values)
					{
						try
						{
							types.add(ArmorType.valueOf(val));
						}
						catch(IllegalArgumentException e)
						{
							e.printStackTrace();
						}
					}

					types.trimToSize();

					newCondition = joinAnd(newCondition, new ConditionUsingItem(types.array()).setMsg(msg));

					break;
				}

			}
		}

		if(newCondition == null)
			log.warning("unrecognized <using> condition in skill " + skill + " in file " + file);

		return newCondition;
	}
}
