package vidivox;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.*;
import javax.swing.Timer;

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
import java.awt.Color;
import java.awt.SystemColor;

import javax.swing.JSlider;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;

import java.awt.event.MouseMotionAdapter;

/*
 * This is the VIDIVOX project.
 * 
 * This class is for the main screen of VIDIVOX, it consists of visual components
 * such as labels and buttons, and also ActionListeners for the buttons.
 * 
 * This class consists of components for playing the video and controls for the video only
 * Authors: Aaron Zhong
 * upi: azho472
 * 
 *
 */
public class PlayerMedia extends JFrame{

	/*
	 * Instance fields useful throughout the player
	 */
	protected PlayerSideBar sidebar;
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	protected final EmbeddedMediaPlayer video;
	volatile private boolean mouseDown = false;
	private JPanel contentPane;
	protected static File videoFile;
	protected static PlayerMedia frame;
	protected JSlider vidSlider;
	protected JButton btnReverse;
	protected JButton btnPlay;
	protected JButton btnFastForward;
	protected JButton btnMute;
	protected JPanel playerPanel;
	protected final JLabel videoLabel;
	protected final JLabel timerLabel;
	protected JButton btnBrowseVideo;
	protected final JSlider volSlider;
	
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
					frame = new PlayerMedia();
					frame.setTitle("YEEZIVOX");
					frame.setVisible(true);					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	//Main menu frame, contains most of the GUI and the media player
	public PlayerMedia() {
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1079, 518);
		contentPane = new JPanel();
		contentPane.setForeground(Color.LIGHT_GRAY);
		contentPane.setBackground(Color.DARK_GRAY);
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// ************** Rewind button ******************
		btnReverse = new JButton("<<");
		btnReverse.setForeground(SystemColor.text);
		btnReverse.setBackground(Color.GRAY);
		btnReverse.setBounds(33, 470, 70, 25);
		//MouseListener for rewind button
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
		contentPane.add(btnReverse);
		
		//**************** Play and pause button **************************
		btnPlay = new JButton("Play/Pause");
		btnPlay.setForeground(SystemColor.text);
		btnPlay.setBackground(Color.GRAY);
		
		//ActionListener for the play/pause button
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
		
		//********************* fastforward button ************************
		btnFastForward = new JButton(">>");
		btnFastForward.setForeground(SystemColor.text);
		btnFastForward.setBackground(Color.GRAY);

		//MouseListener for fast forwarding
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
		
		//******************* Simple mute button ***************************
		btnMute = new JButton("Mute");
		btnMute.setForeground(SystemColor.text);
		btnMute.setBackground(Color.GRAY);
		btnMute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				video.mute();
			}
		});
		btnMute.setBounds(323, 470, 70, 25);
		contentPane.add(btnMute);
		
		//********************* panel for video player *******************************
		playerPanel = new JPanel(new BorderLayout());
		playerPanel.setBounds(33, 41, 699, 393);
		contentPane.add(playerPanel);
		
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		video = mediaPlayerComponent.getMediaPlayer();
		playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
		
		//***********video label, changes with user selection ************************
		videoLabel = new JLabel("No video chosen");
		videoLabel.setForeground(Color.WHITE);
		videoLabel.setBounds(228, 14, 506, 15);
		contentPane.add(videoLabel);
		
		//*******************button for choosing a video to play**********************
		btnBrowseVideo = new JButton("Browse Video");
		btnBrowseVideo.setForeground(SystemColor.text);
		btnBrowseVideo.setBackground(Color.GRAY);
		btnBrowseVideo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChooseVid();
			}
		});
		btnBrowseVideo.setBounds(33, 9, 168, 25);
		contentPane.add(btnBrowseVideo);
		
		//************************ label for timer **************************************
		timerLabel = new JLabel("0 sec");
		timerLabel.setForeground(Color.WHITE);
		timerLabel.setBounds(662, 475, 70, 15);
		contentPane.add(timerLabel);
		
		//************************** Volume slider **************************************
		volSlider = new JSlider();
		volSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				double p = 100 * (e.getX() / 200.00);
				volSlider.setValue((int)p);
				video.setVolume((int)p);
			}
		});
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
		
		//**Progress bar to show how far the video is in, also allow user to drag the bar to move positions in the video**
		vidSlider = new JSlider();
		vidSlider.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				double p = 100 * (e.getX() / 700.00);
				vidSlider.setValue((int)p);
				int time = vidSlider.getValue();
				long vidLen = video.getLength()/100;
				video.setTime(vidLen * time);
			}
		});
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
		
		//*******************Timer used to check video time********************************
		Timer t = new Timer(200, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UpdateTime();
			}
		}); 
		t.start();	 
		
		sidebar = new PlayerSideBar(contentPane, frame);
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

	/*
	 * Handles fastforwarding and rewinding
	 */
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
	
	/*
	 * This method provides functionality to select an avi file
	 */
	private void ChooseVid(){
		//Add file chooser as well as set a filter so that user only picks avi
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
			ProgressBarBG bar = new ProgressBarBG(frame);
			bar.execute();
			
			//If an mp3 and video file is selected, enable the merge button
			if (PlayerSideBar.edit.numAudio != 1){
				PlayerSideBar.btnAddCom.setEnabled(true);
			}
		}
	}
	
	/*
	 * This method updates the progress bar
	 */
	private void UpdateTime(){
		if(video.getMediaPlayerState().toString().equalsIgnoreCase("libvlc_Ended")){
			timerLabel.setText("End of Video");	//check for end of video
			//This doesn't actually display, timer refreshes to 0
		}else{
			timerLabel.setText((video.getTime()/1000)+ " sec");	//get video time
		}
	}
	
}
