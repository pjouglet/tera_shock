package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;

/**
 * Базовая модель фабрики заданий.
 *
 * @author Ronn
 */
public abstract class AbstractTaskFactory implements TaskFactory
{
	protected static final Logger log = Loggers.getLogger(TaskFactory.class);

	public AbstractTaskFactory(Node node){}
}
