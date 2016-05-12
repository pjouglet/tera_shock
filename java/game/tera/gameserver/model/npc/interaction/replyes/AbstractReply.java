package tera.gameserver.model.npc.interaction.replyes;

import org.w3c.dom.Node;

import rlib.logging.Logger;
import rlib.logging.Loggers;

/**
 * Базовая модель ответа на нажатие ссылки.
 *
 * @author Ronn
 */
public abstract class AbstractReply implements Reply
{
	protected static final Logger log = Loggers.getLogger(Reply.class);

	public AbstractReply(Node node)
	{
		super();
	}
}
