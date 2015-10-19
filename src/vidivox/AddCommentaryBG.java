package vidivox;

import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class AddCommentaryBG extends SwingWorker<Void, Void>{
	//Fields include the actual player (for accessing files) and the output name
	private Player player;
	private String comOutName;
	public AddCommentaryBG(Player p, String n){
		player = p;
		comOutName = n;
	}
	@Override
	protected Void doInBackground() throws Exception {
		String audioInfo = "";
		for (int i = 0; i < player.edit.numAudio - 1; i ++) {
			String[] temp = ((String) player.edit.audioTable.getValueAt(i,1)).split(":");
			int min = Integer.parseInt(temp[0]);
			int sec = Integer.parseInt(temp[1]);
			int time = 60*min + sec;
			audioInfo = audioInfo + " -itsoffset " + time + " -i " + player.edit.mp3File.get(i).getAbsolutePath();
		}

		//FFMPEG commands to split audio from video, combine the two audios and re attache the audio and video  
		ProcessBuilder splitter = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i " + player.videoFile.getAbsolutePath() + audioInfo + " -filter_complex amix=inputs=" + player.edit.numAudio +":duration=first -async 1 tempjekjek.mp3");
		ProcessBuilder combiner = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i tempjekjek.mp3 -i " + player.videoFile.getAbsolutePath() + " -map 0:a -map 1:v " + comOutName + ".avi");

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
			JOptionPane.showMessageDialog(null, "Merge Sucessful", "Merge Sucessful", 1);
			File del = new File("tempjekjek.mp3");
			del.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (player.videoFile != null) {
			player.btnAddCom.setEnabled(true);
		}
	}
}
