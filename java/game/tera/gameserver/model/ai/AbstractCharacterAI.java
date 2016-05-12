package tera.gameserver.model.ai;

import rlib.util.array.Array;
import tera.Config;
import tera.gameserver.model.Character;
import tera.gameserver.model.EmotionType;
import tera.gameserver.model.Guild;
import tera.gameserver.model.MoveType;
import tera.gameserver.model.Party;
import tera.gameserver.model.SayType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.World;
import tera.gameserver.model.actions.Action;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;
import tera.gameserver.model.skillengine.SkillType;
import tera.gameserver.network.serverpackets.CharSay;
import tera.gameserver.network.serverpackets.Emotion;
import tera.gameserver.network.serverpackets.NpcDialogWindow;
import tera.gameserver.network.serverpackets.NpcSpeak;
import tera.gameserver.network.serverpackets.RequestNpcInteractionSuccess;
import tera.util.LocalObjects;

/**
 * Базовая реализация АИ персонажей.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public abstract class AbstractCharacterAI<E extends Character> extends AbstractAI<E> implements CharacterAI
{
	/**
	 * @param actor управляемый АИ.
	 */
	public AbstractCharacterAI(E actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return false;
	}

	@Override
	public void notifyAgression(Character attacker, long aggro)
	{
		log.warning(this, "notifyAgression() not supported method.");
	}

	@Override
	public void notifyAppliedEffect(Effect effect)
	{
		log.warning(this, "notifyAppliedEffect() not supported method.");
	}

	@Override
	public void notifyArrived()
	{
		log.warning(this, "notifyArrived() not supported method.");
	}

	@Override
	public void notifyArrivedBlocked()
	{
		log.warning(this, "notifyArrivedBlocked() not supported method.");
	}

	@Override
	public void notifyArrivedTarget(TObject target)
	{
		log.warning(this, "not supported method.");
	}

	@Override
	public void notifyAttack(Character attacked, Skill skill, int damage)
	{
		log.warning(this, "notifyAttack() not supported method.");
	}

	@Override
	public void notifyAttacked(Character attacker, Skill skill, int damage)
	{
		log.warning(this, "notifyAttacked() not supported method.");
	}

	@Override
	public void notifyClanAttacked(Character attackedMember, Character attacker, int damage)
	{
		log.warning(this, "notifyClanAttacked() not supported method.");
	}

	@Override
	public void notifyCollectResourse(ResourseInstance resourse)
	{
		log.warning(this, "notifyCollectResourse() not supported method.");
	}

	@Override
	public void notifyDead(Character killer)
	{
		log.warning(this, "notifyDead() not supported method.");
	}

	@Override
	public void notifyFinishCasting(Skill skill)
	{
		if(Config.DEVELOPER_MAIN_DEBUG)
			actor.sendMessage("end cast skill.");
	}

	@Override
	public void notifyPartyAttacked(Character attackedMember, Character attacker, int damage)
	{
		log.warning(this, "notifyPartyAttacked() not supported method.");
	}

	@Override
	public void notifyPickUpItem(ItemInstance item)
	{
		log.warning(this, "notifyPickUpItem() not supported method.");
	}

	@Override
	public void notifySpawn()
	{
		log.warning(this, "notifySpawn() not supported method.");
	}

	@Override
	public void notifyStartCasting(Skill skill)
	{
		if(Config.DEVELOPER_MAIN_DEBUG)
			actor.sendMessage("start cast skill.");
	}

	@Override
	public void notifyStartDialog(Player player)
	{
		log.warning(this, "notifyStartDialog() not supported method.");
	}

	@Override
	public void notifyStopDialog(Player player)
	{
		log.warning(this, "notifyStopDialog() not supported method.");
	}

	@Override
	public void startAction(Action action)
	{
		log.warning(this, "startAction() not supported method.");
	}

	@Override
	public void startActive()
	{
		log.warning(this, "startActive() not supported method.");
	}

	/**
	 * Запуск работы АИ.
	 */
	public void startAITask()
	{
		log.warning(this, "startAITask() not supported method.");
	}

	@Override
	public void startCast(float startX, float startY, float startZ, Skill skill, int state, int heading, float targetX, float targetY, float targetZ)
	{
		if(actor.isAttackBlocking() || (actor.isOwerturned() && skill.getSkillType() != SkillType.OWERTURNED_STRIKE))
			return;

		actor.doCast(startX, startY, startZ, skill, state, heading, targetX, targetY, targetZ);
	}

	@Override
	public void startCast(Skill skill, int heading, float targetX, float targetY, float targetZ)
	{
		startCast(actor.getX(), actor.getY(), actor.getZ(), skill, 0, heading, targetX, targetY, targetZ);
	}

	@Override
	public void startCollectResourse(ResourseInstance resourse)
	{
		// если ресурса нет, выходи
		if(resourse == null)
		{
			log.warning(this, new Exception("not found resourse"));
			return;
		}

		// получаем актора
		E actor = getActor();

		// если он находится на маунте
		if(actor.isOnMount())
		{
			// слазим с него
			actor.getOffMount();
			// вызодим
			return;
		}

		// собрать ресурс
		resourse.collectMe(actor);
	}

	@Override
	public void startDressItem(int index, int itemId)
	{
		log.warning(this, "not supported method.");
	}

	@Override
	public void startEmotion(EmotionType type)
	{
		// получаем персонажа
		E actor = getActor();
		
		if(actor == null || actor.isBattleStanced() || actor.isCollecting() || actor.isMoving() || actor.isCastingNow() || actor.isAllBlocking() || actor.isFlyingPegas())
			return;

		// отображаем эмоцию персонажа
		actor.broadcastPacket(Emotion.getInstance(actor, type));
	}

	@Override
	public void startItemPickUp(ItemInstance item)
	{
		log.warning(this, "startItemPickUp() not supported method.");
	}

	@Override
	public void startIteract(Character object)
	{
		log.warning(this, "startIteract() not supported method.");
	}

	@Override
	public void startMove(float startX, float startY, float startZ, int heading, MoveType type, float targetX, float targetY, float targetZ, boolean broadCastMove, boolean sendSelfPacket)
	{
		// получаем игрока
		E actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// если движение заблокировано, выходим
		if(actor.isMovementDisabled())
			return;

		// запускаем перемещенение
		actor.moveToLocation(startX, startY, startZ, heading, type, targetX, targetY, targetZ, broadCastMove, sendSelfPacket);
	}

	@Override
	public void startMove(int heading, MoveType type, float targetX, float targetY, float targetZ, boolean broadCastMove, boolean sendSelfPacket)
	{
		startMove(actor.getX(), actor.getY(), actor.getZ(), heading, type, targetX, targetY, targetZ, broadCastMove, sendSelfPacket);
	}

	@Override
	public void startNpcSpeak(Npc npc)
	{
		// получаем персонажа
		E actor = getActor();

		// если нет нпс или нет персонажа или персонаж не игрок, выходим
		if(npc == null || actor == null || !actor.isPlayer())
			return;

		// получаем игрока
		Player player = actor.getPlayer();

		// очищаем ему ссылки
		player.clearLinks();

		if(!npc.checkInteraction(player))
			player.sendPacket(RequestNpcInteractionSuccess.getInstance(RequestNpcInteractionSuccess.NOT_SUCCESS), true);
		else
		{
			player.setLastNpc(npc);
			player.sendPacket(RequestNpcInteractionSuccess.getInstance(RequestNpcInteractionSuccess.SUCCEESS), true);
			player.sendPacket(NpcSpeak.getInstance(player, npc), true);
			player.sendPacket(NpcDialogWindow.getInstance(npc, player, npc.getLinks(player)), true);
		}
	}

	@Override
	public void startRest()
	{
		log.warning(this, "startRest() not supported method.");
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public void startSay(String text, SayType type)
	{
		// получаем актора
		E actor = getActor();

		// если его нет, выходим
		if(actor == null)
		{
			log.warning(this, new Exception("not found actor"));
			return;
		}

		// получаем локальные объекты
		LocalObjects local = LocalObjects.get();

		switch(type)
		{
			// основной чат
			case MAIN_CHAT:
			{
				// получаем окружающих игроков
				Array<Player> around = World.getAround(Player.class, local.getNextPlayerList(), actor, 300);

				// получаем их массив
				Player[] array = around.array();

				// создаем пакет сообщения
				CharSay say = CharSay.getInstance(actor.getName(), text, type, actor.getObjectId(), actor.getSubId());

				// добавляем счетчик отправок
				for(int i = 0, length = around.size(); i < length; i++)
					say.increaseSends();

				// добавляем счетчик отправок
				say.increaseSends();

				// отправляем пакет окружающим
				for(int i = 0, length = around.size(); i < length; i++)
					array[i].sendPacket(say, false);

				// отправялм отправителю
				actor.sendPacket(say, false);

				break;
			}
			// группавой чат
			case PARTY_CHAT:
			{
				// получаем пати
				Party party = actor.getParty();

				// если она есть
				if(party != null)
					// отправлям сообщение в пати
					party.sendMessage((Player) actor, text);

				break;
			}
			// чат гильдии
			case GUILD_CHAT:
			{
				// получаем гильдию игрока
				Guild guild = actor.getGuild();

				// если гильдия есть
				if(guild != null)
					// отправляем сообщение в гильдию
					guild.sendMessage((Player) actor, text);

				break;
			}
			case SHAUT_CHAT:
				actor.broadcastPacket(CharSay.getInstance(actor.getName(), text, type, actor.getObjectId(), actor.getSubId()));
				break;
			case TRADE_CHAT:
			case LOOKING_FOR_GROUP:
			{
				// создаем сообщение
				CharSay say = CharSay.getInstance(actor.getName(), text, type, actor.getObjectId(), actor.getSubId());

				// получаем список онлаин игроков
				Array<Player> players = World.getPlayers();

				players.readLock();
				try
				{
					// увеличиваем счетчик отправок
					for(int i = 0, length = players.size(); i < length; i++)
						say.increaseSends();

					// получаем массив
					Player[] array = players.array();

					// отправляем пакеты
					for(int i = 0, length = players.size(); i < length; i++)
						array[i].sendPacket(say, false);
				}
				finally
				{
					players.readUnlock();
				}

				break;
			}
		}
	}

	@Override
	public void startUseItem(ItemInstance item, int heading, boolean isHeb)
	{
		log.warning(this, "startUseItem() not supported method.");
	}

	@Override
	public void stopAITask()
	{
		log.warning(this, "stopAITask() not supported method.");
	}

	@Override
	public void clearTaskList()
	{
		log.warning(this, "clearTaskList() not supported method.");
	}

	@Override
	public void abortAttack()
	{
		log.warning(this, "abortAttack() not supported method.");
	}

	@Override
	public void startAttack(Character target)
	{
		log.warning(this, "startAttack() not supported method.");
	}
}
