package sockets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class EchoClient extends JFrame {
	String username;
	JTextField text;
	JButton button;
	JTextArea area;
	
	Socket socket = null;
	PrintWriter out = null;
	BufferedReader in = null;
	
	public EchoClient(String username) {
		
		try {
			socket = new Socket("localhost", 1292);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host.");
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for " + "the connection to: taranis.");
		}
		
		this.username = username;
		JPanel panel = new JPanel();
		text = new JTextField(20);
		panel.add(text);
		button = new JButton("send");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String line = text.getText();
				area.append(line + "\n");
				text.setText("");
				out.println(line);
			}
		});
		panel.add(button);
		area = new JTextArea(10, 30);
		panel.add(area);
		setContentPane(panel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(600, 550));
		pack();
		setVisible(true);
		
	}
	
//	public static void main(String[] args) throws IOException {
//		new EchoClient("henco");
//	}
}