package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;
import rlib.util.array.Array;
import rlib.util.array.Arrays;
import tera.gameserver.templates.ResourseTemplate;

/**
 * Парсер темплейтов ресурсов с xml.
 *
 * @author Ronn
 * @created 16.03.2012
 */
public final class DocumentResourse extends AbstractDocument<Array<ResourseTemplate>>
{
	/**
	 * @param file отпрасиваемый фаил.
	 */
	public DocumentResourse(File file)
	{
		super(file);
	}

	@Override
	protected Array<ResourseTemplate> create()
	{
		return Arrays.toArray(ResourseTemplate.class);
	}

	@Override
	protected void parse(Document doc)
	{
		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node temp = list.getFirstChild(); temp != null; temp = temp.getNextSibling())
					if(temp.getNodeType() == Node.ELEMENT_NODE && "template".equals(temp.getNodeName()))
						result.add(new ResourseTemplate(VarTable.newInstance(temp)));
	}
}
