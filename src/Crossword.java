import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("serial")
class PuzzleGUI extends JFrame {

	/**
	 * Start the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new PuzzleGUI();
			}
		});
	}

	private List<Crossword> crosswords;
	private Crossword currentCrossword;
	private Cell[][] puzzle;
	private CrosswordGrid grid;
	private JLabel crosswordTitle;
	@SuppressWarnings("rawtypes")
	private JList acrossJList, downJList;

	public PuzzleGUI() {
		super("Crossword Puzzle");
		initGUI();
	}

	/**
	 * Initialise all GUI components
	 */
	private void initGUI() {
		initialiseCrosswords();

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		JPanel gridPanel = new JPanel(new BorderLayout(10, 10));
		// gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.PAGE_AXIS));
		crosswordTitle = new JLabel(currentCrossword.title, SwingConstants.CENTER);
		gridPanel.add(crosswordTitle, BorderLayout.NORTH);
		grid = new CrosswordGrid(puzzle);
		gridPanel.add(grid, BorderLayout.CENTER);
		panel.add(gridPanel);

		JPanel cluePanel = new JPanel(new GridLayout(2, 1, 5, 5));
		cluePanel.setPreferredSize(new Dimension(200, 200));
		cluePanel.add(new JScrollPane(acrossJList));
		cluePanel.add(new JScrollPane(downJList));
		panel.add(cluePanel);

		setContentPane(panel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// setPreferredSize(new Dimension(600, 600));
		setMinimumSize(new Dimension(600, 446));
		pack();
		setVisible(true);
	}

	private void initialiseCrosswords() {
		crosswords = new ArrayList<Crossword>();
		ArrayList<Clue> acrossClues = new ArrayList<Clue>();
		ArrayList<Clue> downClues = new ArrayList<Clue>();

		// @formatter:off
		acrossClues = new ArrayList<Clue>();
		downClues = new ArrayList<Clue>();

		acrossClues.add(new Clue(1, 1, 0, "Eager Involvement", "enthusiasm"));		acrossClues.add(new Clue(8, 0, 2, "Stream of water", "river"));
		acrossClues.add(new Clue(9, 6, 2, "Take as one's own", "adopt"));		acrossClues.add(new Clue(10, 0, 4, "Ball game", "golf"));
		acrossClues.add(new Clue(12, 5, 4, "Guard", "sentry"));		acrossClues.add(new Clue(14, 0, 6, "Language communication", "speech"));
		acrossClues.add(new Clue(17, 7, 6, "Fruit", "plum"));		acrossClues.add(new Clue(21, 0, 8, "In addition", "extra"));
		acrossClues.add(new Clue(22, 6, 8, "Boundary", "limit"));		acrossClues.add(new Clue(23, 0, 10, "Executives", "management"));

		downClues.add(new Clue(2, 2, 0, "Pertaining to warships", "naval"));		downClues.add(new Clue(3, 4, 0, "Solid", "hard"));
		downClues.add(new Clue(4, 6, 0, "Apportion", "share"));		downClues.add(new Clue(5, 8, 0, "Concerning", "about"));
		downClues.add(new Clue(6, 10, 0, "Friendly", "matey"));		downClues.add(new Clue(7, 0, 1, "Boast", "brag"));
		downClues.add(new Clue(11, 3, 4, "Enemy", "foe"));		downClues.add(new Clue(13, 7, 4, "Doze", "nap"));
		downClues.add(new Clue(14, 0, 6, "Water vapour", "steam"));		downClues.add(new Clue(15, 2, 6, "Consumed", "eaten"));
		downClues.add(new Clue(16, 4, 6, "Loud, resonant sound", "clang"));		downClues.add(new Clue(18, 8, 6, "Yellowish, citrus fruit", "lemon"));
		downClues.add(new Clue(19, 10, 6, "Mongrel dog", "mutt"));		downClues.add(new Clue(20, 6, 7, "Shut with force", "slam"));

		crosswords.add(new Crossword("An Example Crossword", 11, acrossClues, downClues));
		acrossClues = new ArrayList<Clue>();
		downClues = new ArrayList<Clue>();
		
		acrossClues.add(new Clue(1, 1, 0, "Showy", "OSTENTATIOUS"));		acrossClues.add(new Clue(9, 0, 2, "Carrying weapons", "ARMED"));
		acrossClues.add(new Clue(10, 6, 2, "Cocaine (anag)", "OCEANIC"));		acrossClues.add(new Clue(11, 0, 4, "Dull continuous pain", "ACHE"));
		acrossClues.add(new Clue(12, 5, 4, "Under an obligation", "BEHOLDEN"));		acrossClues.add(new Clue(14, 0, 6, "Cheap and showy", "TAWDRY"));
		acrossClues.add(new Clue(15, 7, 6, "Bewail", "LAMENT"));		acrossClues.add(new Clue(18, 0, 8, "Contrary", "OPPOSITE"));
		acrossClues.add(new Clue(20, 9, 8, "Sign of things to come", "OMEN"));		acrossClues.add(new Clue(22, 0, 10, "Impetuous person", "HOTHEAD"));
		acrossClues.add(new Clue(23, 8, 10, "Norwegian dramatist", "IBSEN"));		acrossClues.add(new Clue(24, 0, 12, "Rebuff", "COLD-SHOULDER"));

		downClues.add(new Clue(2, 2, 0, "One way or another", "SOMEHOW"));		downClues.add(new Clue(3, 4, 0, "Swirling current", "EDDY"));
		downClues.add(new Clue(4, 6, 0, "Gardener's tool", "TROWEL"));		downClues.add(new Clue(5, 8, 0, "Sacred writings of Islam", "THE KORAN"));
		downClues.add(new Clue(6, 10, 0, "Possessed", "OWNED"));		downClues.add(new Clue(7, 12, 0, "Best", "SECOND TO NONE"));
		downClues.add(new Clue(8, 0, 1, "Disastrous", "CATASTROPHIC"));		downClues.add(new Clue(13, 0, 1, "European Commission HQ", "BRUSSELS"));
		downClues.add(new Clue(16, 0, 1, "All together", "EN MASSE"));		downClues.add(new Clue(17, 0, 1, "Artist's workroom", "STUDIO"));
		downClues.add(new Clue(19, 0, 1, "Part of a flower ", "PETAL"));		downClues.add(new Clue(21, 0, 1, "English philosopher and economist, d. 1873", "MILL"));

		crosswords.add(new Crossword("Guardian 13,019", 13, acrossClues, downClues));

		loadCrossword(crosswords.get(1));
		// @formatter:on
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadCrossword(Crossword c) {
		currentCrossword = c;
		puzzle = new Cell[currentCrossword.size][currentCrossword.size];
		acrossJList = new JList(currentCrossword.acrossClues.toArray());
		acrossJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		downJList = new JList(currentCrossword.downClues.toArray());
		downJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (Clue clue : currentCrossword.acrossClues)
			loadClue(clue, true);
		for (Clue clue : currentCrossword.downClues)
			loadClue(clue, false);
	}

	private void loadClue(Clue clue, boolean across) {
		char[] answer = clue.answer.replaceAll("(-| )", "").toUpperCase().toCharArray();
		if (puzzle[clue.x][clue.y] == null)
			puzzle[clue.x][clue.y] = new Cell(answer[0], true, clue, null);
		else if (across)
			puzzle[clue.x][clue.y].acrossClue = clue;
		else
			puzzle[clue.x][clue.y].downClue = clue;

		for (int i = 1; i < answer.length; i++) {
			if (across) { // check if it needs to go across or down
				// needed for cells which are for both across and down clues
				if (puzzle[clue.x + i][clue.y] == null)
					puzzle[clue.x + i][clue.y] = new Cell(answer[i], clue, null);
				else
					puzzle[clue.x + i][clue.y].acrossClue = clue;
			} else {
				// needed for cells which are for both across and down clues
				if (puzzle[clue.x][clue.y + i] == null)
					puzzle[clue.x][clue.y + i] = new Cell(answer[i], null, clue);
				else
					puzzle[clue.x][clue.y + i].downClue = clue;
			}
		}
	}

	class CrosswordGrid extends JPanel {
		private static final int NONE = 0, ACROSS = 1, DOWN = 2;
		BufferedImage buffImg;
		private Cell[][] puzzle;
		double cellWidth;
		int xOffset, yOffset;
		int clueHighlight, highlightDirection;
		Point cellHighlight;

		public CrosswordGrid(Cell[][] puzzle) {
			this.puzzle = puzzle;
			setMinimumSize(new Dimension(400, 400));
			addMouseListener(new MouseAdapter() {

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
					setCell(e.getX(), e.getY());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub

				}
			});
			clueHighlight = 0;
			cellHighlight = new Point(-1, -1);
			highlightDirection = NONE;
		}

		public void paint(Graphics gr) {
			xOffset = yOffset = 0; // reset to prevent incorrect displacement
			// teo reduce calls to getHeight() and getWidth()
			int height = getHeight();
			int width = getWidth();
			// smallest dimension (height or width)
			int smallestDim = 0;
			if (width < getHeight()) {
				smallestDim = width;
				yOffset = (height - smallestDim) / 2;
			} else {
				smallestDim = height;
				xOffset = (width - smallestDim) / 2;
			}
			cellWidth = ((double) smallestDim) / ((double) puzzle.length);
			// smallestDim -= 10;
			buffImg = new BufferedImage(smallestDim, smallestDim, BufferedImage.TYPE_INT_ARGB);
			drawGrid(buffImg);
			Graphics2D g = (Graphics2D) gr;
			g.drawImage(buffImg, xOffset, yOffset, smallestDim, smallestDim, null);
			// g.drawImage(buffImg, 0, 0, smallestDim, smallestDim, 0, 0, smallestDim, smallestDim, null);
		}

		private void drawGrid(BufferedImage img) {
			Graphics2D g = (Graphics2D) img.getGraphics();
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			int fontSize = (int) (cellWidth - 17); // roughly the right value to
													// start at
			FontMetrics fm;
			// increase font size till height is roughly 6 less than cell height
			do {
				++fontSize;
				g.setFont(new Font("Arial", Font.BOLD, fontSize));
				fm = g.getFontMetrics();
				System.out.println(fm.getHeight() + " " + fontSize + " " + cellWidth);
			} while (fm.getHeight() < cellWidth - 6);
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0, 0, img.getWidth(), img.getHeight());

			for (int x = 0; x < puzzle.length; x++)
				for (int y = 0; y < puzzle.length; y++)
					drawCell(puzzle[x][y], x, y, g);
		}

		private void drawCell(Cell cell, int x, int y, Graphics2D g) {
			int xCoord = (int) Math.round((x * cellWidth) + 1);
			int yCoord = (int) Math.round((y * cellWidth) + 1);
			int width = (int) Math.round(cellWidth - 2);
			if (cell == null) {
				g.setColor(Color.BLACK);
				g.fillRect(xCoord, yCoord, width, width);
			} else {
				if ((cell.acrossClue != null && cell.acrossClue.number == clueHighlight)
						|| (cell.downClue != null && cell.downClue.number == clueHighlight))
					g.setColor(Color.GREEN);
				if (cellHighlight.equals(new Point(x, y)))
					g.setColor(Color.RED);
				else if ((cell.acrossClue != null && cell.acrossClue.number == clueHighlight)
						|| (cell.downClue != null && cell.downClue.number == clueHighlight))
					g.setColor(Color.GREEN);
				else
					g.setColor(Color.WHITE);
				g.fillRect(xCoord, yCoord, width, width);
				g.setColor(Color.BLACK);
				FontMetrics fm = g.getFontMetrics();
				g.drawString(cell.c, (int) (xCoord + width / 2 - fm.getStringBounds(cell.c, g).getWidth() / 2),
						(int) (yCoord + fm.getHeight() / 2.2 + width / 2));
				if (cell.isStart()) {
					Font temp = g.getFont(); // to reset font afterwards
					g.setFont(new Font("Arial Narrow", Font.BOLD, 9));
					g.drawString(cell.clueNum, xCoord + 1, yCoord + 7);
					g.setFont(temp);
				}
			}
		}

		private void setCell(int x, int y) {
			x = (int) ((x - xOffset) / cellWidth);
			y = (int) ((y - yOffset) / cellWidth);

			if (puzzle[x][y] != null) {
				Cell cell = puzzle[x][y];
				if (highlightDirection == NONE) {
					if (cell.acrossClue != null) {
						clueHighlight = cell.acrossClue.number;
						highlightDirection = ACROSS;
					} else if (cell.downClue != null) {
						clueHighlight = cell.downClue.number;
						highlightDirection = DOWN;
					}
				} else if (highlightDirection == ACROSS) {
					if (cell.downClue != null) {
						clueHighlight = cell.downClue.number;
						highlightDirection = DOWN;
					} else {
						clueHighlight = 0;
						highlightDirection = NONE;
					}
				} else {
					highlightDirection = NONE;
				}
				cellHighlight = new Point(x, y);
				repaint();
			}

		}
	}

	class Cell {
		private String c;
		private String clueNum; // only if first character
		private Clue acrossClue, downClue;

		public Cell(char c, Clue acrossClue, Clue downClue) {
			this(c, false, acrossClue, downClue);
		}

		public Cell(char c, boolean first, Clue acrossClue, Clue downClue) {
			this.c = Character.toString(c);
			if (first) {
				int clueNum = 0;
				if (acrossClue != null)
					clueNum = acrossClue.number;
				else
					clueNum = downClue.number;
				this.clueNum = Integer.toString(clueNum);
			}
			this.acrossClue = acrossClue;
			this.downClue = downClue;
		}

		private boolean isStart() {
			return clueNum != null;
		}
	}
}

class Crossword {

	final ArrayList<Clue> acrossClues, downClues;
	final String title;
	final int size;

	Crossword(String title, int size, ArrayList<Clue> acrossClues, ArrayList<Clue> downClues) {
		this.title = title;
		this.size = size;
		this.acrossClues = acrossClues;
		this.downClues = downClues;
	}
}

class Clue {
	final int number, x, y;
	final String clue, answer;

	Clue(int number, int x, int y, String clue, String answer) {
		this.number = number;
		this.x = x;
		this.y = y;
		this.clue = clue;
		this.answer = answer;
	}

	public int length() {
		return answer.length();
	}

	@Override
	public String toString() {
		return clue;
	}

}