package tera.gameserver.model.skillengine.conditions;

import org.w3c.dom.Node;

import tera.gameserver.model.skillengine.Condition;


/**
 * Фундаментальная модель кондишена.
 * 
 * @author Ronn
 */
public abstract class AbstractCondition implements Condition
{
	protected static final String MESSAGE = "Не выполнено условие.";
	
	private String msg = MESSAGE;
	
	@Override
	public String getMsg()
	{
		return msg;
	}

	@Override
	public Condition setMsg(Node msg)
	{
		if(msg == null)
			return this;
		
		this.msg = msg.getNodeValue();
		
		return this;
	}
}
