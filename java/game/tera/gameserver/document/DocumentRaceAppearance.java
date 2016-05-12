package tera.gameserver.document;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import rlib.data.AbstractDocument;
import rlib.util.VarTable;

import tera.gameserver.model.base.Race;
import tera.gameserver.model.playable.PlayerAppearance;

/**
 * Парсер стандартных внешностей рас игроков с xml.
 *
 * @author Ronn
 */
public final class DocumentRaceAppearance extends AbstractDocument<Void>
{
	public DocumentRaceAppearance(File file)
	{
		super(file);
	}

	@Override
	protected Void create()
	{
		return null;
	}

	@Override
	protected void parse(Document doc)
	{
		VarTable vars = VarTable.newInstance();

		for(Node list = doc.getFirstChild(); list != null; list = list.getNextSibling())
			if("list".equals(list.getNodeName()))
				for(Node child = list.getFirstChild(); child != null; child = child.getNextSibling())
					if(child.getNodeType() == Node.ELEMENT_NODE && "appearance".equals(child.getNodeName()))
					{
						// парсим атрибуты
						vars.parse(child);

						// получаем расу внешности
						Race race = vars.getEnum("race", Race.class);

						// получаем пол внености
						String sex = vars.getString("sex");

						// парсим параметры внешности
						vars.parse(child, "set", "name", "value");

						// парсим внешность
						PlayerAppearance appearance = PlayerAppearance.fromXML(PlayerAppearance.getInstance(0), vars);

						switch(sex)
						{
							case "male": race.setMale(appearance); continue;
							case "female" : race.setFemale(appearance); continue;
							case "all" :
							{
								race.setFemale(appearance);
								race.setMale(appearance);
							}
						}
					}
	}
}