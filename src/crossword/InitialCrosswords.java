package crossword;

import java.util.ArrayList;

public class InitialCrosswords {

	public static Crossword getCrossword1() {
		ArrayList<Clue> acrossClues = new ArrayList<Clue>();
		ArrayList<Clue> downClues = new ArrayList<Clue>();

		acrossClues.add(new Clue(1, 1, 0, "Eager Involvement", "enthusiasm"));
		acrossClues.add(new Clue(8, 0, 2, "Stream of water", "river"));
		acrossClues.add(new Clue(9, 6, 2, "Take as one's own", "adopt"));
		acrossClues.add(new Clue(10, 0, 4, "Ball game", "golf"));
		acrossClues.add(new Clue(12, 5, 4, "Guard", "sentry"));
		acrossClues.add(new Clue(14, 0, 6, "Language communication", "speech"));
		acrossClues.add(new Clue(17, 7, 6, "Fruit", "plum"));
		acrossClues.add(new Clue(21, 0, 8, "In addition", "extra"));
		acrossClues.add(new Clue(22, 6, 8, "Boundary", "limit"));
		acrossClues.add(new Clue(23, 0, 10, "Executives", "management"));

		downClues.add(new Clue(2, 2, 0, "Pertaining to warships", "naval"));
		downClues.add(new Clue(3, 4, 0, "Solid", "hard"));
		downClues.add(new Clue(4, 6, 0, "Apportion", "share"));
		downClues.add(new Clue(5, 8, 0, "Concerning", "about"));
		downClues.add(new Clue(6, 10, 0, "Friendly", "matey"));
		downClues.add(new Clue(7, 0, 1, "Boast", "brag"));
		downClues.add(new Clue(11, 3, 4, "Enemy", "foe"));
		downClues.add(new Clue(13, 7, 4, "Doze", "nap"));
		downClues.add(new Clue(14, 0, 6, "Water vapour", "steam"));
		downClues.add(new Clue(15, 2, 6, "Consumed", "eaten"));
		downClues.add(new Clue(16, 4, 6, "Loud, resonant sound", "clang"));
		downClues.add(new Clue(18, 8, 6, "Yellowish, citrus fruit", "lemon"));
		downClues.add(new Clue(19, 10, 6, "Mongrel dog", "mutt"));
		downClues.add(new Clue(20, 6, 7, "Shut with force", "slam"));

		return new Crossword("An Example Crossword", 11, acrossClues, downClues);
	}

	public static Crossword getCrossword2() {
		ArrayList<Clue> acrossClues = new ArrayList<Clue>();
		ArrayList<Clue> downClues = new ArrayList<Clue>();

		acrossClues.add(new Clue(1, 1, 0, "Showy", "OSTENTATIOUS"));
		acrossClues.add(new Clue(9, 0, 2, "Carrying weapons", "ARMED"));
		acrossClues.add(new Clue(10, 6, 2, "Cocaine (anag)", "OCEANIC"));
		acrossClues.add(new Clue(11, 0, 4, "Dull continuous pain", "ACHE"));
		acrossClues.add(new Clue(12, 5, 4, "Under an obligation", "BEHOLDEN"));
		acrossClues.add(new Clue(14, 0, 6, "Cheap and showy", "TAWDRY"));
		acrossClues.add(new Clue(15, 7, 6, "Bewail", "LAMENT"));
		acrossClues.add(new Clue(18, 0, 8, "Contrary", "OPPOSITE"));
		acrossClues.add(new Clue(20, 9, 8, "Sign of things to come", "OMEN"));
		acrossClues.add(new Clue(22, 0, 10, "Impetuous person", "HOTHEAD"));
		acrossClues.add(new Clue(23, 8, 10, "Norwegian dramatist", "IBSEN"));
		acrossClues.add(new Clue(24, 0, 12, "Rebuff", "COLD-SHOULDER"));

		downClues.add(new Clue(2, 2, 0, "One way or another", "SOMEHOW"));
		downClues.add(new Clue(3, 4, 0, "Swirling current", "EDDY"));
		downClues.add(new Clue(4, 6, 0, "Gardener's tool", "TROWEL"));
		downClues.add(new Clue(5, 8, 0, "Sacred writings of Islam", "THE KORAN"));
		downClues.add(new Clue(6, 10, 0, "Possessed", "OWNED"));
		downClues.add(new Clue(7, 12, 0, "Best", "SECOND TO NONE"));
		downClues.add(new Clue(8, 0, 1, "Disastrous", "CATASTROPHIC"));
		downClues.add(new Clue(13, 4, 5, "European Commission HQ", "BRUSSELS"));
		downClues.add(new Clue(16, 10, 6, "All together", "EN MASSE"));
		downClues.add(new Clue(17, 6, 7, "Artist's workroom", "STUDIO"));
		downClues.add(new Clue(19, 2, 8, "Part of a flower ", "PETAL"));
		downClues.add(new Clue(21, 8, 9, "English philosopher and economist, d. 1873", "MILL"));

		return new Crossword("Guardian 13,019", 13, acrossClues, downClues);
	}

}
