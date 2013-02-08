import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import javax.swing.JPanel;

@SuppressWarnings("serial")
class CrosswordGrid extends JPanel {

	public static final int NONE = 0; // direction to highlight
	public static final int ACROSS = 1;
	public static final int DOWN = 2;
	// direction to move selected cell (DOWN is already 2)
	public static final int UP = 3, LEFT = 4, RIGHT = 5;
	public final Color CELL_HIGHLIGHT_COLOR = new Color(3, 93, 179);
	public final Color CLUE_HIGHLIGHT_COLOR = new Color(82, 159, 225);
	BufferedImage buffImg;
	private Cell[][] puzzle;
	private double cellWidth, prevCellWidth; // prevCellWidth for preventing
												// recalculations
	private Font font; // font to use to draw cell letters
	private int xOffset, yOffset;
	private int clueToHighlight, highlightDirection;
	private Point cellToHighlight;
	private PuzzleGUI mainFrame;

	public CrosswordGrid(Cell[][] puzzle, PuzzleGUI mainFrame) {
		this.mainFrame = mainFrame;
		this.puzzle = puzzle;
		setMinimumSize(new Dimension(400, 400));
		setFocusable(true);
		addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				Point p = coordToCell(e.getX(), e.getY());
				highlightCell(p.x, p.y);
			}
		});
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP:
						move(UP);
						break;
					case KeyEvent.VK_BACK_SPACE:
						setCell(Character.toUpperCase(' '), false);
						break;
					case KeyEvent.VK_DOWN:
						move(DOWN);
						break;
					case KeyEvent.VK_LEFT:
						move(LEFT);
						break;
					case KeyEvent.VK_RIGHT:
						move(RIGHT);
						break;
					default:
						// a cell must be selected
						if (!cellToHighlight.equals(new Point(-1, -1))) {
							char c = Character.toUpperCase(e.getKeyChar());
							if (c >= 'A' && c <= 'Z')
								setCell(Character.toUpperCase(e.getKeyChar()));
						}
						break;
				}
			}

		});
		clueToHighlight = 0;
		cellToHighlight = new Point(-1, -1);
		highlightDirection = NONE;
		cellWidth = 0;
	}

	public void paint(Graphics gr) {
		xOffset = yOffset = 0; // reset to prevent incorrect displacement
		// to reduce calls to getHeight() and getWidth()
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
		prevCellWidth = cellWidth;
		cellWidth = ((double) smallestDim) / ((double) puzzle.length);
		buffImg = new BufferedImage(smallestDim, smallestDim, BufferedImage.TYPE_INT_ARGB);
		drawGrid(buffImg);
		Graphics2D g = (Graphics2D) gr;
		g.setColor(new Color(238, 238, 238));
		g.fillRect(0, 0, width, height);
		g.drawImage(buffImg, xOffset, yOffset, smallestDim, smallestDim, null);
	}

	/**
	 * Draws the grid to the passed {@link Image}
	 * 
	 * @param img
	 *            - image to draw to
	 */
	private void drawGrid(BufferedImage img) {
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		if (prevCellWidth != cellWidth) {
			int fontSize = (int) (cellWidth - 17); // roughly the right value to
			// start at
			FontMetrics fm;
			// increase font size till height is roughly 6 less than cell height
			do {
				++fontSize;
				g.setFont(new Font("arial", Font.BOLD, fontSize));
				fm = g.getFontMetrics();
			} while (fm.getHeight() < cellWidth - 6);
			font = g.getFont();
		} else {
			g.setFont(font);
		}
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		// draw all cells to image
		for (int x = 0; x < puzzle.length; x++)
			for (int y = 0; y < puzzle.length; y++)
				drawCell(puzzle[x][y], x, y, g);
	}

	/**
	 * Draw cells with the correct highlighting and text
	 * 
	 * @param cell
	 *            - cell to draw
	 * @param x
	 *            - x coordnate of cell
	 * @param y
	 *            - y coordinate of cell
	 * @param g
	 *            - {@link Graphics2D} to use
	 */
	private void drawCell(Cell cell, int x, int y, Graphics2D g) {
		// position and size of cell to draw
		int xCoord = (int) Math.round((x * cellWidth) + 1);
		int yCoord = (int) Math.round((y * cellWidth) + 1);
		int width = (int) Math.round(cellWidth - 2);
		// black squares
		if (cell == null) {
			g.setColor(Color.BLACK);
			g.fillRect(xCoord, yCoord, width, width);
		} else {// actual cell
			if (cellToHighlight.equals(new Point(x, y))) // cell clicked on
				g.setColor(CELL_HIGHLIGHT_COLOR);
			// clue clicked on
			else if ((cell.hasAcross() && cell.getAcrossClue().number == clueToHighlight && highlightDirection == ACROSS)
					|| (cell.hasDown() && cell.getDownClue().number == clueToHighlight && highlightDirection == DOWN))
				g.setColor(CLUE_HIGHLIGHT_COLOR);
			// other cell
			else
				g.setColor(Color.WHITE);
			g.fillRect(xCoord, yCoord, width, width);
			// draw character in cell
			g.setColor(Color.BLACK);
			FontMetrics fm = g.getFontMetrics();
			// complex positioning using FontMetrics
			g.drawString(cell.getC(), (int) (xCoord + width / 2 - fm
					.getStringBounds(cell.getC(), g).getWidth() / 2),
					(int) (yCoord + fm.getHeight() / 2.2 + width / 2));
			if (cell.isStart()) { // the little numbers
				Font temp = g.getFont(); // to reset font afterwards
				g.setFont(new Font("Arial Narrow", Font.PLAIN, 9));
				g.drawString(cell.getClueNum(), xCoord + 1, yCoord + 7);
				g.setFont(temp);
			}
		}
	}

	/**
	 * Convert mouse coordinates to coordinates of cells in array
	 * 
	 * @param x
	 *            - x mouse coordinate
	 * @param y
	 *            - y mouse coordinate
	 * @return Point containing x and y coordinates of cell clicked on
	 */
	private Point coordToCell(int x, int y) {
		x = (int) ((x - xOffset) / cellWidth);
		y = (int) ((y - yOffset) / cellWidth);
		return new Point(x, y);
	}

	/**
	 * Highlight a cell. Code to toggle between which clue gets highlighted
	 * isn't very tidy.
	 * 
	 * @param x
	 *            - x coordinate of cell to highlight
	 * @param y
	 *            - y coordinate of cell to highlight
	 */
	private void highlightCell(int x, int y) {
		// out of range of cell
		if (!(x >= 0 && x < puzzle.length && y >= 0 && y < puzzle.length)) {
			this.cellToHighlight = new Point(-1, -1);
			highlightClue(null, NONE);
			repaint();
			requestFocus();
			return;
		}
		Cell cell = puzzle[x][y];
		if (cell != null) {
			Point cellHighlight = new Point(x, y);

			if (this.cellToHighlight.equals(cellHighlight)) { // clicked same
																// cell again
				if (highlightDirection == ACROSS) {
					if (cell.hasDown()) {
						highlightClue(cell.getDownClue(), DOWN);
						this.cellToHighlight = cellHighlight;
					} else {
						highlightNone();
					}
				} else if (highlightDirection == DOWN) {
					highlightNone();
				}
			} else { // clicked different cell
				this.cellToHighlight = cellHighlight;
				if (cell.hasAcross()) {
					// if moving down already highlighted clue, continue to
					// highlight down clue
					// and not across
					if (cell.hasDown() && cell.getDownClue().number == clueToHighlight) {
						highlightClue(cell.getDownClue(), DOWN);
						this.cellToHighlight = cellHighlight;
					} else {
						highlightClue(cell.getAcrossClue(), ACROSS);
						this.cellToHighlight = cellHighlight;
					}
				} else if (cell.hasDown()) {
					highlightClue(cell.getDownClue(), DOWN);
					this.cellToHighlight = cellHighlight;
				} else {
					highlightNone();
				}
			}
		} else {// if clicked on black cell, highlight none.
			highlightNone();
		}
		repaint();
	}

	/**
	 * Convenience method to highlight nothing
	 */
	private void highlightNone() {
		this.cellToHighlight = new Point(-1, -1);
		highlightClue(null, NONE);
	}

	/**
	 * Highlight clue
	 * 
	 * @param clue
	 *            - {@link Clue}
	 * @param direction
	 *            - direction clue is (across, down)
	 */
	private void highlightClue(Clue clue, int direction) {
		highlightDirection = direction;
		if (direction == NONE) {
			clueToHighlight = 0;
		} else {
			clueToHighlight = clue.number;
		}
		selectClueInList(clue, direction); // highlight in list
		requestFocus();
	}

	/**
	 * Highlight clue from coordinates and number and direction
	 * 
	 * @param x
	 *            - x coordinate
	 * @param y
	 *            - y coordinate
	 * @param clueNum
	 *            - clue number
	 * @param direction
	 *            - direction of clue
	 */
	protected void onlyHighlightClue(int x, int y, int clueNum, int direction) {
		if (direction == ACROSS) {
			highlightClue(puzzle[x][y].getAcrossClue(), direction);
		} else {
			highlightClue(puzzle[x][y].getDownClue(), direction);
		}
		cellToHighlight = new Point(x, y);
		repaint();
		requestFocus();
	}

	/**
	 * Selects clue in JList
	 * 
	 * @param clue
	 *            - clue to select
	 * @param direction
	 *            - direction of the clue
	 */
	private void selectClueInList(Clue clue, int direction) {
		if (clue != null) {
			if (direction == ACROSS) {
				mainFrame.getDownJList().clearSelection();
				mainFrame.getAcrossJList().setSelectedValue(clue, true);
			} else if (direction == DOWN) {
				mainFrame.getAcrossJList().clearSelection();
				mainFrame.getDownJList().setSelectedValue(clue, true);
			}
		} else {
			mainFrame.getAcrossJList().clearSelection();
			mainFrame.getDownJList().clearSelection();
		}
	}

	/**
	 * Set the value of Cell, using current highlighted cell
	 * 
	 * @param character
	 *            - character to set to
	 */
	protected void setCell(char character) {
		// only if called by keylistener
		if (mainFrame.isConnected()) {// send data to server
			mainFrame.outStream(cellToHighlight.x, cellToHighlight.y, character,
					mainFrame.getUser());
		}
		setCell(character, true);
	}

	/**
	 * Set the value of Cell, using current highlighted cell.
	 * 
	 * @param character
	 *            - character to set to
	 * @param forward
	 *            - whether to move forward or backward
	 */
	protected void setCell(char character, boolean forward) {
		setCell(cellToHighlight.x, cellToHighlight.y, character, mainFrame.getUser(), forward, true);
	}

	/**
	 * Set the value of Cell, using passed coordinates.
	 * 
	 * @param x
	 *            - x coordinate of cell to set
	 * @param y
	 *            - y coordinate of cell to set
	 * @param character
	 *            - character to set to
	 * @param username
	 *            - username of user who set it
	 */
	protected void setCell(int x, int y, char character, String username) {
		setCell(x, y, character, username, true, true);
	}

	/**
	 * Set the value of Cell, using passed coordinates.
	 * 
	 * @param x
	 *            - x coordinate of cell to set
	 * @param y
	 *            - y coordinate of cell to set
	 * @param character
	 *            - character to set to
	 * @param username
	 *            - username of user who set it
	 * @param forward
	 *            - whether to move forward or backward
	 * @param move
	 *            - whether to move or not
	 */
	protected void setCell(int x, int y, char character, String username, boolean forward,
			boolean move) {
		int dir = NONE;
		if (move) {
			if (forward) {
				if (highlightDirection == ACROSS)
					dir = RIGHT;
				else
					dir = DOWN;
			} else {
				if (highlightDirection == ACROSS)
					dir = LEFT;
				else
					dir = UP;
			}
		}
		setCell(x, y, character, username, dir);
	}

	/**
	 * 
	 * @param x
	 *            - x coordinate of cell to set
	 * @param y
	 *            - y coordinate of cell to set
	 * @param character
	 *            - character to set to
	 * @param username
	 *            - username of user who set it
	 * @param direction
	 *            - direction to move
	 */
	protected void setCell(int x, int y, char c, String username, int direction) {
		puzzle[x][y].setC(Character.toString(c));
		checkClueSolved(puzzle[x][y], username);
		move(direction);
	}

	/**
	 * Check if clue is solved
	 * 
	 * @param cell
	 *            - cell edited, which migh cause clue to be solved
	 * @param username
	 *            - username of person who edited cell
	 */
	private void checkClueSolved(Cell cell, String username) {
		if (cell.hasAcross())
			checkClueSolved(cell.getAcrossClue(), ACROSS, username);
		if (cell.hasDown())
			checkClueSolved(cell.getDownClue(), DOWN, username);

	}

	/**
	 * Check if clue is solved
	 * 
	 * @param clue
	 *            - clue to check solved
	 * @param direct
	 *            - direction of clue
	 * @param username
	 *            - username of person who edited cell
	 */
	private void checkClueSolved(Clue clue, int direct, String username) {
		boolean solved = true;
		String direction;
		if (direct == ACROSS)
			direction = "across";
		else
			direction = "down";
		if (direct == ACROSS) { // check across
			int y = clue.y;
			for (int x = clue.x; x < clue.x + clue.length(); x++) {
				solved = solved && puzzle[x][y].getC().equals(puzzle[x][y].getAnswer());
				if (!solved) // clue not solved, stop checking
					break;
			}
		} else { // check down
			int x = clue.x;
			for (int y = clue.y; y < clue.y + clue.length(); y++) {
				solved = solved && puzzle[x][y].getC().equals(puzzle[x][y].getAnswer());
				if (!solved) // clue not solved, stop checking
					break;
			}
		}
		if (solved && !clue.isSolved()) {// if solved and clue wasn't already
											// solved
			clue.setSolved(username);
			if (mainFrame.supportOn())
				mainFrame.appendLog(Tools.getTime() + "\n\t" + clue.number + " " + direction
						+ " solved by " + username + "\n");
		} else if (!solved && clue.isSolved()) { // not solved and clue was
													// solved
			clue.setUnsolved();
		}
		if (solved && checkPuzzleSolved()) {// solved and whole puzzle solved
			mainFrame.appendLog(Tools.getTime() + "\n\tCrossword \""
					+ mainFrame.getCurrentCrossword() + "\" solved by " + username + "\n");
		}
	}

	/**
	 * Check if the whole {@link Crossword} has been solved
	 * 
	 * @return true if {@link Crossword} is solved
	 */
	private boolean checkPuzzleSolved() {
		boolean solved = true;
		Iterator<Clue> acrossIterator = mainFrame.getCurrentCrossword().acrossClues.iterator();
		Iterator<Clue> downIterator = mainFrame.getCurrentCrossword().downClues.iterator();
		while (acrossIterator.hasNext() || downIterator.hasNext()) {
			if (acrossIterator.hasNext())
				solved = solved && acrossIterator.next().isSolved();
			if (downIterator.hasNext())
				solved = solved && downIterator.next().isSolved();
			if (!solved) // crossword not solved, stop checking
				return false;
		}
		return true;
	}

	/**
	 * Move selected cell in direction passed
	 * 
	 * @param direction
	 *            - direction to move selected cell
	 */
	private void move(int direction) {
		int xMove = 0, yMove = 0;
		switch (direction) {
			case UP:
				yMove = -1;
				break;
			case DOWN:
				yMove = 1;
				break;
			case LEFT:
				xMove = -1;
				break;
			case RIGHT:
				xMove = 1;
				break;
			case NONE:
				repaint();
				return;
		}
		int x = cellToHighlight.x + xMove;
		int y = cellToHighlight.y + yMove;
		highlightCell(x, y);
	}

	/**
	 * Set 2D Cell array to puzzle, for when new Crossword is loaded
	 * 
	 * @param puzzle
	 *            - 2D Cell array to set
	 */
	public void setPuzzle(Cell[][] puzzle) {
		this.puzzle = puzzle;
		repaint();
	}
}
