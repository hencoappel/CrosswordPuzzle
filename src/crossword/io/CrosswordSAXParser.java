package crossword.io;
import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import crossword.Clue;
import crossword.Crossword;

/**
 * I used a SAX parser for reading the XML file because they are more efficient
 * than other methods and scale well for big files. For the purpose of this
 * assignment this is was most likely not necessary and could have been done in
 * a much neater way using a DOM parser or another alternative, but I already
 * had the code from a previous project of mine so I decided to use it and adapt
 * it.
 */
class CrosswordSAXParser extends DefaultHandler {

	private Crossword crossword;

	private ArrayList<Clue> acrossClues, downClues;
	private File xmlFile;
	private String tempVal;
	private boolean across, down;

	// To store temp vals while parsing rest
	private Clue tempClue;
	private String tempClueStr, tempAnswerStr, tempNumStr, tempXStr, tempYStr, tempSolvedByStr,
			tempSolvedAtStr;
	private boolean tempSolved;
	private boolean readState;

	public CrosswordSAXParser(File xmlFile, boolean readState) {
		this.xmlFile = xmlFile;
		acrossClues = new ArrayList<Clue>();
		downClues = new ArrayList<Clue>();
		this.readState = readState;
		parseDocument();
	}

	private void parseDocument() {

		// get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			// parse the file
			sp.parse(xmlFile, this);
		} catch (Exception e) { // just set to null if exception thrown
			crossword = null;
			e.printStackTrace();
		}
	}

	public Crossword getCrossword() {
		return crossword;
	}

	// Event Handlers
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		// reset
		tempVal = "";
		if (qName.equalsIgnoreCase("crossword")) {
			String title = attributes.getValue("title");
			int size = Integer.parseInt(attributes.getValue("size"));
			crossword = new Crossword(title, size, acrossClues, downClues);
		} else if (qName.equalsIgnoreCase("across")) {
			across = true;
		} else if (qName.equalsIgnoreCase("down")) {
			down = true;
		} else if (qName.equalsIgnoreCase("clueEntry")) {
			tempNumStr = attributes.getValue("number");
			tempXStr = attributes.getValue("x");
			tempYStr = attributes.getValue("y");
			if (readState) {
				if (attributes.getValue("solved") != null)
					tempSolved = Boolean.parseBoolean(attributes.getValue("solved"));
			}
		}

	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		tempVal = new String(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (qName.equalsIgnoreCase("clue")) {
			tempClueStr = tempVal;
		} else if (qName.equalsIgnoreCase("across")) {
			across = false;
		} else if (qName.equalsIgnoreCase("down")) {
			down = false;
		} else if (qName.equalsIgnoreCase("answer")) {
			tempAnswerStr = tempVal;
		} else if (qName.equalsIgnoreCase("solvedBy")) {
			tempSolvedByStr = tempVal;
		} else if (qName.equalsIgnoreCase("solvedAt")) {
			tempSolvedAtStr = tempVal;
		} else if (qName.equalsIgnoreCase("clueEntry")) {

			int num = Integer.parseInt(tempNumStr);
			int x = Integer.parseInt(tempXStr);
			int y = Integer.parseInt(tempYStr);

			tempClue = new Clue(num, x, y, tempClueStr, tempAnswerStr);
			if (tempSolved) {
				DateFormat dt = DateFormat.getDateTimeInstance();
				try {
					tempClue.setSolved(tempSolvedByStr, dt.parse(tempSolvedAtStr));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			tempSolved = false;

			if (across)
				acrossClues.add(tempClue);
			else if (down)
				downClues.add(tempClue);

		}
	}
}