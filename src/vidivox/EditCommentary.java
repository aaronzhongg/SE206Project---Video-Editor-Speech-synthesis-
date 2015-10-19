package vidivox;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;

import java.awt.Color;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.Timer;

import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.InputMethodListener;
import java.awt.event.InputMethodEvent;
import javax.swing.SwingConstants;
import java.awt.Font;
/*
 * EditCommentary class provides functionality to add and remove multiple audio files for merging
 * Also allows user to listen to selected audio files
 */
public class EditCommentary extends JFrame {

	static EditCommentary aframe;
	private JPanel contentPane;
	protected JTable audioTable;
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	protected final EmbeddedMediaPlayer audio;
	protected ArrayList<File> mp3File = new ArrayList<File>();
	private boolean isPlaying = false;
	protected int numAudio = 1; //used to keep track of how many audio files added
	protected final JButton btnAddAudio;
	protected JButton btnRemoveMp;
	protected final JButton btnListen;
	protected JButton btnClose;
	protected JLabel lblTitle;
	protected JLabel lblNote;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		//add vlc search path
		NativeLibrary.addSearchPath(
				RuntimeUtil.getLibVlcLibraryName(), "/Applications/vlc-2.0.0/VLC.app/Contents/MacOS/lib"
				);
		Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					aframe = new EditCommentary();
					aframe.setTitle("YEEDITOR");
					aframe.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public EditCommentary() {
		
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//MediaPlayerComponent to allow user to listen to added mp3 files
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		audio = mediaPlayerComponent.getMediaPlayer();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 650, 300);
		//for vlcj to play mp3
		contentPane.add(mediaPlayerComponent);


		//Allow user to add mp3 files to the commentary list
		btnAddAudio = new JButton("Add mp3");
		btnAddAudio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//create file chooser and filter (mp3)
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);
				FileFilter filter = new FileNameExtensionFilter("mp3 files", new String[] {"mp3","MP3"});
				fileChooser.setFileFilter(filter);
				int returnVal = fileChooser.showOpenDialog(new JFrame());

				if(returnVal == JFileChooser.APPROVE_OPTION){

					numAudio++;
					mp3File.add(fileChooser.getSelectedFile());

					//After a audio file is selected add its name to the table and set a default start time as 0s
					int i;
					for (i = 0; i < 7 ; i ++){
						if (audioTable.getValueAt(i,0) == null) { 
							audioTable.setValueAt(mp3File.get(mp3File.size() - 1).getName(), i , 0);
							audioTable.setValueAt("00:00", i, 1);
							break;
						}
					}

					//if the list is full (max 7 audio files) then disable the add audio button to prevent user from adding more
					if (audioTable.getValueAt(6, 0) != null) {
						btnAddAudio.setEnabled(false);
					}

					//if a video file is selected in main menu, enable the add commentary button
					if (PlayerMedia.videoFile != null) {
						PlayerSideBar.btnAddCom.setEnabled(true);
					}
				}
			}
		});
		btnAddAudio.setForeground(Color.WHITE);
		btnAddAudio.setBackground(Color.GRAY);
		btnAddAudio.setBounds(30, 200, 150, 50);
		contentPane.add(btnAddAudio);
		//******************************************************************************
		
		
		//A button to allow users to remove added audio files
		btnRemoveMp = new JButton("Remove Selected");
		btnRemoveMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (audioTable.getSelectedRow() != -1) { //make sure user has actually selected a row
					if (audioTable.getValueAt(audioTable.getSelectedRow(), 0) != null){ 
						mp3File.remove(audioTable.getSelectedRow());
						numAudio--;
						//remove the selected row and shift others up
						for (int i = audioTable.getSelectedRow(); i < 7; i++){ 
							if (audioTable.getValueAt(i+1, 0) == null){	
								audioTable.setValueAt(null, i, 0);
								audioTable.setValueAt(null, i, 1);
								break;
							}
							for (int j = 0; j < 2; j ++) {
								audioTable.setValueAt(audioTable.getValueAt(i+1, j), i, j);
							}
						}

					}
					
					//if everything is removed, disable add commentary button
					if (audioTable.getValueAt(0, 0) == null) {
						PlayerSideBar.btnAddCom.setEnabled(false); 
					}
				} 
			}
		});
		btnRemoveMp.setForeground(Color.WHITE);
		btnRemoveMp.setBackground(Color.GRAY);
		btnRemoveMp.setBounds(354, 200, 155, 50);
		contentPane.add(btnRemoveMp);
		//*********************************************************************************
		
		//Button to allow user to listen to selected mp3 files
		btnListen = new JButton("Listen Selected"); 
		btnListen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//Find out which mp3 file is selected
				if(audioTable.getSelectedRow() != -1){
					if(isPlaying == false && audioTable.getValueAt(audioTable.getSelectedRow(), 0) != null){
						isPlaying = true;
						audio.playMedia(mp3File.get(audioTable.getSelectedRow()).getAbsolutePath());
						btnListen.setText("Stop Listening");	//change button name
					}	
					else{
						//Stop playing the audio file 
						isPlaying = false;
						btnListen.setText("Listen Selected");
						audio.stop();
					}
				}
			}
		});
		btnListen.setForeground(Color.WHITE);
		btnListen.setBackground(Color.GRAY);
		btnListen.setBounds(192, 200, 150, 50);
		contentPane.add(btnListen);
		//********************************************************************************

		//close window but keep everything edited in the window
		btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
		btnClose.setBackground(Color.LIGHT_GRAY);
		btnClose.setBounds(527, 200, 85, 50);
		contentPane.add(btnClose);
		//*********************************************************************************


		//Timer used to check whether a playing audio file has completed 
		Timer t = new Timer(200, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!audio.isPlaying()){
					btnListen.setText("Listen Selected");
					isPlaying = false;

				}
			}
		}); 
		t.start();

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 50, 626, 134);
		contentPane.add(scrollPane);
		
		//To allow the "Start at" cell to be editable once a audio file is added
		DefaultTableModel TM = new DefaultTableModel();
		audioTable = new JTable(TM){
			@Override
			public boolean isCellEditable(int row,int column) {
				if (audioTable.getValueAt(row, 0) != null) {
					switch(column){
					case 1: return true;
					default: return false;
					}
				} else {
					return false;
				}
			}
		};
		//check if cell has been edited and validity of input
		audioTable.getDefaultEditor(String.class).addCellEditorListener(
				new CellEditorListener() {
					public void editingCanceled(ChangeEvent e) {
					}

					public void editingStopped(ChangeEvent e) {
						//if the input is not valid (mm:ss), automatically fix for common inputs, otherwise show message
						CharSequence check = (CharSequence) audioTable.getValueAt(audioTable.getSelectedRow(), audioTable.getSelectedColumn());
						if(Pattern.matches("[0-9][0-9]:[0-5][0-9]", check) == false && check.length() > 0){
							if (Pattern.matches("[0-9]:[0-5][0-9]", check) == true ) {
								audioTable.setValueAt("0" + check, audioTable.getSelectedRow(), audioTable.getSelectedColumn());
							} else if (Pattern.matches("[0-9]:[0-9]", check) == true) {	
								audioTable.setValueAt("0" + check.charAt(0) + ":" + "0" + check.charAt(2), audioTable.getSelectedRow(), audioTable.getSelectedColumn());
							} else if (Pattern.matches("[0-9][0-9]:[0-9]", check) == true) {	
								audioTable.setValueAt(check.charAt(0) + check.charAt(1) + ":0" + check.charAt(2) , audioTable.getSelectedRow(), audioTable.getSelectedColumn());
							} else if (Pattern.matches("[0-9]", check) == true) {
								audioTable.setValueAt("00:0" + check, audioTable.getSelectedRow(), audioTable.getSelectedColumn());
							} else if (Pattern.matches("[0-5][0-9]", check) == true) {
								audioTable.setValueAt("00:" + check, audioTable.getSelectedRow(), audioTable.getSelectedColumn());
							} else {
								audioTable.setValueAt("00:00",audioTable.getSelectedRow(), audioTable.getSelectedColumn());
								JOptionPane.showMessageDialog(contentPane, "Enter a valid time in the form mm:ss", "Invalid Input", 0);
							}
						}
					}
				});
		audioTable.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
				{null, null},
			},
			new String[] {
				"Mp3 Name", "Start in video at (mm:ss) - Editable"
			}
		) {
			private static final long serialVersionUID = 1L;
			Class[] columnTypes = new Class[] {
				Object.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
				false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		audioTable.getColumnModel().getColumn(0).setPreferredWidth(171);
		audioTable.getColumnModel().getColumn(1).setPreferredWidth(197);
		audioTable.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(audioTable);
		//***************************************************************************
		
		//title
		lblTitle = new JLabel("Commentary Editor");
		lblTitle.setFont(new Font("Dialog", Font.BOLD, 22));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setForeground(Color.WHITE);
		lblTitle.setBounds(30, 0, 582, 30);
		contentPane.add(lblTitle);
		
		//Note
		lblNote = new JLabel("NOTE: Audio files with start times greater than length of video file will not merge");
		lblNote.setForeground(Color.WHITE);
		lblNote.setBounds(12, 30, 626, 15);
		contentPane.add(lblNote);


	}
}
