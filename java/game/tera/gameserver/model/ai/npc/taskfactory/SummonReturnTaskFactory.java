package tera.gameserver.model.ai.npc.taskfactory;

import org.w3c.dom.Node;

import rlib.util.Strings;
import rlib.util.VarTable;
import tera.gameserver.model.Character;
import tera.gameserver.model.ai.npc.ConfigAI;
import tera.gameserver.model.ai.npc.MessagePackage;
import tera.gameserver.model.ai.npc.NpcAI;
import tera.gameserver.model.npc.Npc;
import tera.gameserver.tables.MessagePackageTable;
import tera.util.LocalObjects;

/**
 * Модель реализации фабрики заданий для суммона в режиме возвращения домой.
 *
 * @author Ronn
 */
public class SummonReturnTaskFactory extends AbstractTaskFactory
{
	/** пакет сообщений при начале возврата домой */
	private final  MessagePackage returnMessage;
	
	/** интервал в сообщениях */
	private final int messageInterval;
	
	public SummonReturnTaskFactory(Node node)
	{
		super(node);
		
		try
		{
			// парсим параметры
			VarTable vars = VarTable.newInstance(node, "set", "name", "val");

			this.messageInterval = vars.getInteger("messageInterval", 30000);
			
			MessagePackageTable messageTable = MessagePackageTable.getInstance();
			
			this.returnMessage = messageTable.getPackage(vars.getString("returnMessage", Strings.EMPTY));
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
		
		// если владельца нет, выходим
		if(owner == null)
			return;
		
		// получаем пакет сообщений
		MessagePackage messages = getReturnMessage();
		
		// если актор может нормально перемещаться
		if(actor.getRunSpeed() > 10)
		{
			// сообщения НПС
			String message = Strings.EMPTY;
			
			// если есть пакет сообщений и сообщение прошлое уже было давно
			if(messages != null && currentTime - ai.getLastMessage() > getMessageInterval())
			{
				// получаем сообщение
				message = messages.getRandomMessage();
				// обновляем время последнего сообщения
				ai.setLastMessage(currentTime + getMessageInterval());
			}
			
			// добавляем задание бежать в указанную точку
			ai.addMoveTask(owner.getX(), owner.getY(), owner.getZ(), true, message);
		}
	}
	
	/**
	 * @return сообщения при возврате домой.
	 */
	public MessagePackage getReturnMessage()
	{
		return returnMessage;
	}
	
	/**
	 * @return интервал в сообщениях.
	 */
	public int getMessageInterval()
	{
		return messageInterval;
	}
}
