package tera.gameserver.model.items;

/**
 * Перечисление стакуемых типов кристалов.
 *
 * @author Ronn
 */
public enum StackType
{
	NONE("none"),

	/** стак типы кристалов для оружия */
	SLAYING_WEAPON("slaying"),
	FURIOUS_WEAPON("furious"),
	FOCUS_WEAPON("focused"),
	VIRULENT_WEAPON("virulent"),
	BRUTAL_WEAPON("brutal"),
	CRUEL_WEAPON("cruel"),
	FORCEFUL_WEAPON("forceful"),
	SAVAGE_WEAPON("savage"),
	CUNNING_WEAPON("cunning"),
	INFUSED_WEAPON("infused"),
	GLISTENING_WEAPON("glistening"),
	SWIFT_WEAPON("swift"),
	BRILLIANT_WEAPON("brilliant"),
	SALIVATING_WEAPON("salivating"),
	THREATENING_WEAPON("threatening"),
	ACRIMONIOUS_WEAPON("acrimonious"),
	BACKBITING_WEAPON("backbiting"),
	HUNTERS_WEAPON("hunters"),
	CARVING_WEAPON("carving"),
	DOMINEERING_WEAPON("domineering"),
	MUTINOUS_WEAPON("mutinous"),
	SQUELCHING_WEAPON("squelching"),

	/** стак типы кристалов для шмоток */
	PROTECTIVE_ARMOR("protective"),
	RESOLUTE_ARMOR("resolute"),
	POISED_ARMOR("poised"),
	WARDING_ARMOR("warding"),
	INSPIRING_ARMOR("inspiring"),
	RELENTLESS_ARMOR("relentless"),
	FLEETFOT_ARMOR("fleetfoot"),
	VIGOROUS_ARMOR("vigorous"),
	GRIEVING_ARMOR("grieving"),
	NOBLESSE_ARMOR("noblesse"),
	ANARCHIC_ARMOR("anarchic"),
	DAUNTLESS_ARMOR("dauntless"),
	EMPYREAN_ARMOR("empyrean"),
	STALWART_ARMOR("stalwart"),
	SOOTHING_ARMOR("soothing");

	/**
	 * Получение нужного типа по названию в xml.
	 *
	 * @param name название в xml.
	 * @return соответствующий тип.
	 */
	public static StackType valueOfXml(String name)
	{
		for(StackType type : values())
			if(type.getXmlName().equals(name))
				return type;

		throw new IllegalArgumentException("no enum " + name);
	}

	/** название в хмл */
	private String xmlName;

	/**
	 * @param xmlName название в хмл.
	 */
	private StackType(String xmlName)
	{
		this.xmlName = xmlName;
	}

	/**
	 * @return the xmlName
	 */
	public final String getXmlName()
	{
		return xmlName;
	}
}
