package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.geom.Angles;
import rlib.geom.Coords;
import rlib.util.Rnd;
import rlib.util.Strings;
import rlib.util.VarTable;

import tera.gameserver.manager.GeoManager;
import tera.gameserver.manager.PacketManager;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.MessagePackage;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.network.serverpackets.NotifyCharacter.NotifyType;
import tera.gameserver.tables.MessagePackageTable;
import tera.util.LocalObjects;

/**
 * Модель реализации фабрики заданий для суммона в режиме ожидания.
 *
 * @author Ronn
 */
public class SummonWaitTaskFactory extends AbstractTaskFactory
{
	/** сообщения брождения */
	protected final MessagePackage walkMessage;
	/** сообщения догонки */
	protected final MessagePackage followMessage;

	/** минимальный радиус случайного брождения */
	protected final int randomWalkMinRange;
	/** максимальный радиус случайного брождения */
	protected final int randomWalkMaxRange;

	/** минимальный интервал случайного движения */
	protected final int randomWalkMinDelay;
	/** максимальный интервал случайного движения */
	protected final int randomWalkMaxDelay;

	/** интервал сообщений */
	protected final int messageInterval;

	public SummonWaitTaskFactory(Node node)
	{
		super(node);

		try
		{
			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.messageInterval = vars.getInteger("messageInterval", 120000);

			this.randomWalkMinRange = vars.getInteger("randomWalkMinRange", ConfigAI.DEFAULT_RANDOM_MIN_WALK_RANGE);
			this.randomWalkMaxRange = vars.getInteger("randomWalkMaxRange", ConfigAI.DEFAULT_RANDOM_MAX_WALK_RANGE);

			this.randomWalkMinDelay = vars.getInteger("randomWalkMinDelay", ConfigAI.DEFAULT_RANDOM_MIN_WALK_DELAY);
			this.randomWalkMaxDelay = vars.getInteger("randomWalkMaxDelay", ConfigAI.DEFAULT_RANDOM_MAX_WALK_DELAY);

			// получаем таблицу сообщений
			MessagePackageTable messageTable = MessagePackageTable.getInstance();

			this.followMessage = messageTable.getPackage(vars.getString("followMessage", Strings.EMPTY));
			this.walkMessage = messageTable.getPackage(vars.getString("walkMessage", Strings.EMPTY));
		}
		catch(Exception e)
		{
			log.warning(this, e);
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

		if(actor.isBattleStanced())
			actor.stopBattleStance();

		// получаем расстояние максимально допустимое для расхождения
		int randomWalkMaxRange = getRandomWalkMaxRange();

		// если владелец далеко отошел
		if(!owner.isInRange(actor, randomWalkMaxRange))
		{
			// определяем минимальное расстояние до владельца
			int minDist = (int) (actor.getGeomRadius() + owner.getGeomRadius()) + 20;

			// определяем сторону у которой стоять хотим
			float radians = Angles.headingToRadians(Rnd.nextInt(65000));

			// определяем целевую точку на плоскости
			float newX = Coords.calcX(owner.getX(), minDist, radians);
			float newY = Coords.calcY(owner.getY(), minDist, radians);

			// получаем менеджера геодаты
			GeoManager geoManager = GeoManager.getInstance();

			// определяем высоту в том месте
			float newZ = geoManager.getHeight(actor.getContinentId(), newX, newY, actor.getZ());

			// заготовка сообщения
			String message = Strings.EMPTY;

			// получаем пакет сообщений
			MessagePackage followMessage = getFollowMessage();

			// если пакет есть и интервал выполнен
			if(followMessage != null && currentTime - ai.getLastMessage() > getMessageInterval())
			{
				// получаем сообщение
				message = followMessage.getRandomMessage();
				// обновляем время
				ai.setLastMessage(currentTime + getMessageInterval());
			}

			// добавляем задание придти туда
			ai.addMoveTask(newX, newY, newZ, true, message);
			return;
		}

		// если нпс должен бродить и наступило время следующего хождения
		if(randomWalkMaxRange > 0 && currentTime > ai.getNextRandomWalk())
		{
			// если последний раз иконка блистала более 5 сек назад
			if(currentTime - ai.getLastNotifyIcon() > 5000)
			{
				// отображаем иконку думания
				PacketManager.showNotifyIcon(actor, NotifyType.NOTICE_THINK);
				// обновляем время
				ai.setLastNotifyIcon(currentTime);
			}

			// обновляем время следующего
			ai.setNextRandomWalk(currentTime + Rnd.nextInt(getRandomWalkMinDelay(), getRandomWalkMaxDelay()));

			// расчитываем расстояние брождения
			int distance = Rnd.nextInt(getRandomWalkMinRange(),getRandomWalkMaxRange());
			// рассчитываем случацное направление
			int newHeading = Rnd.nextInt(65000);

			// рассчитываем целевую точку
			float newX = Coords.calcX(owner.getX(), distance, newHeading);
			float newY = Coords.calcY(owner.getY(), distance, newHeading);

			// получаем менеджер геодаты
			GeoManager geoManager = GeoManager.getInstance();

			float newZ = geoManager.getHeight(actor.getContinentId(), newX, newY, owner.getZ());

			// заготовка сообщения
			String message = Strings.EMPTY;

			// получаем пакет сообщений
			MessagePackage walkMessage = getWalkMessage();

			// если пакет есть и интервал выполнен
			if(walkMessage != null && currentTime - ai.getLastMessage() > getMessageInterval())
			{
				// получаем сообщение
				message = walkMessage.getRandomMessage();
				// обновляем время
				ai.setLastMessage(currentTime + getMessageInterval());
			}

			// добавляем задание придти туда
			ai.addMoveTask(newX, newY, newZ, true, message);
		}
	}

	/**
	 * @return максимальный период брождения.
	 */
	protected final int getRandomWalkMaxDelay()
	{
		return randomWalkMaxDelay;
	}

	/**
	 * @return максимальный радиус брождения.
	 */
	protected final int getRandomWalkMaxRange()
	{
		return randomWalkMaxRange;
	}

	/**
	 * @return минимальный период брождения.
	 */
	protected final int getRandomWalkMinDelay()
	{
		return randomWalkMinDelay;
	}

	/**
	 * @return минимальный радиус брождения.
	 */
	protected final int getRandomWalkMinRange()
	{
		return randomWalkMinRange;
	}

	/**
	 * @return пакет движения при догонке.
	 */
	public MessagePackage getFollowMessage()
	{
		return followMessage;
	}

	/**
	 * @return интервал сообщений.
	 */
	public int getMessageInterval()
	{
		return messageInterval;
	}

	/**
	 * @return пакет сообщений при брождении.
	 */
	public MessagePackage getWalkMessage()
	{
		return walkMessage;
	}
}
