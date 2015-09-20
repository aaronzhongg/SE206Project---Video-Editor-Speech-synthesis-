package vidivox;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.event.*;
import javax.swing.Timer;

import java.awt.Font;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JTextArea;
import components.DocumentSizeFilter;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.UIManager;

/*
 * This is the VIDIVOX prototype for SE206 assignment 3
 * Authors: Kaimin Li, Aaron Zhong
 * upi: kli438, azho472
 */
public class Player extends JFrame {

	/*
	 * Instance fields useful throughout the player
	 */
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private final EmbeddedMediaPlayer video ;
	volatile private boolean mouseDown = false;
	private JPanel contentPane;
	private File videoFile;
	protected static File mp3File;
	private DefaultStyledDocument docfilt = new DefaultStyledDocument();
	private JLabel lblChars;
	protected static JLabel mp3Label;
	private JButton btnAddCom;
	protected final JTextArea txtArea;
	protected static Player frame;
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
					frame = new Player();
					frame.setVisible(true);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	//Main menu frame, contains most of the GUI and the media player

	public Player() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 865, 511);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		//Reverse button
		JButton btnReverse = new JButton("<<");
		btnReverse.setForeground(SystemColor.text);
		btnReverse.setBackground(Color.GRAY);
		btnReverse.addMouseListener(new MouseAdapter() {
			//hold down to reverse, release to stop reversing
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = true;
					initVidControlThread("R");	//calls method that make a thread for this to keep GUI responsive
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = false;	//the thread will check this to determine when to stop
				}
			}
		});
		btnReverse.setBounds(33, 451, 70, 25);
		contentPane.add(btnReverse);

		//Play and pause button
		JButton btnPlay = new JButton("Play/Pause");
		btnPlay.setForeground(SystemColor.text);
		btnPlay.setBackground(Color.GRAY);
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//check if video is playing and choose action accordingly
				if(video.isPlaying()){
					video.pause();
				}else{
					video.play();
				}
			}
		});
		btnPlay.setBounds(112, 451, 117, 25);
		contentPane.add(btnPlay);

		//fastforward button
		JButton btnFastForward = new JButton(">>");
		btnFastForward.setForeground(SystemColor.text);
		btnFastForward.setBackground(Color.GRAY);

		btnFastForward.addMouseListener(new MouseAdapter() {

			//If held down it will FF, stops FFing when released.
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = true;
					initVidControlThread("FF");	//calls method that make a thread for this to keep GUI responsive
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					mouseDown = false;	//the thread will check this to determine when to stop
				}
			}
		});


		btnFastForward.setBounds(241, 451, 70, 25);
		contentPane.add(btnFastForward);

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
		//simple text area for the user to enter text
		txtArea = new JTextArea();
		txtArea.setWrapStyleWord(true);
		txtArea.setRows(5);
		txtArea.setToolTipText("Enter text for text to speech. ");
		txtArea.setFont(new Font("Dialog", Font.PLAIN, 15));
		txtArea.setLineWrap(true);
		txtArea.setBounds(551, 41, 302, 122);
		txtArea.setDocument(docfilt);
		contentPane.add(txtArea);

		//Simple mute button
		JButton btnMute = new JButton("Mute");
		btnMute.setForeground(SystemColor.text);
		btnMute.setBackground(Color.GRAY);
		btnMute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				video.mute();
			}
		});
		btnMute.setBounds(323, 451, 70, 25);
		contentPane.add(btnMute);

		//Button for listening to text entered
		final JButton btnListen = new JButton("Listen");
		btnListen.setBackground(Color.GRAY);
		btnListen.setForeground(Color.WHITE);
		btnListen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				//disable listen button so speak only one thing at a time
				btnListen.setEnabled(false);


				SwingWorker worker = new SwingWorker<Void, Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						//run festival in bash from the entered text
						ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "echo " + txtArea.getText() + " | festival --tts");

						try {
							//begin process and wait for process to complete
							Process process = builder.start();
							process.waitFor();

						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}

					//after speech is done, re enable listen button
					@Override
					protected void done(){
						btnListen.setEnabled(true);
					}
				};

				//execute SwingWorker
				worker.execute();
			}
		});

		//Button to allow user to create an mp3 file from the text entered
		btnListen.setBounds(551, 192, 135, 40);
		contentPane.add(btnListen);

		JButton btnCreateMp = new JButton("Create mp3");
		btnCreateMp.setBackground(Color.GRAY);
		btnCreateMp.setForeground(Color.WHITE);
		btnCreateMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ask user to enter desired output name
				final String output = JOptionPane.showInputDialog("Enter Mp3 Name: ");
				File f = new File(output+".mp3");
				if (output != null && output.length() > 0){
				if(f.exists() && !f.isDirectory()) { 
					int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
					if (reply == JOptionPane.YES_OPTION){
						CreateMp3DoInBackground maker = new CreateMp3DoInBackground(output);

						maker.execute();
					}
				} else {
					CreateMp3DoInBackground maker = new CreateMp3DoInBackground(output);

					maker.execute();
				}
				}
			}
		});


		btnCreateMp.setBounds(698, 192, 155, 40);
		contentPane.add(btnCreateMp);

		//label for mp3 file
		mp3Label = new JLabel("No mp3 file chosen");
		mp3Label.setForeground(Color.WHITE);
		mp3Label.setBounds(556, 313, 297, 15);
		contentPane.add(mp3Label);

		//Browser for mp3 files
		JButton btnBrowseMp = new JButton("Browse mp3...");
		btnBrowseMp.setBackground(Color.GRAY);
		btnBrowseMp.setForeground(Color.WHITE);
		btnBrowseMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//create file chooser and filter (mp3)
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);
				FileFilter filter = new FileNameExtensionFilter("mp3 files", new String[] {"mp3","MP3"});
				fileChooser.setFileFilter(filter);
				int returnVal = fileChooser.showOpenDialog(new JFrame());

				if(returnVal == JFileChooser.APPROVE_OPTION){
					mp3File = fileChooser.getSelectedFile();
					mp3Label.setText(mp3File.getName());
					if (videoFile.length() != 0) {
						btnAddCom.setEnabled(true);
					}
				}
			}
		});
		btnBrowseMp.setBounds(551, 267, 155, 40);
		contentPane.add(btnBrowseMp);

		btnAddCom = new JButton("Add Commentary\n");

		btnAddCom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				final String comOutName = JOptionPane.showInputDialog("Enter New Video Name: ");

				SwingWorker adder = new SwingWorker<Void,Void>() {

					@Override
					protected Void doInBackground() throws Exception {
						ProcessBuilder splitter = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i " + videoFile.getName() + " -i " + mp3File.getName() + " -filter_complex amix=inputs=2:duration=first temp.mp3");
						ProcessBuilder combiner = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i temp.mp3 -i " + videoFile.getName() + " -map 0:a -map 1:v " + comOutName + ".avi");

						Process split = splitter.start();
						split.waitFor();
						Process combine = combiner.start();
						combine.waitFor();
						return null;
					}

					@Override
					protected void done(){
						//remove the mp3 file that was created
						try {
							File del = new File("temp.mp3");
							del.delete();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				};

				adder.execute();

			}

		});

		btnAddCom.setBackground(Color.GRAY);
		btnAddCom.setForeground(Color.WHITE);
		btnAddCom.setFont(new Font("Dialog", Font.BOLD, 22));
		btnAddCom.setBounds(551, 365, 302, 111);

		btnAddCom.setEnabled(false);
		contentPane.add(btnAddCom);

		//panel for video player
		JPanel playerPanel = new JPanel(new BorderLayout());
		playerPanel.setBounds(33, 41, 506, 399);
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		video = mediaPlayerComponent.getMediaPlayer();

		lblChars = new JLabel("200/200");
		lblChars.setForeground(Color.WHITE);
		lblChars.setBounds(795, 170, 70, 15);
		contentPane.add(lblChars);

		playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
		contentPane.add(playerPanel);

		//video label, changes with user selection
		final JLabel videoLabel = new JLabel("No video chosen");
		videoLabel.setForeground(Color.WHITE);
		videoLabel.setBounds(228, 14, 506, 15);
		contentPane.add(videoLabel);

		//button for choosing a video to play
		JButton btnBrowseVideo = new JButton("Browse Video");
		btnBrowseVideo.setForeground(SystemColor.text);
		btnBrowseVideo.setBackground(Color.GRAY);
		btnBrowseVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//Add file chooser as well as set a filter so that user only picks avi or mp4 files
				final JFileChooser fileChooser = new JFileChooser();
				fileChooser.setAcceptAllFileFilterUsed(false);
				FileFilter filter = new FileNameExtensionFilter("Video files (avi and mp4)", new String[] {"avi", "mp4","AVI","MP4"});
				fileChooser.setFileFilter(filter); 
				int returnVal = fileChooser.showOpenDialog(new JFrame());

				if(returnVal == JFileChooser.APPROVE_OPTION){
					//play the file chosen
					videoFile = fileChooser.getSelectedFile();
					video.playMedia(videoFile.getAbsolutePath());
					videoLabel.setText(videoFile.getName());
					if (mp3File.length() != 0){
						btnAddCom.setEnabled(true);
					}
				}
			}
		});
		btnBrowseVideo.setBounds(33, 9, 168, 25);
		contentPane.add(btnBrowseVideo);

		//label for timer

		final JLabel timerLabel = new JLabel("0 sec");
		timerLabel.setForeground(Color.WHITE);
		timerLabel.setBounds(404, 456, 70, 15);
		contentPane.add(timerLabel);



		//Timer used to check video time
		Timer t = new Timer(200, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timerLabel.setText((video.getTime()/1000)+ " sec");	//get video time
			}
		}); 
		t.start();

	}

	private void charCount() {
		lblChars.setText((200 - docfilt.getLength())+"/200");

	}



	/*
	 * check method to ensure concurrency when multiple events are fired
	 * This is just in case other events are fired while fastforwarding or reversing (highly unlikely)
	 */
	volatile private boolean isRunning = false;
	private synchronized boolean checkAndMark() {
		if (isRunning) return false;
		isRunning = true;
		return true;
	}

	private void initVidControlThread(final String arg){	                    
		if (checkAndMark()) {	//don't start another thread if this one is still running
			new Thread() {
				public void run() {
					do {
						//Check which button was pressed
						if(arg.equals("FF")){
							video.skip(10);
						}else{
							video.skip(-10);
						}
						try {
							sleep(1);	//Slight delay to prevent big jumps
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					} while (mouseDown); //do until released
					isRunning = false;	//no longer running
				}
			}.start();	
		}
	}
}
