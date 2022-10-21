/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc2;

import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.swing.JFrame;

import jvn.JvnException;
import jvn.JvnProxy;
import jvn.JvnServerImpl;

public class Irc {
	public TextArea text;
	public TextField data;
	Frame frame;
	ISentence sentence;

	/**
	 * main method create a JVN object nammed IRC for representing the Chat
	 * application
	 **/
	public static void main(String argv[]) throws IOException{
		ISentence s = null;

		// Create or get if it exists a shared object named IRC
		try {
			s = (ISentence) JvnProxy.newInstance(new Sentence(), "IRC");
		} catch (JvnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// create the graphical part of the Chat application
		new Irc(s);
	}

	/**
	 * IRC Constructor
	 * 
	 * @param jo the JVN object representing the Chat
	 **/
	public Irc(ISentence s) {
		sentence = s;
		frame = new JFrame();

		//add function when closing window
		((JFrame) frame).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				try {
					JvnServerImpl s = JvnServerImpl.jvnGetServer();
					s.jvnTerminate();
				} catch (JvnException e1) {
					System.out.println("Coordinator not connected!");
				}
			}
		});

		frame.setLayout(new GridLayout(1, 1));
		text = new TextArea(10, 60);
		text.setEditable(false);
		text.setForeground(Color.red);
		frame.add(text);
		data = new TextField(40);
		frame.add(data);
		Button read_button = new Button("read");
		read_button.addActionListener(new readListener(this));
		frame.add(read_button);
		Button write_button = new Button("write");
		write_button.addActionListener(new writeListener(this));
		frame.add(write_button);
		frame.setSize(545, 201);
		text.setBackground(Color.black);
		frame.setVisible(true);
	}
}

/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class readListener implements ActionListener {
	Irc irc;

	public readListener(Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed(ActionEvent e) {
		// invoke the method
		String s = irc.sentence.read();
		// display the read value
		irc.data.setText(s);
		irc.text.append(s + "\n");
	}
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class writeListener implements ActionListener {
	Irc irc;

	public writeListener(Irc i) {
		irc = i;
	}

	/**
	 * Management of user events
	 **/
	public void actionPerformed(ActionEvent e) {
		// get the value to be written from the buffer
		String s = irc.data.getText();

		// invoke the method
		irc.sentence.write(s);
	}
}
