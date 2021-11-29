package pacman;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Moteur extends JPanel implements ActionListener {

	private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean dying = false;

    private final int square_size = 24;
    private final int square_number = 15;
    private final int screen_size = square_number * square_size;
    private final int max_number_ghosts = 12;
    private final int pacman_speed = 6;

    private int ghosts_number = 6;
    private int lives_left, score;
    private int[] dx, dy;
    private int[] ghost_pos_x, ghost_pos_y, ghost_direction_x, ghost_direction_y, ghost_speed;

    private Image heart, ghost;
    private Image up, down, left, right;

    private int pacman_pos_x, pacman_pos_y, pacman_direction_x, pacman_direction_y;
    private int req_dx, req_dy;

    private final short mapMarkup[] = {
    	19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
        17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
        19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
        17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
        21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
        17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
        25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 6;

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Moteur() {

        loadImages();
        initVariables();
        addKeyListener(new TAdapter());
        setFocusable(true);
        initGame();
    }
    
    
    
    public Image getImageFromUrl(String url) {
    	
		try {
			URL newUrl = new URL(url);
			Image img = ImageIO.read(newUrl);
			return img;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
			return null;
		}
    	
    }
    
    
    private void loadImages() {
    	
    	
    	down = new ImageIcon(getImageFromUrl("https://raw.githubusercontent.com/Gaspared/Pacman/master/images/down.gif")).getImage();
    	up = new ImageIcon(getImageFromUrl("https://raw.githubusercontent.com/Gaspared/Pacman/master/images/up.gif")).getImage();
    	left = new ImageIcon(getImageFromUrl("https://raw.githubusercontent.com/Gaspared/Pacman/master/images/left.gif")).getImage();
    	right = new ImageIcon(getImageFromUrl("https://raw.githubusercontent.com/Gaspared/Pacman/master/images/right.gif")).getImage();
        ghost = new ImageIcon(getImageFromUrl("https://raw.githubusercontent.com/Gaspared/Pacman/master/images/ghost.gif")).getImage();
        heart = new ImageIcon(getImageFromUrl("https://raw.githubusercontent.com/Gaspared/Pacman/master/images/heart.png")).getImage();

    }
       private void initVariables() {

        screenData = new short[square_number * square_number];
        d = new Dimension(400, 400);
        ghost_pos_x = new int[max_number_ghosts];
        ghost_direction_x = new int[max_number_ghosts];
        ghost_pos_y = new int[max_number_ghosts];
        ghost_direction_y = new int[max_number_ghosts];
        ghost_speed = new int[max_number_ghosts];
        dx = new int[4];
        dy = new int[4];
        
        timer = new Timer(40, this);
        timer.start();
    }

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {
 
    	String start = "Press SPACE to start";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, (screen_size)/4, 150);
    }

    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, screen_size / 2 + 96, screen_size + 16);

        for (int i = 0; i < lives_left; i++) {
            g.drawImage(heart, i * 28 + 8, screen_size + 1, this);
        }
    }

    private void checkMaze() {

        int i = 0;
        boolean finished = true;

        while (i < square_number * square_number && finished) {

            if ((screenData[i]) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {

            score += 50;

            if (ghosts_number < max_number_ghosts) {
                ghosts_number++;
            }

            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

    	lives_left--;

        if (lives_left == 0) {
            inGame = false;
        }

        continueLevel();
    }

    private void moveGhosts(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < ghosts_number; i++) {
            if (ghost_pos_x[i] % square_size == 0 && ghost_pos_y[i] % square_size == 0) {
                pos = ghost_pos_x[i] / square_size + square_number * (ghost_pos_y[i] / square_size);

                count = 0;

                if ((screenData[pos] & 1) == 0 && ghost_direction_x[i] != 1) {
                	System.out.print(screenData[pos]);
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_direction_y[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_direction_x[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_direction_y[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {

                    if ((screenData[pos] & 15) == 15) {
                        ghost_direction_x[i] = 0;
                        ghost_direction_y[i] = 0;
                    } else {
                        ghost_direction_x[i] = -ghost_direction_x[i];
                        ghost_direction_y[i] = -ghost_direction_y[i];
                    }

                } else {

                    count = (int) (Math.random() * count);

                    if (count > 3) {
                        count = 3;
                    }

                    ghost_direction_x[i] = dx[count];
                    ghost_direction_y[i] = dy[count];
                }

            }

            ghost_pos_x[i] = ghost_pos_x[i] + (ghost_direction_x[i] * ghost_speed[i]);
            ghost_pos_y[i] = ghost_pos_y[i] + (ghost_direction_y[i] * ghost_speed[i]);
            drawGhost(g2d, ghost_pos_x[i] + 1, ghost_pos_y[i] + 1);

            if (pacman_pos_x > (ghost_pos_x[i] - 12) && pacman_pos_x < (ghost_pos_x[i] + 12)
                    && pacman_pos_y > (ghost_pos_y[i] - 12) && pacman_pos_y < (ghost_pos_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {
    	g2d.drawImage(ghost, x, y, this);
        }

    private void movePacman() {

        int pos;
        short ch;

        if (pacman_pos_x % square_size == 0 && pacman_pos_y % square_size == 0) {
            pos = pacman_pos_x / square_size + square_number * (int) (pacman_pos_y / square_size);
            ch = screenData[pos];
            System.out.print(ch + "|");

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacman_direction_x = req_dx;
                    pacman_direction_y = req_dy;
                }
            }

            // Check for standstill
            if ((pacman_direction_x == -1 && pacman_direction_y == 0 && (ch & 1) != 0)
                    || (pacman_direction_x == 1 && pacman_direction_y == 0 && (ch & 4) != 0)
                    || (pacman_direction_x == 0 && pacman_direction_y == -1 && (ch & 2) != 0)
                    || (pacman_direction_x == 0 && pacman_direction_y == 1 && (ch & 8) != 0)) {
                pacman_direction_x = 0;
                pacman_direction_y = 0;
            }
        } 
        pacman_pos_x = pacman_pos_x + pacman_speed * pacman_direction_x;
        pacman_pos_y = pacman_pos_y + pacman_speed * pacman_direction_y;
    }

    private void drawPacman(Graphics2D g2d) {

        if (req_dx == -1) {
        	g2d.drawImage(left, pacman_pos_x + 1, pacman_pos_y + 1, this);
        } else if (req_dx == 1) {
        	g2d.drawImage(right, pacman_pos_x + 1, pacman_pos_y + 1, this);
        } else if (req_dy == -1) {
        	g2d.drawImage(up, pacman_pos_x + 1, pacman_pos_y + 1, this);
        } else {
        	g2d.drawImage(down, pacman_pos_x + 1, pacman_pos_y + 1, this);
        }
    }

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < screen_size; y += square_size) {
            for (x = 0; x < screen_size; x += square_size) {
                g2d.setColor(new Color(0,72,251));
                g2d.setStroke(new BasicStroke(5));
                
                if ((mapMarkup[i] == 0)) { 
                	g2d.fillRect(x, y, square_size, square_size);
                }  else if ((screenData[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + square_size - 1);
                }  else if ((screenData[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + square_size - 1, y);
                } else if ((screenData[i] & 4) != 0) { 
                    g2d.drawLine(x + square_size - 1, y, x + square_size - 1,
                            y + square_size - 1);
                } else if ((screenData[i] & 8) != 0) { 
                    g2d.drawLine(x, y + square_size - 1, x + square_size - 1,
                            y + square_size - 1);
                } else if ((screenData[i] & 16) != 0) { 
                    g2d.setColor(new Color(255,255,255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
               }

                i++;
            }
        }
    }



    private void initGame() {

        initLevel();
      }

    private void initLevel() {
    	lives_left = 3;
    	ghosts_number = 6;
        currentSpeed = 3;
    	score = 0;
        int i;
        for (i = 0; i < square_number * square_number; i++) {
            screenData[i] = mapMarkup[i];
        }

        continueLevel();
    }

    private void continueLevel() {

    	int dx = 1;
        int random;

        for (int i = 0; i < ghosts_number; i++) {

            ghost_pos_y[i] = 4 * square_size; //start position
            ghost_pos_x[i] = 4 * square_size;
            ghost_direction_y[i] = 0;
            ghost_direction_x[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghost_speed[i] = validSpeeds[random];
        }

        pacman_pos_x = 7 * square_size;  //start position
        pacman_pos_y = 11 * square_size;
        pacman_direction_x = 0;	//reset direction move
        pacman_direction_y = 0;
        req_dx = 0;		// reset direction controls
        req_dy = 0;
        dying = false;
    }

 
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    //controls
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    inGame = false;
                } 
            } else {
                    inGame = true;
                    initGame();
                
            }
        }
}

	
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
		
	}