package tera.gameserver.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Node;

import rlib.logging.Loggers;
import tera.gameserver.model.ai.npc.taskfactory.DefaultBattleTaskFactory;
import tera.gameserver.model.ai.npc.taskfactory.DefaultPatrolTaskFactory;
import tera.gameserver.model.ai.npc.taskfactory.DefaultRageTaskFactory;
import tera.gameserver.model.ai.npc.taskfactory.DefaultReturnTaskFactory;
import tera.gameserver.model.ai.npc.taskfactory.DefaultRunAwayTaskFactory;
import tera.gameserver.model.ai.npc.taskfactory.DefaultWaitTaskFactory;
import tera.gameserver.model.ai.npc.taskfactory.TaskFactory;
import tera.gameserver.model.ai.npc.thinkaction.DefaultBattleAction;
import tera.gameserver.model.ai.npc.thinkaction.DefaultPatrolAction;
import tera.gameserver.model.ai.npc.thinkaction.DefaultRageAction;
import tera.gameserver.model.ai.npc.thinkaction.DefaultReturnAction;
import tera.gameserver.model.ai.npc.thinkaction.DefaultRunAwayAction;
import tera.gameserver.model.ai.npc.thinkaction.DefaultWaitAction;
import tera.gameserver.model.ai.npc.thinkaction.ThinkAction;

/**
 * Перечисление состояний АИ.
 *
 * @author Ronn
 */
public enum NpcAIState
{
	/** в ожидании чего-то */
	WAIT(DefaultWaitAction.class, DefaultWaitTaskFactory.class),
	/** в патрулировании местности */
	PATROL(DefaultPatrolAction.class, DefaultPatrolTaskFactory.class),
	/** в возвращении на точку респа */
	RETURN_TO_HOME(DefaultReturnAction.class, DefaultReturnTaskFactory.class),
	/** в бою */
	IN_BATTLE(DefaultBattleAction.class, DefaultBattleTaskFactory.class),
	/** в ярости */
	IN_RAGE(DefaultRageAction.class, DefaultRageTaskFactory.class),
	/** убегает от противника */
	IN_RUN_AWAY(DefaultRunAwayAction.class, DefaultRunAwayTaskFactory.class);

	/** дефолтный думатель для стадии */
	private Constructor<? extends ThinkAction> think;
	/** дефолтная фабрика тасков */
	private Constructor<? extends TaskFactory> factory;

	private NpcAIState(Class<? extends ThinkAction> think, Class<? extends TaskFactory> factory)
	{
		try
		{
			this.think = think.getConstructor(Node.class);
			this.factory = factory.getConstructor(Node.class);
		}
		catch(NoSuchMethodException | SecurityException e)
		{
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @return фабрика тасков по умолчанию.
	 */
	public final TaskFactory getFactory()
	{
		try
		{
			return factory.newInstance((Node) null);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, e);
		}

		return null;
	}

	/**
	 * @return думатель по умолчани.
	 */
	public final ThinkAction getThink()
	{
		try
		{
			return think.newInstance((Node) null);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			Loggers.warning(this, e);
		}

		return null;
	}
}
