package crossword;

import java.util.Date;

public class Clue {

	private final int number, x, y, length;
	private final String clue, answer;
	private boolean solved;
	private String solvedBy;
	private Date solvedAt;
	private String clueDisplay;

	public Clue(int number, int x, int y, String clue, String answer) {
		this.number = number;
		this.x = x;
		this.y = y;
		this.clue = clue;
		this.answer = answer;
		length = answer.replaceAll("(-| )", "").length();
		createClueDisplay();
	}

	/**
	 * Set the string to represent the clue, with correct number at start and
	 * the number of letters with hyphens and words representation
	 */
	private void createClueDisplay() {
		String temp = answer;
		String[] words = answer.split("(-| )");
		clueDisplay = number + ". " + clue + " (";
		int i;
		for (i = 0; i < words.length - 1; i++) {
			clueDisplay += words[i].length();
			temp = temp.replaceFirst(words[i], "");
			if (temp.charAt(0) == '-') {
				clueDisplay += "-";
			} else if (temp.charAt(0) == ' ') {
				clueDisplay += ",";
			}
		}
		clueDisplay += words[i].length();
		clueDisplay += ")";
	}

	protected void setSolved(String name) {
		setSolved(name, new Date());
	}

	public void setSolved(String name, Date date) {
		solved = true;
		solvedBy = name;
		solvedAt = date;
	}

	protected void setUnsolved() {
		solved = false;
		solvedBy = null;
		solvedAt = null;
	}

	public boolean isSolved() {
		return solved;
	}

	protected int length() {
		return length;
	}

	@Override
	public String toString() {
		return clueDisplay;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answer == null) ? 0 : answer.hashCode());
		result = prime * result + ((clue == null) ? 0 : clue.hashCode());
		result = prime * result + number;
		result = prime * result + x;
		result = prime * result + y;
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
		Clue other = (Clue) obj;
		if (answer == null) {
			if (other.answer != null)
				return false;
		} else if (!answer.equals(other.answer))
			return false;
		if (clue == null) {
			if (other.clue != null)
				return false;
		} else if (!clue.equals(other.clue))
			return false;
		if (number != other.number)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getLength() {
		return length;
	}

	public int getNumber() {
		return number;
	}

	public String getClue() {
		return clue;
	}

	public String getAnswer() {
		return answer;
	}

	public String getSolvedBy() {
		return solvedBy;
	}

	public Date getSolvedAt() {
		return solvedAt;
	}

	public String getClueDisplay() {
		return clueDisplay;
	}

}