package tera.gameserver.model.ai;

import tera.gameserver.model.Character;
import tera.gameserver.model.EmotionType;
import tera.gameserver.model.MoveType;
import tera.gameserver.model.SayType;
import tera.gameserver.model.TObject;
import tera.gameserver.model.actions.Action;
import tera.gameserver.model.items.ItemInstance;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.model.playable.Player;
import tera.gameserver.model.resourse.ResourseInstance;
import tera.gameserver.model.skillengine.Effect;
import tera.gameserver.model.skillengine.Skill;

/**
 * Интерфейс для реализации АИ.
 *
 * @author Ronn
 * @created 12.04.2012
 */
public interface AI
{
	/**
	 * Изменение уровня агрессии объекта по отношению к объекту.
	 * 
	 * @param attacker атакующий объект.
	 * @param aggro уровень изминения агресии.
	 */
	public void notifyAgression(Character attacker, long aggro);
	
	/**
	 * Уведомление о наложении эффекта.
	 * 
	 * @param effect наложенный эффект.
	 */
	public void notifyAppliedEffect(Effect effect);
	
	/**
	 * Уведомление об прибытии объекта в назначенную точку.
	 */
	public void notifyArrived();
	
	/**
	 * Уведомление об блокировке перемещения объекта.
	 */
	public void notifyArrivedBlocked();
	
	/**
	 * Уведомление о прибытии объекта к указанной цели.
	 * 
	 * @param target цель прибытия.
	 */
	public void notifyArrivedTarget(TObject target);
	
	/**
	 * Уведомление о атаке объектом.
	 * 
	 * @param attacked атакуемый объект.
	 * @param skill атакующий скилл.
	 * @param damage кол-во урона.
	 */
	public void notifyAttack(Character attacked, Skill skill, int damage);
	
	/**
	 * Уведомление о атаке объекта.
	 * 
	 * @param attacker атакующий объект.
	 * @param skill атакующий скил.
	 * @param damage кол-во урона.
	 */
	public void notifyAttacked(Character attacker, Skill skill, int damage);
	
	/**
	 * Уведомление о нападении на члена клана.
	 * 
	 * @param attackedMember атакуемый член клана.
	 * @param attacker атакующий на члена клана.
	 * @param damage нанесенный урон.
	 */
	public void notifyClanAttacked(Character attackedMember, Character attacker, int damage);
	
	/**
	 * Увндомление о сборе ресурса.
	 * 
	 * @param resourse собранный ресурс.
	 */
	public void notifyCollectResourse(ResourseInstance resourse);
	
	/**
	 * Уведомление о смерти объекта.
	 * 
	 * @param killer убийка объекта.
	 */
	public void notifyDead(Character killer);
	
	/**
	 * Уведомление о завершении каста скилла объектом.
	 * 
	 * @param skill кастуемый скил.
	 */
	public void notifyFinishCasting(Skill skill);
	
	/**
	 * Уведомление о нападении на члена группы.
	 * 
	 * @param attackedMember член группы.
	 * @param attacker атакующий члена группы.
	 * @param damage нанесенный урон.
	 */
	public void notifyPartyAttacked(Character attackedMember, Character attacker, int damage);
	
	/**
	 * Уведомление о поднятие объектом итем с земли.
	 * 
	 * @param item поднятый итем.
	 */
	public void notifyPickUpItem(ItemInstance item);
	
	/**
	 * Уведомление об спавне объекта.
	 */
	public void notifySpawn();
	
	/**
	 * Уведомление о начале каста скила объектом.
	 * 
	 * @param skill кастуемый скил.
	 */
	public void notifyStartCasting(Skill skill);
	
	/**
	 * Уведомление об начале диалога объекта с игроком.
	 * 
	 * @param player разговариваемый игрок.
	 */
	public void notifyStartDialog(Player player);
	
	/**
	 * Уведомление об завершении диалога объекта с игроком.
	 * 
	 * @param player разговариваемый игрок.
	 */
	public void notifyStopDialog(Player player);
	
	/**
	 * Начать акшен.
	 * 
	 * @param action акшен, который нужно начать.
	 */
	public void startAction(Action action);
	
	/**
	 * Запусть режим активности.
	 */
	public void startActive();
	
	/**
	 * Запустить каст скила.
	 * 
	 * @param startX стартовая координата.
	 * @param startY стартовая координата.
	 * @param startZ стартовая координата.
	 * @param skill скилл, который нужно скастануть.
	 * @param state состояние скила.
	 * @param heading разворот, необходимый для каста скила.
	 * @param targetX целевая координата каста.
	 * @param targetY целевая координата каста.
	 * @param targetZ целевая координата каста.
	 */
	public void startCast(float startX, float startY, float startZ, Skill skill, int state, int heading, float targetX, float targetY, float targetZ);
	
	/**
	 * Запустить каст скила.
	 * 
	 * @param skill скилл, который нужно скастануть.
	 * @param heading разворот, необходимый для каста скила.
	 * @param targetX целевая координата каста.
	 * @param targetY целевая координата каста.
	 * @param targetZ целевая координата каста.
	 */
	public void startCast(Skill skill, int heading, float targetX, float targetY, float targetZ);
	
	/**
	 * Собрать ресурс.
	 * 
	 * @param resourse ресурс, который нкжно собрать.
	 */
	public void startCollectResourse(ResourseInstance resourse);
	
	/**
	 * Запуск одевания/снятия итема.
	 * 
	 * @param index ячейка, в которой лежит нужный итем.
	 * @param itemId ид итема снимаего.
	 */
	public void startDressItem(int index, int itemId);
	
	/**
	 * Запустить эмоцию.
	 * 
	 * @param type тип эмоции.
	 */
	public void startEmotion(EmotionType type);
	
	/**
	 * Поднять итем.
	 * 
	 * @param item итем, который нужно поднять.
	 */
	public void startItemPickUp(ItemInstance item);
	
	/**
	 * Взаимодействовать с объектом.
	 * 
	 * @param object объект, с которым нужно взаимодействовать.
	 */
	public void startIteract(Character object);
	
	/**
	 * Идти в указаную точку.
	 * 
	 * @param heading разворот, с которым идти.
	 * @param type тип перемещения.
	 * @param targetX координата конечной точки.
	 * @param targetY координата конечной точки.
	 * @param targetZ координата конечной точки.
	 * @param broadCastMove отправлять ли пакет всем.
	 * @param sendSelfPacket отправлять ли себе пакет.
	 */
	public void startMove(float startX, float startY, float startZ, int heading, MoveType type, float targetX, float targetY, float targetZ, boolean broadCastMove, boolean sendSelfPacket);
	
	/**
	 * Идти в указаную точку.
	 * 
	 * @param startX стартовая координата.
	 * @param startY стартовая координата.
	 * @param startZ стартовая координата.
	 * @param heading разворот, с которым идти.
	 * @param type тип перемещения.
	 * @param targetX координата конечной точки.
	 * @param targetY координата конечной точки.
	 * @param targetZ координата конечной точки.
	 * @param broadCastMove отправлять ли пакет всем.
	 * @param sendSelfPacket отправлять ли себе пакет.
	 */
	public void startMove(int heading, MoveType type, float targetX, float targetY, float targetZ, boolean broadCastMove, boolean sendSelfPacket);
	
	/**
	 * Начать разговор с НПС.
	 * 
	 * @param npc нпс, с которым нужно начать разговор.
	 */
	public void startNpcSpeak(Npc npc);
	
	/**
	 * Начать отдых.
	 */
	public void startRest();
	
	/**
	 * Сказать в чат что-то.
	 * 
	 * @param text текст сообщения.
	 * @param type тип сообщения.
	 */
	public void startSay(String text, SayType type);
	
	/**
	 * Использовать итем.
	 * 
	 * @param item итем, который нужно использовать.
	 * @param heading направление.
	 * @param isHeb является ли итем хербом.
	 */
	public void startUseItem(ItemInstance item, int heading, boolean isHeb);
}
