package vidivox;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import java.awt.Font;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

/*
 * Main menu frame, contains most of the GUI and the media player
 */
public class Player extends JFrame {
	
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

	private JPanel contentPane;

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
					Player frame = new Player();
					frame.setVisible(true);
					//play big buck bunny, will change later
			        frame.mediaPlayerComponent.getMediaPlayer().playMedia("sample_video_big_buck_bunny_1_minute.avi");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Player() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 865, 511);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnNewButton = new JButton("<<");
		btnNewButton.setBounds(33, 451, 54, 25);
		contentPane.add(btnNewButton);
		
		JButton btnNewButton_1 = new JButton("Play");
		btnNewButton_1.setBounds(99, 451, 117, 25);
		contentPane.add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton(">>");
		btnNewButton_2.setBounds(228, 451, 70, 25);
		contentPane.add(btnNewButton_2);
		
		JButton btnMute = new JButton("Mute");
		btnMute.setBounds(316, 451, 70, 25);
		contentPane.add(btnMute);
		
		JButton btnListen = new JButton("Listen");
		btnListen.setBounds(551, 192, 135, 40);
		contentPane.add(btnListen);
		
		JButton btnCreateMp = new JButton("Create mp3");
		btnCreateMp.setBounds(698, 192, 155, 40);
		contentPane.add(btnCreateMp);
		
		JTextPane textPane = new JTextPane();
		textPane.setBounds(556, 12, 297, 144);
		contentPane.add(textPane);
		
		JButton btnBrowseMp = new JButton("Browse mp3...");
		btnBrowseMp.setBounds(551, 267, 155, 40);
		contentPane.add(btnBrowseMp);
		
		JLabel lblNewLabel = new JLabel("Insert mp3 path");
		lblNewLabel.setBounds(556, 313, 297, 15);
		contentPane.add(lblNewLabel);
		
		JButton btnNewButton_3 = new JButton("Add Commentary\n");
		btnNewButton_3.setFont(new Font("Dialog", Font.BOLD, 22));
		btnNewButton_3.setBounds(551, 365, 302, 111);
		contentPane.add(btnNewButton_3);
		
		//panel for video player
		JPanel playerPanel = new JPanel(new BorderLayout());
		playerPanel.setBounds(33, 80, 506, 360);
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
		playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
		contentPane.add(playerPanel);
		
		//button for choosing a video to play
		JButton btnBrowseVideo = new JButton("Browse Video");
		btnBrowseVideo.setBounds(33, 43, 168, 25);
		contentPane.add(btnBrowseVideo);
	}
}
