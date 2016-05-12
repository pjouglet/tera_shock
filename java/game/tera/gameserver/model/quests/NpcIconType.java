package tera.gameserver.model.quests;

/**
 * Перечисление видов знаков над нпс.
 * 
 * @author Ronn
 */
public enum NpcIconType
{
	/**
	  		//1 красная звздочка
			//2 красный знак вопроса
			//3 красный знак впорсоа с закручеными стрелками
			//4 красный воскл знак
			//5 красный воскл знак со стрелками вокруг
			//6 жёлтая звёздочка
			//7 жёлтый воскл знак
			//8 жёлтый воскл знак вокруг со стрелками
			//9 жёлтый вокл знак
			//10 жёлтый вокл знак стрелки вокруг
			//11 синяя звёздочка
			//12 Синий знак вопроса
			//13 Синий знак вопроса с кружащимися полосками
			//14 Синий воскл знак 
			//15 Синий воскл знак с кружащимися полосками
			//16 зелёная звёздочка
			//17 зелёный знак вопроса
			//18 зелёный знак вопроса с кружащимися полосками
			//19 зелёный воскл знак 
			//20 зелёный воскл знак с кружащимися полосками
			//21 серый воскл знак
			//22 синяя спираль
	 */
	
	NONE,
	
	RED_STAR,
	RED_QUESTION,
	RED_QUESTION_SWITCH,
	RED_NOTICE,
	RED_NOTICE_SWITCH,
	
	YELLOW_STAR,
	YELLOW_QUESTION,
	YELLOW_QUESTION_SWITCH,
	YELLOW_NOTICE,
	YELLOW_NOTICE_SWITCH,

	BLUE_STAR,
	BLUE_QUESTION,
	BLUE_QUESTION_SWITCH,
	BLUE_NOTICE,
	BLUE_NOTICE_SWITCH,
	
	GREEN_STAR,
	GREEN_QUESTION,
	GREEN_QUESTION_SWITCH,
	GREEN_NOTICE,
	GREEN_NOTICE_SWITCH,
	
	GRAY_NOTICE,
	GRAY_SPIRAL;
}
