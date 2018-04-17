
/*
 * Grab.java
 *
 * This is the Client for the Grab game.
 * Two-player, networked game.  Run around and grab coins.
 *
 * Code modified from NetOthello (Black Art of Java Game Programming)
 *
 * modified by mike slattery - apr 2000
 * changed to application, mcs - mar 2015
 * Ported to JavaFX - mcs, mar 2018
 */
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.net.*;
import java.io.*;

public class Grab extends Application implements Runnable {
	final String appName = "Grab";
	
	GraphicsContext gc; // declare here to use in handlers

	/* the Thread */
	Thread kicker;

	int grid[][] = new int[GameGroup.GWD][GameGroup.GHT]; // Game board
	static int grabbed[][] = new int[GameGroup.GWD][GameGroup.GHT];
	public static int bcoins, rcoins, btnt = 4, rtnt = 4;
	public static boolean b_no_tnt = false;

	public static boolean r_no_tnt = false;
	public static final int CELLSIZE = 64;
	public static final int WIDTH = GameGroup.GWD * CELLSIZE;
	public static final int HEIGHT = GameGroup.GHT * CELLSIZE;
	boolean setup = false; // record whether we've got the board yet
	Player blue = null, red = null;
	String my_name;

	/* the network stuff */
	PrintWriter pw;
	Socket s = null;
	BufferedReader br = null;
	String name, theHost = "localhost";
	int thePort = 2001;
	int once = 0;
	int once2 = 0;
	
	Font font = Font.font("Helvetica", FontWeight.BOLD, 15);
	Image lilypad, dragonfly, p1, p2;
	Media forest_noise;
	AudioClip eat;
	MediaPlayer media;

	void initialize() {
		makeContact();
		/* start a new game */
		/* start the thread */
		kicker = new Thread(this);
		kicker.setPriority(Thread.MIN_PRIORITY);
		kicker.setDaemon(true);
		kicker.start();
		
		lilypad = new Image("file:src/sprites/lilypad.png");
		dragonfly  = new Image("file:src/sprites/dragonfly.gif");
		p1  = new Image("file:src/sprites/p1_0.png");
		p2  = new Image("file:src/sprites/p2_0.png");
		forest_noise = new Media(ClassLoader.getSystemResource("audio/forest_noise.mp3").toString());
		eat = new AudioClip(ClassLoader.getSystemResource("audio/bite.mp3").toString());
		render(gc);
	}

	private void makeContact()
	// contact the GameServer
	{
		/* ok, now make the socket connection */
		while (s == null)
			try {
				System.out.println("Attempting to make connection:" + theHost + ", " + thePort);
				s = new Socket(theHost, thePort);
				br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				pw = new PrintWriter(s.getOutputStream());
			} catch (Exception e) {
				System.out.println(e);
				try {
					Thread.sleep(7500);
				} catch (Exception ex) {
				}
				;
			}

		System.out.println("Connection established");

	} // end of makeContact()

	/* the main Thread loop */
	public void run() {

		/*
		 * Here is the main network loop Wait for messages from the server
		 */
		while (kicker != null) {
			String input = null;
			
			while (input == null)
				try {
					Thread.sleep(100);
					input = br.readLine();
				} catch (Exception e) {
					input = null;
				}

			System.out.println("Got input: " + input);

			// Chop up the message and see what to do
			String[] words = input.split(",");
			String cmd = words[0];

			/* if we are ready to start a game */
			if (cmd.equals("start")) {
				fillGrid(words[1]);
				setup = true;
				render(gc);
			} else if (cmd.equals("who")) {
				my_name = words[1];
			} else if (cmd.equals("blue")) {
				try {
					if (blue == null) {
						blue = new Player(0, 0, 0, p1);
						//blue = new Player(0, 0, 0, "p1");
					}
					blue.x = Integer.valueOf(words[1]).intValue();
					blue.y = Integer.valueOf(words[2]).intValue();
					blue.dir = Integer.valueOf(words[3]).intValue();
				} catch (Exception e) {
				}
				; // if nonsense message, just ignore it
				render(gc);
			} else if (cmd.equals("red")) {
				try {
					if (red == null) {
						red = new Player(0, 0, 0, p2);
						//red = new Player(0, 0, 0, "p2");
					}
					red.x = Integer.valueOf(words[1]).intValue();
					red.y = Integer.valueOf(words[2]).intValue();
					red.dir = Integer.valueOf(words[3]).intValue();
				} catch (Exception e) {
				}
				; // if nonsense message, just ignore it
				render(gc);
			}
			else if (cmd.equals("grabbed")) {
				try {
					grid[Integer.valueOf(words[2]).intValue()][Integer.valueOf(words[3]).intValue()] = 0;
					if(words[1].equals("blue")) {
						System.out.println("Got blue coins: ");
						bcoins++;
					}
					else if(words[1].equals("red")) {
						System.out.println("Got red coins: ");
						rcoins++;
					}
				}
				catch(Exception e) {
				};
				render(gc);
			} // end grabbed command
			else if (cmd.equals("blasted")) {
			    try {
					grid[Integer.valueOf(words[2]).intValue()][Integer.valueOf(words[3]).intValue()] = 0;
				}	// end try
				catch(Exception e) {
				};
				render(gc);
			} // end blasted command
		}
		
	}	// end run()

	void fillGrid(String board) {
		// Fill in the grid array with the values
		// in the String board.
		int x, y, i = 0;
		char c;

		for (y = 0; y < GameGroup.GHT; y++)				// height
			for (x = 0; x < GameGroup.GWD; x++) {		// width
				c = board.charAt(i);
				i++;
				switch (c) {
				case '0':
					grid[x][y] = 0;
					break;
				case '1':
					grid[x][y] = 1;
					break;
				case '2':
					grid[x][y] = 2;			// Fill with orange coins if '2'
					break;
				}
			}
	}

	/* if the Thread stops, be sure to clean up! */
	public void finalize() {

		try {
			br.close();
			pw.close();
			s.close();
		} catch (Exception e) {
		}
		;
	}

	public void render(GraphicsContext gc) {
		int x, y;

		gc.setFill(Color.rgb(66, 134, 244));
		gc.fillRect(0, 0, WIDTH, HEIGHT);
		if (!setup) {
			gc.setFill(Color.BLACK);
			gc.fillText("Waiting...", 50, 50);
		} else {
			
			if(once < 1) {
				once++;
		    	media = new MediaPlayer(forest_noise);
				media.setCycleCount(5);
				media.play();
				media.setVolume(0.7);
			}
			
			// Draw board
			for (x = 0; x < GameGroup.GWD; x++)
				for (y = 0; y < GameGroup.GHT; y++) {
					if (grid[x][y] == 1) {
						gc.drawImage(lilypad, CELLSIZE * x, CELLSIZE * y, CELLSIZE - 1, CELLSIZE - 1);
					}
					else if (grid[x][y] == 2) {
						gc.drawImage(dragonfly, CELLSIZE * x + 2, CELLSIZE * y + 2, CELLSIZE - 4, CELLSIZE - 4);
					}
				}
			gc.setStroke(Color.BLACK);
			gc.strokeRect(0, 0, WIDTH, HEIGHT);
			// Add the players if they're there
			if (blue != null)
				blue.render(gc);
			if (red != null)
				red.render(gc);
		}
		gc.setFont(font);
		gc.setFill(Color.BLUE);
		gc.fillText("P1:\nFlies: "+bcoins, 50, 50);
		gc.fillText("TNT: "+btnt, 50, 80);
		
		gc.setFill(Color.RED);
		gc.fillText("P2:\nFlies: "+rcoins, WIDTH-100, 50);
		gc.fillText("TNT: "+rtnt, WIDTH-100, 80);
	}

	public void tellServer(String msg) {
		/* send a message to the server */
		boolean flag = false;
		while (!flag) // we keep trying until it's sent
			try {
				pw.println(msg);
				pw.flush();
				flag = true;
			} catch (Exception e1) {
				flag = false;
			}
	}

	void setHandlers(Scene scene) {
		scene.setOnKeyPressed(e -> {
			KeyCode c = e.getCode();
			switch (c) {
			case J:
			case LEFT:
				tellServer("turnleft," + my_name);
				break;
			case L:
			case RIGHT:
				tellServer("turnright," + my_name);
				break;
			case K:
			case UP:
				tellServer("step," + my_name);
				break; 
			case G:
				tellServer("trygrab," + my_name);
				break;
			case B:
				tellServer("tryblast," + my_name);
				break;
			default: /* Do Nothing */
				break;
			}
		});
	}

	/*
	 * Begin boiler-plate code... [Events with initialization]
	 */
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage theStage) {
		theStage.setTitle(appName);

		Group root = new Group();
		Scene theScene = new Scene(root);
		theStage.setScene(theScene);

		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		root.getChildren().add(canvas);

		gc = canvas.getGraphicsContext2D();

		// Initial setup
		initialize();

		setHandlers(theScene);

		theStage.show();
	}
	/*
	 * ... End boiler-plate code
	 */
}
