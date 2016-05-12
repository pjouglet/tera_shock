package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.model.ai.npc.MessagePackage;

/**
 * Парсер пакетов сообщений для АИ НПС.
 *
 * @author Ronn
 */
public class DocumentMessagePackage extends AbstractDocument<Array<MessagePackage>>
{
	public DocumentMessagePackage(File file)
	{
		super(file);
	}

	@Override
	protected Array<MessagePackage> create()
	{
		return Arrays.toArray(MessagePackage.class);
	}

	@Override
	protected void parse(Document doc)
	{
		// создаем буферный список
		Array<String> messages = Arrays.toArray(String.class);

		// создаем парсер атрибутов
		VarTable vars = VarTable.newInstance();

		// перебираем элементы
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
		{
			// если это не элемент ,пропускаем
			if(lst.getNodeType() != Node.ELEMENT_NODE)
				continue;

			// если это список
			if("list".equals(lst.getNodeName()))
				// перебираем его элементы
				for(Node pckg = lst.getFirstChild(); pckg != null; pckg = pckg.getNextSibling())
				{
					// если это не элемент ,пропускаем
					if(pckg.getNodeType() != Node.ELEMENT_NODE)
						continue;

					// если это не пакет, пропускаем
					if(!"package".equals(pckg.getNodeName()))
						continue;

					// парсим атрибуты пакета
					vars.parse(pckg);

					// получаем название пакета
					String name = vars.getString("name");

					// очищаем буферный список
					messages.clear();

					// перебираем сообщения пакета
					for(Node msg = pckg.getFirstChild(); msg != null; msg = msg.getNextSibling())
					{
						// если это не элемент и не сообщение, пропускаем
						if(msg.getNodeType() != Node.ELEMENT_NODE || !"msg".equals(msg.getNodeName()))
							continue;

						// парсим атрибуты сообщения
						vars.parse(msg);

						// добавляем в список
						messages.add(vars.getString("text"));
					}

					// создаем и добавляем пакет
					result.add(new MessagePackage(name, messages.toArray(new String[messages.size()])));
				}
		}
	}
}
