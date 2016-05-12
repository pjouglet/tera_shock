package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Angles;
import rlib.util.Rnd;
import rlib.util.Strings;
import rlib.util.VarTable;
import rlib.util.array.Array;

import tera.gameserver.model.Character;
import tera.gameserver.model.Party;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.MessagePackage;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.skillengine.OperateType;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.gameserver.tables.MessagePackageTable;
import tera.util.LocalObjects;

/**
 * Модель фабрики заданий для суммона в бою НПС.
 *
 * @author Ronn
 */
public class SummonBattleTaskFactory extends AbstractTaskFactory
{
	/** сообщения при уклонении */
	protected final MessagePackage evasionMessages;
	/** сообщения при бафе */
	protected final MessagePackage buffMessages;
	/** сообщения при дебафе */
	protected final MessagePackage debuffMesages;
	/** сообщения при блоке */
	protected final MessagePackage shieldMessages;
	/** сообщения использовании ловушек */
	protected final MessagePackage trapMessages;
	/** сообщения при атаке */
	protected final MessagePackage attackMessages;
	/** сообщения при хиле */
	protected final MessagePackage healMessages;

	/** шансы каста скилов */
	protected final int[] groupChance;

	/** шанс отправки сообщения */
	protected final int shortRange;
	/** интервал сообщений */
	protected final int messageInterval;
	/** дистанция оказания помощи персонажу суммоном от владельца */
	protected final int supportRange;

	/** использовать ли быстрый поворот */
	protected final boolean fastTurn;

	public SummonBattleTaskFactory(Node node)
	{
		super(node);

		try
		{
			// парсим атрибуты
			VarTable vars = VarTable.newInstance(node);

			// получаем шанс групп скилов по умолчанию
			int def = vars.getInteger("groupChance", ConfigAI.DEFAULT_GROUP_CHANCE);

			// получаем список всех групп скилов
			SkillGroup[] groups = SkillGroup.values();

			// парсим параметры
			vars = VarTable.newInstance(node, "set", "name", "val");

			// создаем таблицу шансов групп скилов
			this.groupChance = new int[groups.length];
			this.shortRange = vars.getInteger("shortRange", ConfigAI.DEFAULT_SHORT_RATE);
			this.supportRange = vars.getInteger("supportRange", getShortRange() * 2);
			this.fastTurn = vars.getBoolean("fastTurn", false);

			// заполняем таблицу
			for(int i = 0, length = groupChance.length; i < length; i++)
				groupChance[i] = vars.getInteger(groups[i].name(), def);

			this.messageInterval = vars.getInteger("messageInterval", 120000);

			// получаем таблицу сообщений
			MessagePackageTable messageTable = MessagePackageTable.getInstance();

			this.evasionMessages = messageTable.getPackage(vars.getString("evasionMessages", Strings.EMPTY));
			this.buffMessages = messageTable.getPackage(vars.getString("buffMessages", Strings.EMPTY));
			this.debuffMesages = messageTable.getPackage(vars.getString("debuffMesages", Strings.EMPTY));
			this.shieldMessages = messageTable.getPackage(vars.getString("shieldMessages", Strings.EMPTY));
			this.trapMessages = messageTable.getPackage(vars.getString("trapMessages", Strings.EMPTY));
			this.attackMessages = messageTable.getPackage(vars.getString("attackMessages", Strings.EMPTY));
			this.healMessages = messageTable.getPackage(vars.getString("healMessages", Strings.EMPTY));
		}
		catch(Exception e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public <A extends Npc> void addNewTask(NpcAI<A> ai, A actor, LocalObjects local, ConfigAI config, long currentTime)
	{
		// получаем владельца суммона
		Character owner = actor.getOwner();

		// если его нет, выходим
		if(owner == null)
			return;

		// пакет с сообщениями
		MessagePackage messagePackage = null;

		// если срабатывает шанс на хил
		if(chance(SkillGroup.HEAL))
		{
			// получаем скил для хила
			Skill skill = actor.getRandomSkill(SkillGroup.HEAL);

			// если он есть и не в откате
			if(skill != null && skill.checkCondition(actor, actor.getX(), actor.getY(), actor.getZ()))
			{
				// делаем заготовку на цель хила
				Character target = null;

				// если у самона не фул хп, будем хилить себя
				if(!skill.isNoCaster() && actor.getCurrentHp() < actor.getMaxHp())
					target = actor;
				// если у владельца не фул хп, будем хилить его
				else if(!skill.isTargetSelf() && owner.getCurrentHp() < owner.getMaxHp())
					target = owner;
				else if(!skill.isTargetSelf())
				{
					// получаем группу владельца
					Party party = owner.getParty();

					// если она есть
					if(party != null)
					{
						float distance = getSupportRange();

						// получаем ее членов
						Array<Player> members = party.getMembers();

						members.readLock();
						try
						{
							Player[] array = members.array();

							// перебираем
							for(int i = 0, length = members.size(); i < length; i++)
							{
								Player member = array[i];

								// если член группы с фул хп или владелец, пропускаем
								if(members == owner || member.getCurrentHp() >= member.getMaxHp())
									continue;

								// получаем дистанцию члена группы от владельца
								float dist = owner.getDistance3D(member);

								// если она ближе чем надо
								if(dist < distance)
								{
									// запоминаем члена группы
									target = member;
									// запоминаем его дистанцию
									distance = dist;
								}
							}
						}
						finally
						{
							members.readUnlock();
						}
					}
				}

				// если есть цель для хила
				if(target != null)
				{
					String message = Strings.EMPTY;

					// получаем пакет сообщений для хила
					messagePackage = getHealMessages();

					// если есть пакет и время для сообщения
					if(messagePackage != null && currentTime - ai.getLastMessage() > getMessageInterval())
					{
						// полцчаем сообщение
						message = messagePackage.getRandomMessage();
						// обновляем время
						ai.setLastMessage(currentTime + getMessageInterval());
					}

					// добавляем на каст
					ai.addCastTask(skill, target, message);
					return;
				}
			}
		}

		// если срабатывает шанс на баф
		if(chance(SkillGroup.BUFF))
		{
			// получаем скил для бафа
			Skill skill = actor.getRandomSkill(SkillGroup.BUFF);

			// если он есть и не в откате
			if(skill != null && skill.checkCondition(actor, actor.getX(), actor.getY(), actor.getZ()))
			{
				// делаем заготовку на цель хила
				Character target = null;

				// если у самона этого эффекта нету
				if(!actor.containsEffect(skill))
					target = actor;
				// если у владельца этого эффекта нету
				else if(skill.isTargetSelf() && !owner.containsEffect(skill))
					target = owner;
				else if(skill.isTargetSelf())
				{
					// получаем группу владельца
					Party party = owner.getParty();

					// если она есть
					if(party != null)
					{
						float distance = getSupportRange();

						// получаем ее членов
						Array<Player> members = party.getMembers();

						members.readLock();
						try
						{
							Player[] array = members.array();

							// перебираем
							for(int i = 0, length = members.size(); i < length; i++)
							{
								Player member = array[i];

								// если член группы нет эффекта
								if(members == owner || member.containsEffect(skill))
									continue;

								// получаем дистанцию члена группы от владельца
								float dist = owner.getDistance3D(member);

								// если она ближе чем надо
								if(dist < distance)
								{
									// запоминаем члена группы
									target = member;
									// запоминаем его дистанцию
									distance = dist;
								}
							}
						}
						finally
						{
							members.readUnlock();
						}
					}
				}

				// если есть цель для хила
				if(target != null)
				{
					String message = Strings.EMPTY;

					// получаем пакет сообщений для бафа
					messagePackage = getBuffMessages();

					// если есть пакет и время для сообщения
					if(messagePackage != null && currentTime - ai.getLastMessage() > getMessageInterval())
					{
						// полцчаем сообщение
						message = messagePackage.getRandomMessage();
						// обновляем время
						ai.setLastMessage(currentTime + getMessageInterval());
					}

					// добавляем на каст
					ai.addCastTask(skill, target, message);
					return;
				}
			}
		}

		// если выпал случай на установку ловушки
		if(chance(SkillGroup.TRAP))
		{
			// получаем скил ловушки
			Skill skill = actor.getRandomSkill(SkillGroup.TRAP);

			// если он есть и не в откате
			if(skill != null && skill.checkCondition(actor, actor.getX(), actor.getY(), actor.getZ()))
			{
				String message = Strings.EMPTY;

				// получаем пакет сообщений для бафа
				messagePackage = getTrapMessages();

				// если есть пакет и время для сообщения
				if(messagePackage != null && currentTime - ai.getLastMessage() > getMessageInterval())
				{
					// полцчаем сообщение
					message = messagePackage.getRandomMessage();
					// обновляем время
					ai.setLastMessage(currentTime + getMessageInterval());
				}

				// добавляем на каст
				ai.addCastTask(skill, actor, message);
				return;
			}
		}

		// получаем текущую цель
		Character target = ai.getTarget();

		// если ее нет, выходим
		if(target == null)
			return;

		if(!actor.isBattleStanced())
			actor.startBattleStance(target);

		// если выпал случай использования бафа
		if(chance(SkillGroup.DEBUFF))
		{
			// получаем скил для бафа
			Skill skill = actor.getRandomSkill(SkillGroup.DEBUFF);

			// если он есть и не в откате
			if(skill != null && skill.checkCondition(actor, target.getX(), target.getY(), target.getZ()))
			{
				String message = Strings.EMPTY;

				// получаем пакет сообщений для бафа
				messagePackage = getDebuffMesages();

				// если есть пакет и время для сообщения
				if(messagePackage != null && currentTime - ai.getLastMessage() > getMessageInterval())
				{
					// полцчаем сообщение
					message = messagePackage.getRandomMessage();
					// обновляем время
					ai.setLastMessage(currentTime + getMessageInterval());
				}

				// добавляем на каст
				ai.addCastTask(skill, target, message);
				return;
			}
		}

		// если НПС не в боевой стойке
		if(!actor.isBattleStanced())
		{
			// отправляем в боевую стойку
			ai.addNoticeTask(target, true);
			return;
		}

		// получаем кастующий скил цели
		Skill castingSkill = target.getCastingSkill();

		// если цель что-то кастует
		if(castingSkill != null && castingSkill.getOperateType() == OperateType.ACTIVE)
		{
			// если срабатывает случай на использование блока
			if(chance(SkillGroup.SHIELD))
			{
				// получаем скил блока
				Skill skill = actor.getRandomSkill(SkillGroup.SHIELD);

				// если он есть и не в откате
				if(skill != null && skill.checkCondition(actor, actor.getX(), actor.getY(), actor.getZ()))
				{
					// получаем итоговую зону поражения скила
					int range = castingSkill.getRange() + castingSkill.getRadius();

					// если НПС входит в зону поражения и цель находится с переди
					if(actor.isInRange(target, range) && target.isInFront(actor))
					{
						String message = Strings.EMPTY;

						// получаем пакет сообщений для бафа
						messagePackage = getShieldMessages();

						// если есть пакет и время для сообщения
						if(messagePackage != null && currentTime - ai.getLastMessage() > getMessageInterval())
						{
							// полцчаем сообщение
							message = messagePackage.getRandomMessage();
							// обновляем время
							ai.setLastMessage(currentTime + getMessageInterval());
						}

						// добавляем на каст
						ai.addCastTask(skill, target, message);
						return;
					}
				}
			}

			// если срабатывает шанс на использование скила прыжка
			if(chance(SkillGroup.JUMP))
			{
				// пробуем получить прыжковый скил
				Skill skill = actor.getRandomSkill(SkillGroup.JUMP);

				// если такой скил есть и он не в откате
				if(skill != null && skill.checkCondition(actor, actor.getX(), actor.getY(), actor.getZ()))
				{
					String message = Strings.EMPTY;

					// получаем пакет сообщений для бафа
					messagePackage = getEvasionMessages();

					// если есть пакет и время для сообщения
					if(messagePackage != null && currentTime - ai.getLastMessage() > getMessageInterval())
					{
						// полцчаем сообщение
						message = messagePackage.getRandomMessage();
						// обновляем время
						ai.setLastMessage(currentTime + getMessageInterval());
					}

					// получаем дистанцию прыжка
					int range = skill.getMoveDistance();

					// определяем направление прыжка
					boolean positive = range > 0;

					// определяем боковое ли это движение
					boolean isSide = skill.getHeading() != 0;

					// если это боковое движение
					if(isSide)
					{
						// если кастуемый скил нацеленный
						if(castingSkill.isOneTarget())
						{
							// добавляем в задание скастануть боковое движение
							ai.addCastTask(skill, actor, Angles.calcHeading(actor.getX(), actor.getY(), target.getX(), target.getY()), message);
							return;
						}
					}
					// если цель находится в оборонительной стойке, а мы в зоне обороны
					else if(target.isDefenseStance() && target.isInFront(actor))
					{
						// нужный разворот
						int newHeading = 0;

						// определеям нужный разворот для запрыгивания заспину
						if(positive)
							newHeading = actor.calcHeading(target.getX(), target.getY());
						else
							newHeading = target.calcHeading(actor.getX(), actor.getY());

						// добавляем задание запрыгнуть за спину
						ai.addCastTask(skill, actor, newHeading, message);
						return;
					}
					// если прыжок - отпрыгивание назад
					else if(!positive)
					{
						// если мы находимся под ударом и имеем возможность отскочить
						if(castingSkill.getRange() < getShortRange() && actor.getGeomDistance(target) < getShortRange())
						{
							// добавляем отскок от него
							ai.addCastTask(skill, actor, actor.calcHeading(target.getX(), target.getY()), message);
							return;
						}
					}
					// если это укланение
					else if(skill.isEvasion())
					{
						// добавляем задание использовать укланение
						ai.addCastTask(skill, target, message);
						return;
					}
				}
			}
		}

		// получаем мили скил
		Skill shortSkill = actor.getRandomSkill(SkillGroup.SHORT_ATTACK);

		// получаем дистанционный скил
		Skill longSkill = actor.getRandomSkill(SkillGroup.LONG_ATTACK);

		String message = Strings.EMPTY;

		// получаем пакет сообщений для бафа
		messagePackage = getAttackMessages();

		// если есть пакет и время для сообщения
		if(messagePackage != null && currentTime - ai.getLastMessage() > getMessageInterval())
		{
			// получаем сообщение
			message = messagePackage.getRandomMessage();
			// обновляем время
			ai.setLastMessage(currentTime + getMessageInterval());
		}

		// если цель находится в ближнем радиусе
		if(actor.getGeomDistance(target) < getShortRange())
		{
			for(int i = 0, length = 5; (shortSkill == null || !shortSkill.checkCondition(actor, actor.getX(), actor.getY(), actor.getZ())) && i < length; i++)
				shortSkill = actor.getRandomSkill(SkillGroup.SHORT_ATTACK);

			// если ближний скил доступен
			if(shortSkill != null && !actor.isSkillDisabled(shortSkill))
			{
				// используем ближний скил
				ai.addCastTask(shortSkill, target, message);
				return;
			}
			// иначе если доступен дальний скил
			else if(longSkill != null && !actor.isSkillDisabled(longSkill))
			{
				// используем дальний скил
				ai.addCastTask(longSkill, target, message);
				return;
			}
		}
		// если цель в дальнем радиусе
		else
		{
			for(int i = 0, length = 5; (longSkill == null || !longSkill.checkCondition(actor, actor.getX(), actor.getY(), actor.getZ())) && i < length; i++)
				longSkill = actor.getRandomSkill(SkillGroup.LONG_ATTACK);

			// если дальний скил доступен
			if(longSkill != null && !actor.isSkillDisabled(longSkill))
			{
				// используем дальний скил
				ai.addCastTask(longSkill, target, message);
				return;
			}
			// иначе если ближний доступен
			else if(shortSkill != null && !actor.isSkillDisabled(shortSkill))
			{
				// используем ближний скил
				ai.addCastTask(shortSkill, target, message);
				return;
			}
		}
	}

	/**
	 * @return сработала ли указанная группа.
	 */
	protected boolean chance(SkillGroup group)
	{
		return Rnd.chance(groupChance[group.ordinal()]);
	}

	/**
	 * @return дистанция, считающася ближней.
	 */
	protected final int getShortRange()
	{
		return shortRange;
	}

	/**
	 * @return сообщении при атаки.
	 */
	public MessagePackage getAttackMessages()
	{
		return attackMessages;
	}

	/**
	 * @return сообщении при использовании бафа.
	 */
	public MessagePackage getBuffMessages()
	{
		return buffMessages;
	}

	/**
	 * @return сообщении при использовании дебафа.
	 */
	public MessagePackage getDebuffMesages()
	{
		return debuffMesages;
	}

	/**
	 * @return сообщении при использовании укланения.
	 */
	public MessagePackage getEvasionMessages()
	{
		return evasionMessages;
	}

	/**
	 * @return интервла сообщений.
	 */
	public int getMessageInterval()
	{
		return messageInterval;
	}

	/**
	 * @return сообщении при использовании блока.
	 */
	public MessagePackage getShieldMessages()
	{
		return shieldMessages;
	}

	/**
	 * @return сообщении при использовании ловушек.
	 */
	public MessagePackage getTrapMessages()
	{
		return trapMessages;
	}

	/**
	 * @return сообщения при хиле.
	 */
	public MessagePackage getHealMessages()
	{
		return healMessages;
	}

	/**
	 * @return дистанция от владельца для оказание поддержки.
	 */
	public int getSupportRange()
	{
		return supportRange;
	}
}
