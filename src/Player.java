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
	Image image;

	Player(int x1, int y1, int d, Image m) {
		x = x1;
		y = y1;
		dir = d;
		image = m;
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
		gc.drawImage(image, px, py, Grab.CELLSIZE - 1, Grab.CELLSIZE - 1);
		gc.setFill(Color.BLACK);
		// Direction circles
		switch (dir) {
		case UP:
			gc.fillOval(px + Grab.CELLSIZE / 5.2, py+Grab.CELLSIZE / 9, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			gc.fillOval(px + Grab.CELLSIZE / 1.6, py+Grab.CELLSIZE / 9, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			break;
		case RIGHT:
			gc.fillOval(px + Grab.CELLSIZE / 3.1, py+Grab.CELLSIZE / 4, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			gc.fillOval(px + Grab.CELLSIZE / 1.4, py+Grab.CELLSIZE / 4, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			
			break;
		case DOWN:
			gc.fillOval(px + Grab.CELLSIZE / 5.2, py+Grab.CELLSIZE / 3, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			gc.fillOval(px + Grab.CELLSIZE / 1.6, py+Grab.CELLSIZE / 3, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			break;
		case LEFT:
			gc.fillOval(px + Grab.CELLSIZE / 9, py+Grab.CELLSIZE / 4, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			gc.fillOval(px + Grab.CELLSIZE / 2, py+Grab.CELLSIZE / 4, Grab.CELLSIZE / 6, Grab.CELLSIZE / 6);
			break;
		}
	}
}
