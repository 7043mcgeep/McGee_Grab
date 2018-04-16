import java.net.*;
import java.awt.*;

public class GameGroup extends Thread {

	GameClientThread arr[];
	final int SIZE=2;

	int grid[][];  //map of the board
	//int grabbed[][];	// map of grabbed coins
	int coins;
	public static final int GWD=12; // width
	public static final int GHT=10; // and height of board
	Player red, blue;  //The two players
	public static boolean grabit = false;
	
	// Init to 100 because 0 would ruin my coin-rendering logic (see Grab.java)
	public static int     coin_x = 0;
	public static int     coin_y = 0;

	GameGroup ( Socket s ) {
		arr = new GameClientThread[SIZE];
		addClient( s );
	}

	public void addClient( Socket s ) {
		int x;

		for( x=0; x<SIZE; x++)
			if( arr[x] == null || !arr[x].isAlive() ) {
				arr[x] = new GameClientThread(s,this);
				arr[x].start();
				return ;
				}
	}

	public void run() {
		Point p;

		System.out.println("GameGroup begun");
		//Get a random starting board
		String board = fillGrid();

		//Position the two players - Note, we never use	the colors here
		p = emptySpot();
		blue = new Player(p.x, p.y, (int)(4*Math.random()), null);

		// We also need to mark each player's spot in the grid, so we'll
		// know it's not empty
		grid[p.x][p.y] = 3;
		p = emptySpot();
		red = new Player(p.x, p.y, (int)(4*Math.random()), null);
		grid[p.x][p.y] = 3;

		//Send each player the config.
		output("start,"+board);
		//and player info (including which they are)
		output("blue,"+blue.x+","+blue.y+","+blue.dir);
		output("red,"+red.x+","+red.y+","+red.dir);
		// We don't use output() here, because we need to send
		// different messages to each player
		arr[0].message("who,blue");
		arr[1].message("who,red");
	}

	public String fillGrid()
	{
		// fill in the board at random and return
		// a String representing the board in row-major order
		// Coords are used like screen coords - 0,0 in top-left,
		// first coord is to right, second is down.
		int x,y,i;
		Point p;

		grid = new int[GWD][GHT];
		// Clear grid
		for (x = 0; x < GWD; x++)
		 for (y = 0; y < GHT; y++)
			grid[x][y] = 0;
		// Place blocks
		for (i = 0; i < 40; i++)
		{
			p = emptySpot();
			grid[p.x][p.y] = 1;
		}
		// Place money
		for (i = 0; i < 8; i++)
		{
			p = emptySpot();
			grid[p.x][p.y] = 2;
		}
		//Now, make the string
		StringBuffer sb = new StringBuffer(GHT*GWD);
		for (y = 0; y < GHT; y++)
		 for (x = 0; x < GWD; x++)
			sb.append(grid[x][y]);
		return new String(sb);
	}

	public Point emptySpot()
	{
		int x, y;
		// Find an empty square in the grid
		do
		{
			x = (int)(GWD*Math.random());
			y = (int)(GHT*Math.random());
		} while (grid[x][y] != 0);
		return new Point(x,y);
	}

	public synchronized void processMessage(String msg)
	{
		Player p;

		//System.out.println("pM got:"+msg);

		//Chop up the message, adjust the state, and tell the clients
		String[] words = msg.split(",");
		String cmd = words[0];

		// get the player name and find the correct
		// Player object
		// NOTE: This depends on all of the messages having the
		//   same "command,name" structure.
		String pname = words[1];
		if (pname.equals("blue"))
			p = blue;
		else
			p = red;

		if (cmd.equals("turnleft"))
		{
			p.turnLeft();
			output(pname+","+p.x+","+p.y+","+p.dir);
		}
		else if (cmd.equals("turnright"))
		{
			p.turnRight();
			output(pname+","+p.x+","+p.y+","+p.dir);
		}
		else if (cmd.equals("step"))
		{
			int newx=-1, newy=-1;	//set to illegal subscripts in case the
									//logic below ever fails (at least we'll
									// get a message).

			//Compute new location
			switch(p.dir)
			{
				case Player.UP: newx = p.x; newy = p.y-1;
								if (newy < 0) return;
					break;
				case Player.RIGHT: newx = p.x+1; newy = p.y;
								if (newx >= GameGroup.GWD) return;
					break;
				case Player.DOWN: newx = p.x; newy = p.y+1;
								if (newy >= GameGroup.GHT) return;
					break;
				case Player.LEFT: newx = p.x-1; newy = p.y;
								if (newx < 0) return;
					break;
			}
			if (grid[newx][newy] != 0)
				return;
			// Clear mark in grid first
			grid[p.x][p.y] = 0;
			p.x = newx; p.y = newy;
			// Then, mark the new spot
			grid[p.x][p.y] = 3;
			output(pname+","+p.x+","+p.y+","+p.dir);
		}
		
		/* GRAB COMMAND:
		 * If keypress = 'G' , this command grabs a coin which you are standing next to
		 * and facing. Does nothing if there is no such coin. 
		 */
		else if(cmd.equals("trygrab")) {
			output("trying to grab...,"+pname+","+p.x+","+p.y+","+p.dir);
			
			// Helper function checks if a coin on the grid is adjacent to the player:
			if(adjCoin(p)) {
				coins++;
				output("grab it!,"+pname+","+p.x+","+p.y+","+p.dir+",coins: "+coins);
			}
			else {
				output("couldn't grab!,"+pname+","+p.x+","+p.y+","+p.dir);
			}
		}
		
		/* BLAST COMMAND:
		 * If keypress = 'B', this command takes a block which you are standing next to and
		 * facing and removes the block from the board.
		 */
		else if(cmd.equals("tryblast")) {
			output("trying to blast...,"+pname+","+p.x+","+p.y+","+p.dir);
			
		}
	}

	public void output(String str) {
	// Send a message to each client
		int x;

		for(x=0;x<SIZE;x++)
			if(arr[x] != null)
				arr[x].message(str);
	}

	public boolean full() {
	// Check if we have all our players
		int x;

		for(x=0;x<SIZE;x++)
			if( arr[x] == null )
				return false;
		return true;
	}
	
	/* adjCoin: A subroutine.
	 * Return true if a coin exists adjacent to the player in any of the four directions.
	 * Otherwise, return false.
	 */
	public boolean adjCoin(Player p) {
		
		coin_x = p.x;
		coin_y = p.y;
		
				if((grid[p.x + 1][p.y]) == 2 && p.dir == 1) {	// If a coin exists to the right of the player's position, and player is facing rightward
					coin_x = p.x+1;
					removeCoin(coin_x, coin_y);
					return true;
				}else if(grid[p.x - 1][p.y] == 2 && p.dir == 3) { // If a coin exists to the left of the player's position, and player is facing leftward
					coin_x = p.x-1;
					removeCoin(coin_x, coin_y);
					return true;
				}else if(grid[p.x][p.y -1] == 2 && p.dir == 0) { // If a coin exists above the player's position, and player is facing upward
					coin_y = p.y-1;
					removeCoin(coin_x, coin_y);
					return true;
				}else if(grid[p.x][p.y + 1] == 2 && p.dir == 2) { // If a coin exists below the player's position, and player is facing downward
					coin_y = p.y+1;
					removeCoin(coin_x, coin_y);
					return true;
				}
		
		return false;
	}
	
	/* Removes a coin from the grid. Grid == 0, render white "walkable" square.
	 * 										1		  gray blocking square.
	 * 										2		  orange coin.
	 * Given coin coordinates (x, y), this helper function removes a coin from the playing grid.
	 * by setting that cell value from 2 to 0.
	 * Then, set grabit flag to true (to stop rendering coin).
	 */
	public void removeCoin(int x, int y){
		
		grid[x][y] = 0;			// Make previous coin cell "walkable"
        grabbed(grid, x, y);	// Call to helper function below
		
	}
	
	/* grabbed() subroutine keeps track of the coins that have been grabbed, so that they
	 * will not be rendered anymore.
	 */
	public void grabbed(int grid[][], int x, int y){
		output( "grabbed x y: " + Grab.grabbed[x][y]);
		Grab.grabbed = grid;					// Store current array into 'grab' array
		Grab.grabbed[x][y] = 4;					// Set coin cell to 3 for non-rendering (see Grab.java).
		output("new grabbed x=" + x + " y=" + y  + " = " + Grab.grabbed[x][y]);
	}
}
