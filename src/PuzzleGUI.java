import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/*
 * I have implemented network play and included my server file with source code for you to use.
 * Currently my program defaults to linuxproj where I am already running a server which you can use.
 * To use linuxproj as a server, you either have to be on a ECS computer or on the ECS VPN.
 * Alternatively, compile and run the server yourself and if testing the server from a single computer,
 * set the host to 'localhost' or find out the IP of the computer the server is running on if on two
 * separate computer on a network.
 * 
 * My network play currently only supports both users already on the same crossword and it doesn't 
 * synchronise data entered before entering network mode as I haven't had time to implement this.
 * 
 */
@SuppressWarnings("serial")
public class PuzzleGUI extends JFrame {

	/**
	 * Start the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// execute on EDT
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				new PuzzleGUI();
			}
		});
	}

	public static final String CW_EXT = "cw";// CrossWord
	public static final String CW_SAVE_EXT = "cws";// CrossWord Save
	private List<Crossword> crosswords; // available crosswords
	private Crossword currentCrossword;
	private Cell[][] puzzle; // stores all cells for the grid
	private CrosswordGrid grid; // JPanel which renders the grid
	private JLabel crosswordTitle;
	private JList acrossJList, downJList;
	private JTextArea logArea;
	private String userName;
	private Window window; // to reference this JFrame
	private boolean solvedSupport;

	// Networking objects
	private Socket socket = null;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private boolean connected;
	private Thread input;

	public PuzzleGUI() {
		super("Crossword Puzzle");
		initGUI();
	}

	/**
	 * Initialise all GUI components
	 */
	private void initGUI() {
		// setup before initialise crossword, because these items are accessed
		// in there
		acrossJList = new JList();
		acrossJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		acrossJList.setCellRenderer(new ClueRenderer());
		acrossJList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				JList source = (JList) e.getSource();
				if (!source.isSelectionEmpty()) {
					// highlight clue in grid when clicked on in JList
					int selected = source.getSelectedIndex();
					Clue clue = currentCrossword.acrossClues.get(selected);
					grid.onlyHighlightClue(clue.x, clue.y, clue.number, CrosswordGrid.ACROSS);
				}
			}
		});

		downJList = new JList();
		downJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		downJList.setCellRenderer(new ClueRenderer());
		downJList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!((JList) e.getSource()).isSelectionEmpty()) {
					// highlight clue in grid when clicked on in JList
					int selected = ((JList) e.getSource()).getSelectedIndex();
					Clue clue = currentCrossword.downClues.get(selected);
					grid.onlyHighlightClue(clue.x, clue.y, clue.number, CrosswordGrid.DOWN);
				}
			}
		});
		crosswordTitle = new JLabel("", SwingConstants.CENTER);

		initialiseCrosswords();
		window = this;

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel crosswordPanel = new JPanel();
		crosswordPanel.setLayout(new BoxLayout(crosswordPanel, BoxLayout.X_AXIS));
		JPanel gridPanel = new JPanel(new BorderLayout(10, 10));
		gridPanel.add(crosswordTitle, BorderLayout.NORTH);
		grid = new CrosswordGrid(puzzle, this);
		gridPanel.add(grid, BorderLayout.CENTER);
		crosswordPanel.add(gridPanel);

		JPanel cluePanel = new JPanel(new GridLayout(2, 1, 5, 5));
		cluePanel.setPreferredSize(new Dimension(220, 200));

		JPanel acrossCluesPanel = new JPanel(new BorderLayout());
		acrossCluesPanel.add(new JLabel("Across Clues", SwingConstants.CENTER), BorderLayout.NORTH);
		acrossCluesPanel.add(new JScrollPane(acrossJList), BorderLayout.CENTER);

		JPanel downCluesPanel = new JPanel(new BorderLayout());
		downCluesPanel.add(new JLabel("Down Clues", SwingConstants.CENTER), BorderLayout.NORTH);
		downCluesPanel.add(new JScrollPane(downJList), BorderLayout.CENTER);

		cluePanel.add(acrossCluesPanel);
		cluePanel.add(downCluesPanel);
		crosswordPanel.add(cluePanel);
		panel.add(crosswordPanel);

		JPanel textPanel = new JPanel(new BorderLayout());

		JPanel chatPanel = new JPanel();
		chatPanel.add(new JLabel("Chat:"));
		final JTextField chatField = new JTextField(30);
		chatField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				// Send chat to server to broadcast
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (out != null)
						out.println("chat:" + userName + ":" + chatField.getText());
					else
						JOptionPane.showMessageDialog(window, "No network connection", "Error",
								JOptionPane.ERROR_MESSAGE);
					logArea.append(Tools.getTime() + "\n\t" + userName + " says: "
							+ chatField.getText() + "\n");
					chatField.setText("");
				}
			}
		});
		chatPanel.add(chatField);
		textPanel.add(chatPanel, BorderLayout.NORTH);

		logArea = new JTextArea();
		logArea.setEditable(false);
		JScrollPane textAreaPanel = new JScrollPane(logArea);
		textAreaPanel.setAutoscrolls(true);
		textAreaPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		textPanel.add(textAreaPanel, BorderLayout.CENTER);
		textPanel.setPreferredSize(new Dimension(500, 140));
		textPanel.setMinimumSize(new Dimension(500, 400));
		textPanel.setMaximumSize(new Dimension(2000, 400));

		panel.add(textPanel);

		setContentPane(panel);
		// setup menubar
		JMenuBar menuBar = createMenuBar();
		setJMenuBar(menuBar);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 600));
		pack();
		setVisible(true);
		// set username on start
		do {
			setUser();
			if (userName == null)
				JOptionPane.showMessageDialog(window, "Must enter a name", "Error",
						JOptionPane.ERROR_MESSAGE);
		} while (userName == null);// force user to set a name
	}

	/**
	 * Adds a {@link Crossword} to available {@link Crossword}
	 * 
	 * @param crossword
	 *            - {@link Crossword} to add
	 */
	private void addCrossword(Crossword crossword) {
		if (crossword != null && !crosswords.contains(crossword))
			crosswords.add(crossword);
	}

	/**
	 * Choose a {@link Crossword} from a list of the available ones
	 * 
	 * @return {@link Crossword} the user selected in the list or null if none
	 *         selected
	 */
	private Crossword chooseCrossword() {
		JList list = new JList(crosswords.toArray());
		JScrollPane pane = new JScrollPane(list);
		pane.setPreferredSize(new Dimension(160, 200));
		list.setLayoutOrientation(JList.VERTICAL);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		int option = JOptionPane.showOptionDialog(window, pane, "Choose Crossword",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
		if (option == JOptionPane.OK_OPTION)
			return (Crossword) list.getSelectedValue();
		else
			return null;

	}

	/**
	 * Set the user
	 */
	private void setUser() {
		String option = JOptionPane.showInputDialog(window, "Name: ");
		// ignore cancel or empty string
		if (option != null && !option.equals("")) {
			userName = option;
			logArea.append(Tools.getTime() + "\n\tCurrent user: " + userName + "\n");
		}
	}

	/**
	 * Create 2 {@link Crossword}s and add them to my list of crosswords, then
	 * load a {@link Crossword}
	 */
	private void initialiseCrosswords() {
		crosswords = new ArrayList<Crossword>();
		ArrayList<Clue> acrossClues = new ArrayList<Clue>();
		ArrayList<Clue> downClues = new ArrayList<Clue>();
		// to shorten code length
		// @formatter:off
		acrossClues = new ArrayList<Clue>();
		downClues = new ArrayList<Clue>();

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

		crosswords.add(new Crossword("An Example Crossword", 11, acrossClues, downClues));

		acrossClues = new ArrayList<Clue>();
		downClues = new ArrayList<Clue>();

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

		crosswords.add(new Crossword("Guardian 13,019", 13, acrossClues, downClues));

		currentCrossword = crosswords.get(1);
		loadCrossword(crosswords.get(1));
		// @formatter:on
	}

	/**
	 * 
	 * ListCellRenderer for {@link Clue} {@link JList}s. It highlights solved
	 * {@link Clue}s in green
	 * 
	 * @author hja1g11
	 * 
	 */
	public class ClueRenderer extends DefaultListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			// set background to green if solve support is on and clue is solved
			if (solvedSupport && ((Clue) value).solved)
				setBackground(new Color(151, 206, 139));
			if (isSelected)
				setBorder(BorderFactory.createLineBorder(new Color(99, 130, 191)));

			return this;
		}

	}

	/**
	 * Load a {@link Crossword}
	 * 
	 * @param crossword
	 *            - {@link Crossword} to load
	 */
	private void loadCrossword(Crossword crossword) {
		currentCrossword.resetCrossword(); // reset data so next load is clear
		currentCrossword = crossword;
		crosswordTitle.setText(crossword.title);
		puzzle = new Cell[currentCrossword.size][currentCrossword.size];

		acrossJList.setListData(currentCrossword.acrossClues.toArray());
		downJList.setListData(currentCrossword.downClues.toArray());

		// load clues
		for (Clue clue : currentCrossword.acrossClues)
			loadClue(clue, true);
		for (Clue clue : currentCrossword.downClues)
			loadClue(clue, false);
		if (grid != null) {// not first time
			grid.setPuzzle(puzzle);
		}
	}

	/**
	 * Load a given {@link Clue} into the puzzle array
	 * 
	 * @param clue
	 *            - {@link Clue} to load
	 * @param across
	 *            - true if across {@link Clue}, false if down {@link Clue}
	 */
	private void loadClue(Clue clue, boolean across) {
		char[] answer = clue.answer.replaceAll("(-| )", "").toUpperCase().toCharArray();
		// set first char separately
		char character = ' '; // this is to allow loading of state of Crossword
		if (clue.isSolved())
			character = answer[0];
		if (puzzle[clue.x][clue.y] == null)
			puzzle[clue.x][clue.y] = new Cell(character, answer[0], null, null);

		if (across) {
			puzzle[clue.x][clue.y].setAcrossClue(clue);
			// only if already empty, set to character
			if (puzzle[clue.x][clue.y].getC().equals(" ") && clue.isSolved())
				puzzle[clue.x][clue.y].setC(Character.toString(character));
		} else {
			puzzle[clue.x][clue.y].setDownClue(clue);
			// only if already empty, set to character
			if (puzzle[clue.x][clue.y].getC().equals(" ") && clue.isSolved())
				puzzle[clue.x][clue.y].setC(Character.toString(character));
		}
		puzzle[clue.x][clue.y].setClueNum(Integer.toString(clue.number));
		// set rest of chars
		for (int i = 1; i < answer.length; i++) {
			character = ' ';
			if (clue.isSolved())
				character = answer[i];
			// check if it needs to go across or down
			// needed for cells which are for both across and down clues
			if (across) {
				if (puzzle[clue.x + i][clue.y] == null)
					puzzle[clue.x + i][clue.y] = new Cell(character, answer[i], clue, null);
				else {
					puzzle[clue.x + i][clue.y].setAcrossClue(clue);
					// only if already empty, set to character
					if (puzzle[clue.x][clue.y].getC().equals(" ") && clue.isSolved())
						puzzle[clue.x][clue.y].setC(Character.toString(character));
				}
			} else {
				// needed for cells which are for both across and down clues
				if (puzzle[clue.x][clue.y + i] == null)
					puzzle[clue.x][clue.y + i] = new Cell(character, answer[i], null, clue);
				else {
					puzzle[clue.x][clue.y + i].setDownClue(clue);
					// only if already empty,set to a
					if (puzzle[clue.x][clue.y].getC().equals(" ") && clue.isSolved())
						puzzle[clue.x][clue.y].setC(Character.toString(character));
				}
			}
		}
	}

	/**
	 * Create the {@link JMenuBar}
	 * 
	 * @return JMenuBar completely setup
	 */
	private JMenuBar createMenuBar() {
		// setup menubar
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");

		JMenuItem loadProgress = new JMenuItem();
		loadProgress.setAction(new AbstractAction("Open Saved Game") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String[] extensionAllowed = { CW_SAVE_EXT };
				File file = CrosswordIO.getFile(window, extensionAllowed, true);
				if (file == null) // no file selected
					return;
				Crossword c = CrosswordIO.readPuzzle(file);
				if (c != null) {
					addCrossword(c);
					loadCrossword(c);
				} else {
					JOptionPane.showMessageDialog(window, "Error occurred while reading the file",
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		loadProgress.setMnemonic(KeyEvent.VK_O);
		loadProgress.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		fileMenu.add(loadProgress);

		JMenuItem saveProgress = new JMenuItem();
		saveProgress.setAction(new AbstractAction("Save Game") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String[] extensionAllowed = { CW_SAVE_EXT };
				File file = CrosswordIO.getFile(window, extensionAllowed, false);
				if (file != null) // no file selected
					CrosswordIO.writePuzzle(file, currentCrossword);
			}
		});
		saveProgress.setMnemonic(KeyEvent.VK_S);
		saveProgress.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		fileMenu.add(saveProgress);

		fileMenu.addSeparator();

		JMenuItem resetCrossword = new JMenuItem();
		resetCrossword.setAction(new AbstractAction("Reset Crossword") {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentCrossword != null) {
					currentCrossword.resetCrossword();

					// reset Cell values
					for (Cell[] cellarr : puzzle)
						for (Cell cell : cellarr)
							if (cell != null)
								cell.setC("");
					repaint();
				}
			}
		});
		resetCrossword.setMnemonic(KeyEvent.VK_R);
		resetCrossword.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		fileMenu.add(resetCrossword);

		JMenuItem loadCrossword = new JMenuItem();
		loadCrossword.setAction(new AbstractAction("Load Crossword") {

			@Override
			public void actionPerformed(ActionEvent e) {
				Crossword c = chooseCrossword();
				if (c != null)
					loadCrossword(c);
			}
		});
		loadCrossword.setMnemonic(KeyEvent.VK_L);
		loadCrossword.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		fileMenu.add(loadCrossword);

		fileMenu.addSeparator();

		JMenuItem importCrossword = new JMenuItem();
		importCrossword.setAction(new AbstractAction("Import Crossword") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String[] extensionAllowed = { CW_EXT };
				File file = CrosswordIO.getFile(window, extensionAllowed, true);
				if (file != null) { // no file selected
					Crossword c = CrosswordIO.importPuzzle(file);
					if (c != null)
						addCrossword(c);
					else
						JOptionPane.showMessageDialog(window,
								"Error occurred while reading the file", "Error",
								JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		importCrossword.setMnemonic(KeyEvent.VK_I);
		importCrossword
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		fileMenu.add(importCrossword);

		JMenuItem exportCrossword = new JMenuItem();
		exportCrossword.setAction(new AbstractAction("Export Crossword") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String[] extensionAllowed = { CW_EXT };
				Crossword c = chooseCrossword();
				File file = CrosswordIO.getFile(window, extensionAllowed, false);
				if (file != null) // no file selected
					if (c != null)
						CrosswordIO.exportPuzzle(file, c);
			}
		});
		exportCrossword.setMnemonic(KeyEvent.VK_E);
		exportCrossword
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		fileMenu.add(exportCrossword);

		fileMenu.addSeparator();

		JMenuItem closeWindow = new JMenuItem();
		closeWindow.setAction(new AbstractAction("Close") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// close window
				WindowEvent wev = new WindowEvent(window, WindowEvent.WINDOW_CLOSING);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
			}
		});
		closeWindow.setMnemonic(KeyEvent.VK_Q);
		closeWindow.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		fileMenu.add(closeWindow);

		menuBar.add(fileMenu);

		JMenu optionsMenu = new JMenu("Options");

		JMenuItem setUser = new JMenuItem();
		setUser.setAction(new AbstractAction("Set User") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// set username
				setUser();
			}
		});
		setUser.setMnemonic(KeyEvent.VK_U);
		setUser.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		optionsMenu.add(setUser);

		JCheckBoxMenuItem toggleSolvedSupport = new JCheckBoxMenuItem();
		toggleSolvedSupport.setAction(new AbstractAction("Solved Help") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// toggle solved support
				solvedSupport = !solvedSupport;
				window.repaint();
			}
		});
		toggleSolvedSupport.setMnemonic(KeyEvent.VK_H);
		toggleSolvedSupport.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
				ActionEvent.CTRL_MASK));
		optionsMenu.add(toggleSolvedSupport);

		JMenuItem anagram = new JMenuItem();
		final SpinnerModel numWordsModel = new SpinnerNumberModel(1, // initial
																		// value
				1, // min
				6, // max
				1); // step
		final JSpinner numWordsSpinner = new JSpinner(numWordsModel);
		anagram.setAction(new AbstractAction("Anagrams") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// easy anagram using a web page
				String word = JOptionPane.showInputDialog(window, "Word or phrase:");
				if (word == null || word.equals(""))
					return;
				// remove none letter or space chars
				word = word.replaceAll("[^a-zA-Z ]", "");
				if (word.equals(""))
					return;
				word = word.replaceAll(" ", "+");
				int option = JOptionPane.showOptionDialog(window, numWordsSpinner, "Number Words",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null,
						null);
				int numWords;
				if (option == JOptionPane.OK_OPTION) {
					numWords = ((Integer) numWordsModel.getValue());
				} else {
					return;
				}
				numWordsSpinner.setValue(1);
				try {
					// open webpage
					String address = "http://www.ssynth.co.uk/~gay/cgi-bin/nph-an?line=" + word
							+ "&words=" + numWords + "&dict=antworth&doai=on";
					URL webpage = new URL(address);

					BufferedReader readPage = new BufferedReader(new InputStreamReader(webpage
							.openStream()));
					String line = "";
					// move to line starting with <pre>
					while (!(line = readPage.readLine()).contains("<pre>"))
						;
					ArrayList<String> anagrams = new ArrayList<String>();
					// add all lines up to </pre>
					if (!line.contains("</pre>")) {
						line = line.replaceAll("<pre>", "");
						anagrams.add(line);
						boolean loop = true;
						while ((line = readPage.readLine()) != null && loop) {
							if (line.contains("</pre>"))
								break;
							anagrams.add(line);
						}
					}
					// show anagrams in JList
					JList list = new JList(anagrams.toArray());
					JScrollPane pane = new JScrollPane(list);
					pane.setPreferredSize(new Dimension(160, 200));
					list.setLayoutOrientation(JList.VERTICAL);
					list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					JOptionPane.showMessageDialog(window, pane, "Anagrams",
							JOptionPane.PLAIN_MESSAGE);

				} catch (IOException ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(window,
							"A problem occured, possibly no internet", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		anagram.setMnemonic(KeyEvent.VK_A);
		anagram.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		optionsMenu.add(anagram);

		menuBar.add(optionsMenu);

		JMenu networkingMenu = new JMenu("Networking");

		final JMenuItem connect = new JMenuItem();
		connect.setAction(new AbstractAction("Connect") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// disconect
				if (connected) {
					try {
						socket.close();
						out.close();
						in.close();
					} catch (IOException ex) {
						System.err.println("Couldn't get I/O for the connection to"
								+ socket.getInetAddress());
					}
					connect.setText("Connect");
					connected = false;
				} else { // connect
					try {
						socket = new Socket("linuxproj.ecs.soton.ac.uk", 1292);
						out = new PrintWriter(socket.getOutputStream(), true);
						in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						connected = true;
						input = new Thread(new InStream());
						input.start();
						connect.setText("Disconnect");
					} catch (UnknownHostException ex) {
						JOptionPane.showMessageDialog(window,
								"Host server (linuxproj)  inaccessible at \n"
										+ "the moment. Try setting new host", "Error",
								JOptionPane.ERROR_MESSAGE);
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(window, "IO for host (linuxproj) failed",
								"Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		networkingMenu.add(connect);

		JMenuItem changeHost = new JMenuItem();
		changeHost.setAction(new AbstractAction("Change Host") {

			@Override
			public void actionPerformed(ActionEvent e) {
				// disconect
				if (connected) {
					try {
						socket.close();
						out.close();
						in.close();
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(window, "IO for host failed", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
					connect.setText("Connect");
					connected = false;
				}
				try {// ask for host and attempt connection
					String host = JOptionPane.showInputDialog(window, "Enter Host Address:",
							"Change Host", JOptionPane.PLAIN_MESSAGE);
					socket = new Socket(host, 1292);
					out = new PrintWriter(socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					connected = true;
					input = new Thread(new InStream());
					input.start();
					connect.setText("Disconnect");
				} catch (UnknownHostException ex) {
					JOptionPane.showMessageDialog(window, "Host server inaccessible at \n"
							+ "the moment. Try setting new host", "Error",
							JOptionPane.ERROR_MESSAGE);
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(window, "IO for host failed", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		networkingMenu.add(changeHost);

		menuBar.add(networkingMenu);

		return menuBar;
	}

	/**
	 * Class to run and wait for input from the {@link Socket} connection
	 * 
	 * @author hja1g11
	 * 
	 */
	class InStream implements Runnable {

		public void run() {
			String line = "";
			try {
				while ((line = in.readLine()) != null) {
					String[] vals = line.split(":");
					if (!vals[0].equals("chat")) { // cell entered data
						int x = Integer.parseInt(vals[0]);
						int y = Integer.parseInt(vals[1]);
						char c = vals[2].charAt(0);
						String username = vals[3];
						grid.setCell(x, y, c, username, false, false); // set
																		// cell
					} else { // output chat text to log
						logArea.append(Tools.getTime() + "\n\t" + vals[1] + " says: " + vals[2]
								+ "\n");
					}
				}
				connected = false;
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Send data as {@link String} to {@link CrosswordServer}
	 * 
	 * @param x
	 *            - int of y coordinate of {@link Cell}
	 * @param y
	 *            - int of x coordinate of {@link Cell}
	 * @param c
	 *            - char stored in {@link Cell}
	 * @param username
	 *            - username of user who entered data
	 */
	protected void outStream(int x, int y, char c, String username) {
		String line = "";
		line += Integer.toString(x);
		line += ":";
		line += Integer.toString(y);
		line += ":";
		line += Character.toString(c);
		line += ":";
		line += username;
		out.println(line);
	}

	/**
	 * Check if connected to server
	 * 
	 * @return boolean whether or not there is a connection to server
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Get current username
	 * 
	 * @return username
	 */
	public String getUser() {
		return userName;
	}

	/**
	 * Check if {@link Clue} support is on
	 * 
	 * @return whether or not solved {@link Clue} support is on
	 */
	public boolean supportOn() {
		return solvedSupport;
	}

	/**
	 * Return current {@link Crossword}
	 * 
	 * @return current {@link Crossword}
	 */
	public Crossword getCurrentCrossword() {
		return currentCrossword;
	}

	/**
	 * Appends the {@link String} to logArea
	 * 
	 * @param string
	 *            - string to append to logArea
	 */
	public void appendLog(String string) {
		logArea.append(string);
	}

	/**
	 * return the acrossJList
	 * 
	 * @return acrossJList
	 */
	public JList getAcrossJList() {
		return acrossJList;
	}

	/**
	 * return the downJList
	 * 
	 * @return downJList
	 */
	public JList getDownJList() {
		return downJList;
	}

}