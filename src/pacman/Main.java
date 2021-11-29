package pacman;

import javax.swing.JFrame;

public class Main extends JFrame{

	public Main() {
		add(new Moteur());
	}
	
	
	public static void main(String[] args) {
		Main pac = new Main();
		pac.setVisible(true);
		pac.setTitle("Pacman");
		pac.setSize(380,420);
		pac.setDefaultCloseOperation(EXIT_ON_CLOSE);
		pac.setLocationRelativeTo(null);
		
	}

}