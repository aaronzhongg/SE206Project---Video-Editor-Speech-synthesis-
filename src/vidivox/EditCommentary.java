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

public class EditCommentary extends JFrame {


	private JPanel contentPane;
	protected JTable audioTable;
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	protected final EmbeddedMediaPlayer audio;
	protected ArrayList<File> mp3File = new ArrayList<File>();
	private boolean isPlaying = false;
	protected int numAudio = 1;

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
					EditCommentary frame = new EditCommentary();
					frame.setVisible(true);
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
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		audio = mediaPlayerComponent.getMediaPlayer();
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 650, 241);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		final JButton btnAddAudio = new JButton("Add mp3");
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

					int i;
					for (i = 0; i < 7 ; i ++){
						if (audioTable.getValueAt(i,0) == null) { 
							audioTable.setValueAt(mp3File.get(mp3File.size() - 1).getName(), i , 0);

							break;
						}
					}

					if (audioTable.getValueAt(6, 0) != null) {
						btnAddAudio.setEnabled(false);
					}

				}


			}
		});
		btnAddAudio.setForeground(Color.WHITE);
		btnAddAudio.setBackground(Color.GRAY);
		btnAddAudio.setBounds(80, 158, 150, 50);
		contentPane.add(btnAddAudio);

		JButton btnRemoveMp = new JButton("Remove Selected");
		btnRemoveMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (audioTable.getSelectedRow() != -1) {
					if (audioTable.getValueAt(audioTable.getSelectedRow(), 0) != null){
						mp3File.remove(audioTable.getSelectedRow());
						numAudio--;
						for (int i = audioTable.getSelectedRow(); i < 7; i++){
							if (audioTable.getValueAt(i+1, 0) == null){	
								audioTable.setValueAt(null, i, 0);
								audioTable.setValueAt(null, i, 1);
								audioTable.setValueAt(null, i, 2);
								audioTable.setValueAt(null, i, 3);
								break;
							}
							for (int j = 0; j < 4; j ++) {
								audioTable.setValueAt(audioTable.getValueAt(i+1, j), i, j);
							}
						}

					}
				}
			}
		});
		btnRemoveMp.setForeground(Color.WHITE);
		btnRemoveMp.setBackground(Color.GRAY);
		btnRemoveMp.setBounds(400, 158, 155, 50);
		contentPane.add(btnRemoveMp);

		final JButton btnListen = new JButton("Listen Selected");
		btnListen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
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
		btnListen.setBounds(242, 158, 150, 50);
		contentPane.add(btnListen);

		contentPane.add(mediaPlayerComponent);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 12, 626, 134);
		contentPane.add(scrollPane);

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



		DefaultTableModel TM = new DefaultTableModel();
		audioTable = new JTable(TM){
			@Override
			public boolean isCellEditable(int row,int column) {
				if (audioTable.getValueAt(row, 0) != null) {
					switch(column){
					case 2: return true;
					default: return false;
					}
				} else {
					return false;
				}
			}
		};
		audioTable.getDefaultEditor(String.class).addCellEditorListener(
				new CellEditorListener() {
					public void editingCanceled(ChangeEvent e) {
					}

					public void editingStopped(ChangeEvent e) {
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
								audioTable.setValueAt("",audioTable.getSelectedRow(), audioTable.getSelectedColumn());
								JOptionPane.showMessageDialog(contentPane, "Enter a valid time in the form mm:ss");
							}
						}
					}
				});
		audioTable.setModel(new DefaultTableModel(
				new Object[][] {
						{null, null, null, null},
						{null, null, null, null},
						{null, null, null, null},
						{null, null, null, null},
						{null, null, null, null},
						{null, null, null, null},
						{null, null, null, null},
				},
				new String[] {
						"Mp3 Name", "Duration (mm:ss)", "Start Time (mm:ss)", "End Time (mm:ss)"
				}
				) {
			Class[] columnTypes = new Class[] {
					Object.class, Long.class, String.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			boolean[] columnEditables = new boolean[] {
					false, false, true, true
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		audioTable.getColumnModel().getColumn(0).setPreferredWidth(171);
		audioTable.getColumnModel().getColumn(1).setPreferredWidth(127);
		audioTable.getColumnModel().getColumn(2).setPreferredWidth(175);
		audioTable.getColumnModel().getColumn(3).setPreferredWidth(183);
		audioTable.getTableHeader().setReorderingAllowed(false);
		scrollPane.setViewportView(audioTable);


	}


}
