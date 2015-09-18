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

public class Player extends JFrame {

	private JPanel contentPane;
	
	//Testing if git werks from my side (alex)

	//aladmladmladmlallll alloha aku
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Player frame = new Player();
					frame.setVisible(false);
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
		btnNewButton_1.setBounds(99, 451, 117, 25)
;
		contentPane.add(btnNewButton_1);
		
		JButton btnNewButton_2 = new JButton(">>");
		btnNewButton_2.setBounds(228, 451, 70, 25);
		contentPane.add(btnNewButton_2);
		
		JButton btnMute = new JButton("Mute");
		btnMute.setBounds(316, 451, 70, 25);
		contentPane.add(btnMute);
		
		JButton btnListen = new JButton("Listen");
		btnListen.setBounds(500, 192, 155, 40);
		contentPane.add(btnListen);
		
		JButton btnCreateMp = new JButton("Create mp3");
		btnCreateMp.setBounds(698, 192, 155, 40);
		contentPane.add(btnCreateMp);
		
		JTextPane textPane = new JTextPane();
		textPane.setBounds(500, 12, 353, 144);
		contentPane.add(textPane);
		
		JButton btnBrowseMp = new JButton("Browse mp3...");
		btnBrowseMp.setBounds(500, 272, 155, 40);
		contentPane.add(btnBrowseMp);
		
		JLabel lblNewLabel = new JLabel("Insert mp3 path");
		lblNewLabel.setBounds(500, 313, 353, 15);
		contentPane.add(lblNewLabel);
		
		JButton btnNewButton_3 = new JButton("Add Commentary\n");
		btnNewButton_3.setFont(new Font("Dialog", Font.BOLD, 22));
		btnNewButton_3.setBounds(500, 365, 353, 111);
		contentPane.add(btnNewButton_3);
	}
}
