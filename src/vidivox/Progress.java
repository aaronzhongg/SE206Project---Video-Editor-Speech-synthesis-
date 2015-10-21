package vidivox;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JProgressBar;
import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Window.Type;

/*
 * This class is a popup window that has a progress bar showing something is happening while merging audio/video
 */
public class Progress extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Progress frame = new Progress();
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
	public Progress() {
		setTitle("Please wait\n");
		setResizable(false);
		setAlwaysOnTop(true);
		setEnabled(false);
		setBounds(100, 100, 274, 127);
		contentPane = new JPanel();
		contentPane.setBackground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		//Progress bar that is set to indeterminate to show user stuff is happening
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBackground(Color.LIGHT_GRAY);
		progressBar.setForeground(UIManager.getColor("CheckBoxMenuItem.acceleratorForeground"));
		progressBar.setBounds(12, 49, 240, 31);
		progressBar.setIndeterminate(true);
		contentPane.add(progressBar);
		
		//Label to let user know that the file is being generated
		JLabel lblPleasWait = new JLabel("Generating file. Please wait...");
		lblPleasWait.setHorizontalAlignment(SwingConstants.CENTER);
		lblPleasWait.setForeground(Color.WHITE);
		lblPleasWait.setBounds(12, 12, 240, 15);
		contentPane.add(lblPleasWait);
	}
}
