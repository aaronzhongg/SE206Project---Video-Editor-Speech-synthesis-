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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.*;
import javax.swing.Timer;

import java.awt.Font;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;

import components.DocumentSizeFilter;

import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.UIManager;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.event.MouseMotionAdapter;

/*
 * This is the VIDIVOX prototype for SE206 assignment 3
 * Authors: Kaimin Li, Aaron Zhong
 * upi: kli438, azho472
 */
public class Player extends JFrame{

	/*
	 * Instance fields useful throughout the player
	 */
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	protected final EmbeddedMediaPlayer video ;
	volatile private boolean mouseDown = false;
	private JPanel contentPane;
	protected static File videoFile;
	private DefaultStyledDocument docfilt = new DefaultStyledDocument();
	private JLabel lblChars;
	protected static JButton btnAddCom;
	protected JButton btnListen;
	protected final JTextArea txtArea;
	protected JButton btnCreateMp;
	protected static Player frame;
	protected JSlider vidSlider;
	protected EditCommentary edit = new EditCommentary();
	
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
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1079, 518);
		contentPane = new JPanel();
		contentPane.setForeground(Color.LIGHT_GRAY);
		contentPane.setBackground(Color.DARK_GRAY);
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
		btnReverse.setBounds(33, 470, 70, 25);
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
		btnPlay.setBounds(112, 470, 117, 25);
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


		btnFastForward.setBounds(241, 470, 70, 25);
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

		//Simple mute button
		JButton btnMute = new JButton("Mute");
		btnMute.setForeground(SystemColor.text);
		btnMute.setBackground(Color.GRAY);
		btnMute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				video.mute();
			}
		});
		btnMute.setBounds(323, 470, 70, 25);
		contentPane.add(btnMute);

		//Button for listening to text entered
		btnListen = new JButton("Listen");
		btnListen.setEnabled(false);
		btnListen.setBackground(Color.GRAY);
		btnListen.setForeground(Color.WHITE);
		btnListen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				//disable listen button so speak only one thing at a time
				btnListen.setEnabled(false);

				ListenDoInBackground ListenWorker = new ListenDoInBackground(frame);

				//execute SwingWorker
				ListenWorker.execute();
			}
		});

		//Button to allow user to create an mp3 file from the text entered
		btnListen.setBounds(750, 192, 135, 40);
		contentPane.add(btnListen);

		btnCreateMp = new JButton("Create mp3");
		btnCreateMp.setEnabled(false);
		btnCreateMp.setBackground(Color.GRAY);
		btnCreateMp.setForeground(Color.WHITE);
		btnCreateMp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//ask user to enter desired output name
				final String output = JOptionPane.showInputDialog("Enter Mp3 Name: ");
				File f = new File(output+".mp3");
				if (output != null && output.length() > 0){
					if(f.exists() && !f.isDirectory()) { 
						//ask if user would want to overwrite existing file
						int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
						if (reply == JOptionPane.YES_OPTION){
							CreateMp3DoInBackground maker = new CreateMp3DoInBackground(frame, output);

							maker.execute();
						}
					} else {
						CreateMp3DoInBackground maker = new CreateMp3DoInBackground(frame, output);

						maker.execute();
					}
				}
			}
		});


		btnCreateMp.setBounds(905, 192, 142, 40);
		contentPane.add(btnCreateMp);

		//Button to combined selected audio and video files
		btnAddCom = new JButton("Add Commentary\n");

		btnAddCom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				//pick a name for the output file
				final String comOutName = JOptionPane.showInputDialog("Enter New Video Name: ");
				File f = new File(comOutName+".avi");

				if(f.exists() && !f.isDirectory()) { 
					//ask if user would want to overwrite existing file
					int reply = JOptionPane.showConfirmDialog(null, "File already exists, overwrite?", "Overwrite?", JOptionPane.YES_NO_OPTION);
					if (reply == JOptionPane.YES_OPTION){
						//generate swingworker instance
						AddComDoInBackground adder = new AddComDoInBackground(frame, comOutName);

						adder.execute();
					}
				} else {
					//generate swingworker instance
					AddComDoInBackground adder = new AddComDoInBackground(frame, comOutName);

					adder.execute();
				}


			}
		});

		btnAddCom.setBackground(Color.GRAY);
		btnAddCom.setForeground(Color.WHITE);
		btnAddCom.setFont(new Font("Dialog", Font.BOLD, 22));
		btnAddCom.setBounds(750, 334, 297, 100);

		btnAddCom.setEnabled(false);
		contentPane.add(btnAddCom);

		//panel for video player
		JPanel playerPanel = new JPanel(new BorderLayout());
		playerPanel.setBounds(33, 41, 699, 393);
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		video = mediaPlayerComponent.getMediaPlayer();

		lblChars = new JLabel("200/200");
		lblChars.setForeground(Color.WHITE);
		lblChars.setBounds(980, 170, 70, 15);
		contentPane.add(lblChars);
		//simple text area for the user to enter text
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

		//Allow text area to scroll
		JScrollPane scrollPane = new JScrollPane(txtArea);
		scrollPane.setBounds(750, 41, 297, 122);
		contentPane.add(scrollPane);

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
				FileFilter filter = new FileNameExtensionFilter("Video files (avi)", new String[] {"avi", "AVI"});
				fileChooser.setFileFilter(filter); 
				int returnVal = fileChooser.showOpenDialog(new JFrame());

				if(returnVal == JFileChooser.APPROVE_OPTION){
					//play the file chosen
					videoFile = fileChooser.getSelectedFile();
					video.playMedia(videoFile.getAbsolutePath());

					videoLabel.setText(videoFile.getName());
					
					ProgressBarDoInBackground bar = new ProgressBarDoInBackground(frame);
					bar.execute();

				}


			}
		});
		btnBrowseVideo.setBounds(33, 9, 168, 25);
		contentPane.add(btnBrowseVideo);

		//label for timer

		final JLabel timerLabel = new JLabel("0 sec");
		timerLabel.setForeground(Color.WHITE);
		timerLabel.setBounds(662, 475, 70, 15);
		contentPane.add(timerLabel);

		//Volume slider
		final JSlider volSlider = new JSlider();
		volSlider.setValue(100);
		volSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				int vol = volSlider.getValue();	//get slider value
				video.setVolume(vol);	//set the video volume
			}
		});
		volSlider.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Volume", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(255, 255, 255)));
		volSlider.setForeground(Color.BLACK);
		volSlider.setBackground(Color.DARK_GRAY);
		volSlider.setBounds(444, 470, 200, 25);
		contentPane.add(volSlider);

		//Progress bar to show how far the video is in, also allow user to drag the bar to move positions in the video
		vidSlider = new JSlider();
		vidSlider.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				int time = vidSlider.getValue();
				long vidLen = video.getLength()/100;
				video.setTime(vidLen * time);
			}
		});
		vidSlider.setMinorTickSpacing(5);
		vidSlider.setMajorTickSpacing(10);
		vidSlider.setPaintTicks(true);
		
		vidSlider.setForeground(Color.GRAY);
		vidSlider.setBackground(Color.DARK_GRAY);
		vidSlider.setBounds(33, 442, 699, 16);
		vidSlider.setValue(0);
		contentPane.add(vidSlider);
		
		JButton btnNewButton = new JButton("Edit Commentary");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				edit.setVisible(true);
			}
		});
		btnNewButton.setForeground(Color.WHITE);
		btnNewButton.setFont(new Font("Dialog", Font.BOLD, 22));
		btnNewButton.setBackground(Color.GRAY);
		btnNewButton.setBounds(750, 244, 297, 78);
		contentPane.add(btnNewButton);



		//Timer used to check video time
		
		Timer t = new Timer(200, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(video.getMediaPlayerState().toString().equalsIgnoreCase("libvlc_Ended")){
					timerLabel.setText("End of Video");	//check for end of video
					//This doesn't actually display, timer refreshes to 0
				}else{
					timerLabel.setText((video.getTime()/1000)+ " sec");	//get video time
				}
			}
		}); 
		t.start();
		 
	}

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
							video.skip(20);
						}else{
							video.skip(-20);
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
