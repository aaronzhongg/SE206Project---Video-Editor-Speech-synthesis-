package vidivox;

import java.util.List;

import javax.swing.SwingWorker;

public class ProgressBarDoInBackground extends SwingWorker<Void,Float>{
	private Player player;
	
	public ProgressBarDoInBackground(Player player) {
		this.player = player;
	}
	
	//updates the progress bar as the video is playing
	@Override
	protected Void doInBackground() throws Exception {
		
		while (player.video.getPosition() != 1){
			publish(player.video.getPosition());
			Thread.sleep(400);
		}
		return null;
	}
	
	@Override
	protected void process(List<Float> chunks){
		for (float l:chunks){
			player.vidSlider.setValue(Math.round(l*100));
		}
	}

}
