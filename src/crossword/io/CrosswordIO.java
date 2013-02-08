package crossword.io;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import crossword.Clue;
import crossword.Crossword;

/**
 * All my I/O is done with XML format. My XML format for crosswords:
 * 
 * <crossword title="Guardian 13,019" size="13"> <across> <clueEntry number="1"
 * x="1" y="0" solved="true"> <solvedBy>gdfgdf</solvedBy> <solvedAt>11-May-2012
 * 14:10:44</solvedAt> <clue>Showy</clue> <answer>OSTENTATIOUS</answer>
 * </clueEntry> </across> <down> <clueEntry number="2" x="2" y="0"
 * solved="false"> <clue>One way or another</clue> <answer>SOMEHOW</answer>
 * </clueEntry> </down> </crossword>
 * 
 * You can see that if solved is false, there are no solvedBy or solvedAt
 * elements
 * 
 * @author hja1g11
 * 
 */
public class CrosswordIO {

	/**
	 * Reads in a Crossword from file passed with solved state of clues
	 * 
	 * @param file
	 *            - input file
	 * @return Crossword created from file data. null if failed for any reason
	 */
	public static Crossword readPuzzle(File file) {
		return inputPuzzle(file, true);
	}

	/**
	 * Reads in a Crossword from file passed without solved state of clues
	 * 
	 * @param file
	 *            - input file
	 * @return Crossword created from file data. null if failed for any reason
	 */
	public static Crossword importPuzzle(File file) {
		return inputPuzzle(file, false);
	}

	/**
	 * Reads in a Crossword from file passed.
	 * 
	 * @param file
	 *            - input file
	 * @param readState
	 *            - whether to read in solved state of clues
	 * @return Crossword created from file data. null if failed for any reason
	 */
	private static Crossword inputPuzzle(File file, boolean readState) {
		CrosswordSAXParser parser = new CrosswordSAXParser(file, readState);
		return parser.getCrossword();
	}

	/**
	 * Save puzzle to file with solved state of clues
	 * 
	 * @param file
	 *            - output file
	 * @param crossword
	 *            - crossword to output
	 */
	public static void writePuzzle(File file, Crossword crossword) {
		ouputPuzzle(file, crossword, true);
	}

	/**
	 * Save puzzle to file without solved state of clues
	 * 
	 * @param file
	 *            - output file
	 * @param crossword
	 *            - crossword to output
	 */
	public static void exportPuzzle(File file, Crossword crossword) {
		ouputPuzzle(file, crossword, false);
	}

	/**
	 * Save puzzle to file
	 * 
	 * @param file
	 *            - output file
	 * @param crossword
	 *            - crossword to output
	 * @param storeState
	 *            - whether to store the solved state of clues
	 */
	private static void ouputPuzzle(File file, Crossword crossword, boolean storeState) {
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		XMLStreamWriter writer = null;

		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(
					new OutputStreamWriter(outputStream, "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}

		try {
			writer.writeStartDocument();
			writer.writeStartElement("crossword");

			writer.writeAttribute("title", crossword.getTitle());
			writer.writeAttribute("size", Integer.toString(crossword.getSize()));

			writer.writeStartElement("across");
			for (Clue clue : crossword.getAcrossClues()) {
				writeClue(writer, clue, storeState);
			}
			writer.writeEndElement();

			writer.writeStartElement("down");
			for (Clue clue : crossword.getDownClues()) {
				writeClue(writer, clue, storeState);
			}
			writer.writeEndElement();

			writer.writeEndElement();
			writer.writeEndDocument();

			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write each clue
	 * 
	 * @param writer
	 *            - XMLStreamWriter to write to
	 * @param file
	 *            - output file
	 * @param clue
	 *            - clue to output
	 * @param storeState
	 *            - whether to store the solved state of clues
	 * @throws XMLStreamException
	 *             - if something goes awry
	 */
	private static void writeClue(XMLStreamWriter writer, Clue clue, boolean storeState)
			throws XMLStreamException {
		writer.writeStartElement("clueEntry");

		writer.writeAttribute("number", Integer.toString(clue.getNumber()));
		writer.writeAttribute("x", Integer.toString(clue.getX()));
		writer.writeAttribute("y", Integer.toString(clue.getY()));

		if (storeState) {
			writer.writeAttribute("solved", Boolean.toString(clue.isSolved()));
			if (clue.isSolved()) {
				writer.writeStartElement("solvedBy");
				writer.writeCharacters(clue.getSolvedBy());
				writer.writeEndElement();

				writer.writeStartElement("solvedAt");

				DateFormat dt = DateFormat.getDateTimeInstance();
				writer.writeCharacters(dt.format(clue.getSolvedAt()));
				writer.writeEndElement();
			}
		}

		writer.writeStartElement("clue");
		writer.writeCharacters(clue.getClue());
		writer.writeEndElement();

		writer.writeStartElement("answer");
		writer.writeCharacters(clue.getAnswer());
		writer.writeEndElement();

		writer.writeEndElement();
	}

	/**
	 * Get a file with an extension the matches those passed in.
	 * 
	 * @param component
	 *            - parent component of the JFileChoose
	 * @param extensionsAllowed
	 *            - extensions allowed for files
	 * @param read
	 *            - whether reading or writing to show correct JFileChooser
	 * @return File
	 */
	public static File getFile(Component component, final String[] extensionsAllowed, boolean read) {
		final JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {

			// Accept all directories and all allowed extensions files.
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}

				// get extension
				String fileName = f.getName();
				int i = fileName.lastIndexOf('.');
				String extension = "";
				if (i > 0 && i < fileName.length() - 1) {
					extension = fileName.substring(i + 1).toLowerCase();
				}

				return checkExtension(extension, extensionsAllowed);
			}

			// The description of this filter
			public String getDescription() {
				return Arrays.toString(extensionsAllowed);
			}
		});
		fc.setAcceptAllFileFilterUsed(false);
		int returnVal;
		if (read)
			returnVal = fc.showOpenDialog(component);
		else
			returnVal = fc.showSaveDialog(component);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String fileName = file.getName();
			int i = fileName.lastIndexOf('.');
			String extension = "";
			if (i > 0 && i < fileName.length() - 1) {
				extension = fileName.substring(i + 1).toLowerCase();
			}
			// only accept these extensions
			if (checkExtension(extension, extensionsAllowed)) {
				return file;
			} else {
				Object[] options = { "Try Again", "Cancel" };
				int n = JOptionPane.showOptionDialog(component, "Invalid extension", "Error",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
						options[1]);
				if (n == 0) // Try again
					return getFile(component, extensionsAllowed, read);
			}
		}
		return null;
	}

	private static boolean checkExtension(String extension, String[] extensionAllowed) {
		if (extension != null) {
			boolean allowed = false;
			for (int i = 0; i < extensionAllowed.length; i++) {
				allowed = allowed || extension.equals(extensionAllowed[i]);
				if (allowed)
					return true;
			}
		}
		return false;
	}
}