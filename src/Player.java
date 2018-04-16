import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Player {
	// A simple class to keep track of each player
	// (this class is used by Server and Client)
	int x, y, tnt;
	int dir;
	Color color;

	// Directions
	public static final int UP = 0;
	public static final int RIGHT = 1;
	public static final int DOWN = 2;
	public static final int LEFT = 3;

	// P1 images
	static Image p1_0 = new Image("sprites/p1_0.gif");
	static Image p1_1 = new Image("sprites/p1_1.gif");
	static Image p1_2 = new Image("sprites/p1_2.gif");
	static Image p1_3 = new Image("sprites/p1_3.gif");
	
	// P2 images
	static Image p2_0 = new Image("sprites/p2_0.gif");
	static Image p2_1 = new Image("sprites/p2_1.gif");
	static Image p2_2 = new Image("sprites/p2_2.gif");
	static Image p2_3 = new Image("sprites/p2_3.gif");
	
	Image img;
	Player(int x1, int y1, int d, String p) {
		x = x1;
		y = y1;
		dir = d;
		if(p.equals("p1")) {
			img = p1_0;
		}
		else if(p.equals("p2")) {
			img = p2_0;
		}
	}

	public void turnLeft() {
		dir--;
		if (dir < UP)
			dir = LEFT;
	}

	public void turnRight() {
		dir++;
		if (dir > LEFT)
			dir = UP;
	}

	public void render(GraphicsContext gc) {
		int px = Grab.CELLSIZE * x;
		int py = Grab.CELLSIZE * y;
		gc.drawImage(img,px, py, Grab.CELLSIZE - 1, Grab.CELLSIZE - 1);
	//	gc.fillOval(px, py, Grab.CELLSIZE - 1, Grab.CELLSIZE - 1);
		
		gc.setFill(Color.BLACK);
		// Direction circles
		switch (dir) {
		case UP:
			gc.fillOval(px + Grab.CELLSIZE / 4, py, Grab.CELLSIZE / 2, Grab.CELLSIZE / 2);
			break;
		case RIGHT:
			gc.fillOval(px + Grab.CELLSIZE / 2, py + Grab.CELLSIZE / 4, Grab.CELLSIZE / 2, Grab.CELLSIZE / 2);
			break;
		case DOWN:
			gc.fillOval(px + Grab.CELLSIZE / 4, py + Grab.CELLSIZE / 2, Grab.CELLSIZE / 2, Grab.CELLSIZE / 2);
			break;
		case LEFT:
			gc.fillOval(px, py + Grab.CELLSIZE / 4, Grab.CELLSIZE / 2, Grab.CELLSIZE / 2);
			break;
		}
	}
}
