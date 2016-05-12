package tera.gameserver.model.quests.classes;

import org.w3c.dom.Node;

import tera.gameserver.model.quests.QuestType;


/**
 * Модель сюжетного одноразового квеста.
 * 
 * @author Ronn
 */
public class StoryQuest extends AbstractQuest
{
	public StoryQuest(QuestType type, Node node)
	{
		super(type, node);
	}
}
