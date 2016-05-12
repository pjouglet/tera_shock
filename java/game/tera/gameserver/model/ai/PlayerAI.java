package tera.gameserver.model.ai;

import tera.gameserver.manager.ObjectEventManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.MoveType;
import tera.gameserver.model.Party;
import tera.gameserver.model.SayType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.actions.Action;
import tera.gameserver.model.actions.dialogs.ActionDialog;
import tera.gameserver.model.equipment.Equipment;
import tera.gameserver.model.inventory.Inventory;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.summons.Summon;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillType;

/**
 * Модель АИ игрока.
 *
 * @author Ronn
 */
public class PlayerAI extends AbstractCharacterAI<Player>
{
	/**
	 * Проверка, можно ли попробовать начать кастовать скил.
	 *
	 * @param actor кастующий.
	 * @param skill кастуемый скил.
	 * @return можно ли попробовать.
	 */
	private static boolean checkStartSkill(Player actor, Skill skill)
	{
		// если сейчас нельзя скатить скил
		if(actor.isOnMount() && skill != actor.getMountSkill() || actor.isAttackBlocking() || (actor.isRooted() && skill.getMoveDistance() != 0) || (actor.isOwerturned() && skill.getSkillType() != SkillType.OWERTURNED_STRIKE))
		{
			// получаем заряжаемый скил в текущий момент
			Skill chargeSkill = actor.getChargeSkill();

			// еслит акой есть
			if(chargeSkill != null)
			{
				// зануляем его
				actor.setChargeSkill(null);
				// зануляем уровень заряда
				actor.setChargeLevel(0);

				// обрабатываем его завершение
				chargeSkill.endSkill(actor, actor.getX(), actor.getY(), actor.getZ(), true);
			}

			return false;
		}

		return true;
	}

	/**
	 * @param actor управляемый.
	 */
	public PlayerAI(Player actor)
	{
		super(actor);
	}

	@Override
	public void notifyAttacked(Character attacker, Skill skill, int damage)
	{
		// получаем игрока
		Player actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// получаем его самона
		Summon summon = actor.getSummon();

		// если он есть
		if(summon != null)
			// уведомляем его об атаке игрока
			summon.getAI().notifyAttacked(attacker, skill, damage);
	}

	@Override
	public void startAction(Action action)
	{
		if(action == null)
			return;

		// если акшен не подготовлен к активации
		if(!action.test())
		{
			// отменяем
			action.cancel(null);
			return;
		}

		// акивируем
		action.invite();
	}

	@Override
	public synchronized void startCast(float startX, float startY, float startZ, Skill skill, int state, int heading, float targetX, float targetY, float targetZ)
	{
		// получаем игрока
		Player actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// если собирает, отменяем сбор
		if(actor.isCollecting())
			actor.abortCollect();

		// проверяем, можно ли попробовать кастануть скил
		if(!checkStartSkill(actor, skill))
			return;

		// пробуем скастануть
		actor.doCast(startX, startY, startZ, skill, state, heading, targetX, targetY, targetZ);
	}

	@Override
	public void startDressItem(int index, int itemId)
	{
		// получаем игрока
		Player actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// если идет сбор, выходим
		if(actor.isCollecting())
			return;

		// получаем экиперовку
		Equipment equipment = actor.getEquipment();
		// получаем инвентарь
		Inventory inventory = actor.getInventory();

		// если чего-то из этого нету, выходим
		if(equipment == null || inventory == null)
			return;

		// если индекс меньше 20, значит это снятие итема
		if(index < 20)
			equipment.shootItem(inventory, index - 1, itemId);
		// иначе это одевание итема из инвенторя
		else
			equipment.dressItem(inventory, inventory.getCell(index - 20));
	}

	@Override
	public void startItemPickUp(ItemInstance item)
	{
		if(item == null)
			return;

		// получаем игрока
		Player actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// если идет сбор, выходим
		if(actor.isStuned() || actor.isCollecting())
			return;

		// если на коне
		if(actor.isOnMount())
		{
			actor.sendMessage("Нельзя поднимать сидя на маунте.");
			return;
		}

		// получаем пати, которая выбила итем
		Party party = item.getTempOwnerParty();

		// если такая есть
		if(party != null)
		{
			// и если игрок не из этой птаи
			if(actor.getParty() != party)
				// сообщаем об этом
				actor.sendMessage("Не ваша группа выбила этот предмет.");
			else
				// подбираем итем
				item.pickUpMe(actor);

			return;
		}

		// получаем объект, который выбил итем
		TObject owner = item.getTempOwner();

		// еслио он есть
		if(owner != null)
		{
			// и если это не игрок
			if(owner != actor)
				// сообщаем об этом
				actor.sendMessage("Не вы выбили этот предмет.");
			else
				// иначе подбираем
				item.pickUpMe(actor);

			return;
		}

		// подбираем итем
		item.pickUpMe(actor);
	}

	@Override
	public void startMove(float startX, float startY, float startZ, int heading, MoveType type, float targetX, float targetY, float targetZ, boolean broadCastMove, boolean sendSelfPacket)
	{
		// получаем игрока
		Player actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		if(actor.isCollecting())
			actor.abortCollect();

		super.startMove(startX, startY, startZ, heading, type, targetX, targetY, targetZ, broadCastMove, sendSelfPacket);

		// если есть активный диалог акшена
		ActionDialog dialog = actor.getLastActionDialog();

		// закрываем его
		if(dialog != null)
			dialog.cancel(actor);
	}

	@Override
	public void startSay(String text, SayType type)
	{
		// получаем игрока
		Player actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// флаг бана чата
		boolean banned = false;

		// если за игроком записана время окончания бана
		if(actor.getEndChatBan() > 0)
		{
			// и если оно позже текущего
			if(actor.getEndChatBan() > System.currentTimeMillis())
				// ставим флаг бана
				banned = true;
			else
				// зануляем бан
				actor.setEndChatBan(0);
		}

		// если стоит бан чата, сообщеаем и выходим
		if(banned && (type == SayType.MAIN_CHAT || type == SayType.LOOKING_FOR_GROUP || type == SayType.SHAUT_CHAT || type == SayType.TRADE_CHAT || type == SayType.LOOKING_FOR_GROUP))
		{
			actor.sendMessage("Ваш чат заблокирован.");
			return;
		}

		// запускаем отправку сообщения в чат
		super.startSay(text, type);
	}

	@Override
	public void startUseItem(ItemInstance item, int heading, boolean isHerb)
	{
		// получаем игрока
		Player actor = getActor();

		if(actor == null || item == null || !item.isCommon() || item.getItemLevel() > actor.getLevel() || actor.isOwerturned())
			return;

		// если собирает, отменяем сбор
		if(actor.isCollecting())
			actor.abortCollect();

		// полуаем менеджер событий
		ObjectEventManager eventManager = ObjectEventManager.getInstance();

		// уведомляем о использовании придмета
		eventManager.notifyUseItem(item, actor);

		// получаем активный скил итема
		Skill skill = item.getActiveSkill();

		// если его нет, выходим
		if(skill == null)
			return;

		// запускаем попытку скастовать
		actor.doCast(skill, heading, item);
	}

	@Override
	public void notifyArrived()
	{
		// TODO Автоматически созданная заглушка метода
	}

	@Override
	public void notifyAppliedEffect(Effect effect)
	{
		// TODO Автоматически созданная заглушка метода
	}

	@Override
	public void notifySpawn()
	{
		// TODO Автоматически созданная заглушка метода
	}

	@Override
	public void notifyAttack(Character attacked, Skill skill, int damage)
	{
		// TODO Автоматически созданная заглушка метода
	}

	@Override
	public void notifyPickUpItem(ItemInstance item)
	{
		// TODO Автоматически созданная заглушка метода
	}

	@Override
	public void notifyDead(Character killer)
	{
		// TODO Автоматически созданная заглушка метода
	}

	@Override
	public void notifyCollectResourse(ResourseInstance resourse)
	{
		// TODO Автоматически созданная заглушка метода
	}
}
