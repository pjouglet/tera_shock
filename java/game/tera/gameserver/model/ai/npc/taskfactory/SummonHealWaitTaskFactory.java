package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

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
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillGroup;
import tera.gameserver.tables.MessagePackageTable;
import tera.util.LocalObjects;

/**
 * Модель фабрики заданий хилера суммона во время ожидания.
 *
 * @author Ronn
 */
public class SummonHealWaitTaskFactory extends SummonWaitTaskFactory
{
	/** сообщения при хиле */
	protected final MessagePackage healMessages;
	/** сообщения при бафе */
	protected final MessagePackage buffMessages;

	/** шансы каста скилов */
	protected final int[] groupChance;

	/** дистанция оказания помощи персонажу суммоном от владельца */
	protected final int supportRange;

	public SummonHealWaitTaskFactory(Node node)
	{
		super(node);

		try
		{
			// парсим атрибуты
			VarTable vars = VarTable.newInstance(node);

			// получаем список всех групп скилов
			SkillGroup[] groups = SkillGroup.values();

			// парсим параметры
			vars = VarTable.newInstance(node, "set", "name", "val");

			// создаем таблицу шансов групп скилов
			this.groupChance = new int[groups.length];
			this.supportRange = vars.getInteger("supportRange", 300);

			// заполняем таблицу
			for(int i = 0, length = groupChance.length; i < length; i++)
				groupChance[i] = vars.getInteger(groups[i].name(), 0);

			// получаем таблицу сообщений
			MessagePackageTable messageTable = MessagePackageTable.getInstance();

			this.buffMessages = messageTable.getPackage(vars.getString("buffMessages", Strings.EMPTY));
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

		super.addNewTask(ai, actor, local, config, currentTime);
	}

	/**
	 * @return сработала ли указанная группа.
	 */
	protected boolean chance(SkillGroup group)
	{
		return Rnd.chance(groupChance[group.ordinal()]);
	}

	/**
	 * @return сообщении при использовании бафа.
	 */
	public MessagePackage getBuffMessages()
	{
		return buffMessages;
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
