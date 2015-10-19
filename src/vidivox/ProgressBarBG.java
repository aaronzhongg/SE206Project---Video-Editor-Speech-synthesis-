package vidivox;

import java.util.List;

import javax.swing.SwingWorker;

public class ProgressBarBG extends SwingWorker<Void,Float>{
	private PlayerMedia player;
	
	public ProgressBarBG(PlayerMedia player) {
		this.player = player;
	}
	
	//updates the progress bar as the video is playing
	@Override
	protected Void doInBackground() throws Exception {
		
		while (player.video.getPosition() != 1){
			publish(player.video.getPosition());
			Thread.sleep(200);
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
