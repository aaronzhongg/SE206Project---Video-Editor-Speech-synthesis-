package vidivox;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultStyledDocument;

import components.DocumentSizeFilter;

/*
 * PlayerSideBar consists of components of the main screens side bar, mainly the editing functions
 */
public class PlayerSideBar {
	private JPanel contentPane;
	protected JScrollPane scrollPane;
	private JLabel lblChars;
	protected static JButton btnAddCom;
	protected static JButton btnListen;
	protected static JTextArea txtArea;
	protected JButton btnCreateMp;
	protected JButton btnEdit;
	protected static EditCommentary edit = new EditCommentary();
	protected PlayerMedia frame;
	private DefaultStyledDocument docfilt = new DefaultStyledDocument();

	public PlayerSideBar(JPanel panel, final PlayerMedia frame){
		this.contentPane = panel;
		this.frame = frame;
		//******************** Button for listening to text entered **********
		btnListen = new JButton("Listen");
		btnListen.setEnabled(false);
		btnListen.setBackground(Color.GRAY);
		btnListen.setForeground(Color.WHITE);
		// ActionListener to perform the speaking when pressed
		btnListen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				//disable listen button so speak only one thing at a time
				btnListen.setEnabled(false);

				ListenBG ListenWorker = new ListenBG(frame);

				//execute SwingWorker
				ListenWorker.execute();
			}
		});
		btnListen.setBounds(750, 192, 135, 40);
		contentPane.add(btnListen);
		//******************************************************************

		//********************** Create mp3 (TTS) *****************************
		btnCreateMp = new JButton("Create mp3");
		btnCreateMp.setEnabled(false);
		btnCreateMp.setBackground(Color.GRAY);
		btnCreateMp.setForeground(Color.WHITE);
		//ActionListener: Creates mp3 from text entered TTS
		btnCreateMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ask user to enter desired output name
				final String output = JOptionPane.showInputDialog(null,"Enter Mp3 Name: ");
				File f = new File(output+".mp3");

				if (output != null && output.length() > 0){
					if(f.exists() && !f.isDirectory()) { 
						//ask if user would want to overwrite existing file
						int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
						if (reply == JOptionPane.YES_OPTION){
							CreateMp3BG maker = new CreateMp3BG(frame, output);
							maker.execute();
						}
					} else {
						CreateMp3BG maker = new CreateMp3BG(frame, output);
						maker.execute();
					}
				}
			}
		});
		btnCreateMp.setBounds(905, 192, 142, 40);
		contentPane.add(btnCreateMp);
		//******************************************************************


		//*************Button to combined selected audio and video files*************
		btnAddCom = new JButton("Merge Video/Audio\n");
		btnAddCom.setBackground(Color.GRAY);
		btnAddCom.setForeground(Color.WHITE);
		btnAddCom.setFont(new Font("Dialog", Font.BOLD, 22));
		btnAddCom.setBounds(750, 334, 297, 100);
		btnAddCom.setEnabled(false);
		btnAddCom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pick a name for the output file
				final String comOutName = JOptionPane.showInputDialog("Enter New Video Name: ");
				File f = new File(comOutName+".avi");

				if (comOutName != null && comOutName.length() > 0){
					if(f.exists() && !f.isDirectory()) { 
						//ask if user would want to overwrite existing file
						int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);

						if (reply == JOptionPane.YES_OPTION){
							//generate swingworker instance
							AddCommentaryBG adder = new AddCommentaryBG(frame, comOutName);
							adder.execute();
						}

					} else {
						//generate swingworker instance
						AddCommentaryBG adder = new AddCommentaryBG(frame, comOutName);
						adder.execute();
					}
				}


			}
		});
		contentPane.add(btnAddCom);

		//Label to indicate how many characters are remaining
		lblChars = new JLabel("200/200");
		lblChars.setForeground(Color.WHITE);
		lblChars.setBounds(980, 170, 70, 15);
		contentPane.add(lblChars);
		//***************************************************************************

		//*****************simple text area for the user to enter text ******************
		txtArea = new JTextArea();
		txtArea.setText("Add Text Here");
		txtArea.setWrapStyleWord(true);
		txtArea.setRows(5);
		txtArea.setToolTipText("Enter text for text to speech. ");
		txtArea.setFont(new Font("Dialog", Font.PLAIN, 15));
		txtArea.setLineWrap(true);
		txtArea.setBounds(551, 41, 302, 122);
		txtArea.setDocument(docfilt);
		contentPane.add(txtArea);

		//****** Scroll pane which allow text area to scroll **************************
		scrollPane = new JScrollPane(txtArea);
		scrollPane.setBounds(750, 41, 297, 122);
		contentPane.add(scrollPane);

		JLabel textLabel = new JLabel("Enter text for text-to-speech");
		scrollPane.setColumnHeaderView(textLabel);


		//**************** Document Filter to count characters ************************
		//set the maximum character to 200 so the festival voice doesn't die
		docfilt.setDocumentFilter(new DocumentSizeFilter(200));
		//add a listener to show user how many characters remaining
		docfilt.addDocumentListener(new DocumentListener(){
			@Override
			public void changedUpdate(DocumentEvent e) {
				charCount();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				charCount();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				charCount();
			}

		});
		//******************************************************************

		//*************** Button to open window for eiditing commentary *****************
		btnEdit = new JButton("Add Audio Files To Merge");
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				edit.setVisible(true);
			}
		});
		btnEdit.setForeground(Color.WHITE);
		btnEdit.setFont(new Font("Dialog", Font.BOLD, 18));
		btnEdit.setBackground(Color.GRAY);
		btnEdit.setBounds(750, 244, 297, 78);
		contentPane.add(btnEdit);
		//*********************************************************************************

	}

	/*
	 * Update the character count
	 */
	private void charCount() {
		//Set a label to indicate how many characters remaining
		lblChars.setText((200 - docfilt.getLength())+"/200");
		//disable/reenable create mp3 button when text area is empty/non-empty
		if (docfilt.getLength() == 0){
			btnCreateMp.setEnabled(false);
			btnListen.setEnabled(false);
		} else {
			btnCreateMp.setEnabled(true);
			btnListen.setEnabled(true);
		}

	}




}
