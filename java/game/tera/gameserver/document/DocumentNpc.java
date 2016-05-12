package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;

import tera.gameserver.model.skillengine.funcs.Func;
import tera.gameserver.parser.FuncParser;
import tera.gameserver.templates.NpcTemplate;

/**
 * Парсер шаблонов нпс с xml.
 *
 * @author Ronn
 * @created 16.03.2012
 */
public final class DocumentNpc extends AbstractDocument<Array<NpcTemplate>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentNpc(File file)
	{
		super(file);
	}

	@Override
	protected Array<NpcTemplate> create()
	{
		return Arrays.toArray(NpcTemplate.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node lst = doc.getFirstChild(); lst != null; lst = lst.getNextSibling())
			if("list".equals(lst.getNodeName()))
				for(Node sp = lst.getFirstChild(); sp != null; sp = sp.getNextSibling())
					if("template".equals(sp.getNodeName()))
					{
						NpcTemplate template = parseTemplate(sp);

						if(template == null)
							continue;

						result.add(template);
					}
	}

	/**
	 * @param node данные с хмл.
	 * @return набор функций.
	 */
	private final Func[] parseFuncs(Node node)
	{
		Array<Func> funcs = Arrays.toArray(Func.class);

		// получаем парсер функций
		FuncParser parser = FuncParser.getInstance();

		// парсим функции
		parser.parse(node, funcs, file);

		// сжимаем список
		funcs.trimToSize();

		return funcs.array();
	}

	/**
	 * @param nodes данные с хмл.
	 * @return новый темплейт.
	 */
	private final NpcTemplate parseTemplate(Node nodes)
	{
		return new NpcTemplate(VarTable.newInstance(nodes), parseFuncs(nodes));
	}
}
