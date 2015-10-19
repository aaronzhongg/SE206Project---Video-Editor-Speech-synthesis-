package vidivox;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import vidivox.PlayerMedia;
public class CreateMp3BG extends SwingWorker<Void,Void> {
	private String output;
	private PlayerMedia player;
	
	public CreateMp3BG(PlayerMedia p, String output){
		this.output = output;
		this.player = p;
	}
	
	@Override
	protected Void doInBackground() throws Exception {
		if (output != null){
			//create mp3 file
			ProcessBuilder makeWav = new ProcessBuilder("/bin/bash", "-c", "echo " + PlayerSideBar.txtArea.getText() + " | text2wave -o " + output +".wav");
			ProcessBuilder convert = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -y -i " + output + ".wav -f mp3 "+ output+".mp3");
			try {
				Process process = makeWav.start();
				process.waitFor();
				Process converse = convert.start();
				converse.waitFor();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	protected void done(){

		//remove the wav file that was created
		try {
			JOptionPane.showMessageDialog(null, "Sucessfully created text-to-speech mp3", "Sucess", 1);
			File del = new File(output+".wav");
			del.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
