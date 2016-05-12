package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.quests.Quest;
import tera.gameserver.model.quests.QuestType;

/**
 * Парсер квестов с хмл.
 *
 * @author Ronn
 */
public class DocumentQuest extends AbstractDocument<Array<Quest>>
{
	public DocumentQuest(File file)
	{
		super(file);
	}

	@Override
	protected Array<Quest> create()
	{
		return Arrays.toArray(Quest.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node questNode = lst.getFirstChild(); questNode != null; questNode = questNode.getNextSibling())
					if("quest".equals(questNode.getNodeName()))
					{
						VarTable vars = VarTable.newInstance(questNode);

						Quest quest = vars.getEnum("type", QuestType.class).newInstance(questNode);

						if(quest != null)
							result.add(quest);
					}
	}
}
