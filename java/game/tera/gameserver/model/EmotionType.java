package tera.gameserver.model;

/**
 * Перечисление типом эмоций.
 * 
 * @author Ronn
 */
public enum EmotionType
{
	NULL,
	/** осмотреться, мбо */
	INSPECTION, //1
	/** быстро осмотреться, моб */
	FAST_INSPECTION, //2
	NONE3, //3
	NULL4, //4
	/** приветствие */
	NONE5, //5
	NULL6, //6
	NULL7, //7
	NULL8,//8
	NULL9, //9
	NULL10, //10
	CHEMISTRY, //11
	/** крафт */
	SMITH, //12
	/** шитье */
	SEWS,//13
	BOW,//14	
	NULL15, //15
	/** приветсвие, игрок */
	HELLO, //16
	/** поклон, игрок */
	BUW, //17
	/** громкий смех, игрок */ 
	LAUGHTER, //18
	/** плач, игрок */
	CRYING, //19
	/** хвастаство, игрок */
	BOASTING, //20
	/** танец, игрок */
	DANCE, //21
	/** унылый, игрок */
	DULL, //22
	/** хлопание, игрок */
	SLAM, //23
	/** любовь, игрок */
	I_LOVE_YOU, //24
	/** задумчивость, игрок */
	MUSE, //25
	/** сердце */
	HEART, //26
	
	SHOW_ON_TERGET,//27
	
	POINT_FINGER,//28
	
	DISORDER,//29
	
	FAIL,//30
	
	INSPECT,//31
	
	/** разминать кулаки, игрок */
	KNEAD_FISTS,//32
	/** попрыгивание, игрок */
	BUMPING, //33
	/** разговор, игрок */
	TALK, //34
	/** рассказ, игрок */
	EXPLANATION, //35
	/** рассказ, игрок */
	EXPLANATION_2, //36
	/** каст, игрок */
	CAST, //37
	/** сесть, игрок */
	SIT_DOWN, //38
	/** встать, игрок */
	STAND_UP, //39
	NONE11, //40
	NONE12, //41
	
	
	
	
	
	
	NONE13, //30
	NONE14, //31
	NONE15, //32
	NONE16, //33
	NONE17, //34
	NONE18, //35
	NONE19, //36
	NONE20, //37
	NONE21, //38
	NONE22, //39
	NONE23, //40
	NONE24; //41
	
	/** список эмоций */
	public static EmotionType[] VALUES = values();
	
	/** кол-во эмоций */
	public static int SIZE = VALUES.length;
	
	/**
	 * Получить тим эмоции по индексу.
	 * 
	 * @param index тип эмоции.
	 * @return тип эмоции.
	 */
	public static final EmotionType valueOf(int index)
	{
		if(index < 0 || index >= SIZE)
			return EmotionType.INSPECTION;
		
		return VALUES[index];
	}
}
