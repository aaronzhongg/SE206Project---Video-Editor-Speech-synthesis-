package vidivox;

import java.io.IOException;

import javax.swing.SwingWorker;

public class ListenBG extends SwingWorker<Void, Void>{
	private PlayerMedia player;
	
	public ListenBG(PlayerMedia p){
		player = p;
	}
	@Override
	protected Void doInBackground() throws Exception {
		//run festival in bash from the entered text
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", "echo " + PlayerSideBar.txtArea.getText() + " | festival --tts");

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
		PlayerSideBar.btnListen.setEnabled(true);
	}
}
