package crossword;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Crossword {

	private final ArrayList<Clue> acrossClues, downClues;
	private final String title;
	private final int size;

	public Crossword(String title, int size, ArrayList<Clue> acrossClues, ArrayList<Clue> downClues) {
		this.title = title;
		this.size = size;
		this.acrossClues = acrossClues;
		this.downClues = downClues;
	}

	/**
	 * Set all clues to unsolved
	 */
	public void resetCrossword() {
		Iterator<Clue> acrossIterator = acrossClues.iterator();
		Iterator<Clue> downIterator = downClues.iterator();
		while (acrossIterator.hasNext() || downIterator.hasNext()) {
			if (acrossIterator.hasNext())
				acrossIterator.next().setUnsolved();
			if (downIterator.hasNext())
				downIterator.next().setUnsolved();
		}
	}

	@Override
	public String toString() {
		return title + " (" + size + "x" + size + ")";
	}

	// Assume each crossword has a unique name

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Crossword other = (Crossword) obj;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	public String getTitle() {
		return title;
	}

	public int getSize() {
		return size;
	}

	public List<Clue> getAcrossClues() {
		return acrossClues;
	}

	public List<Clue> getDownClues() {
		return downClues;
	}

}
