package MonteCarloMini;

/* M. Kuttel 2023
 * Searcher class that lands somewhere random on the surfaces and 
 * then moves downhill, stopping at the local minimum.
 */

public class Search {
	private int id;				// Searcher identifier
	private int pos_row, pos_col;		// Position in the grid
	private int steps; //number of steps to end of search
	private boolean stopped;			// Did the search hit a previous trail?
	
	private TerrainArea terrain;
	enum Direction {
		STAY_HERE,
	    LEFT,
	    RIGHT,
	    UP,
	    DOWN
	  }

	public Search(int id, int pos_row, int pos_col, TerrainArea terrain) {
		this.id = id;
		this.pos_row = pos_row; //randomly allocated
		this.pos_col = pos_col; //randomly allocated
		this.terrain = terrain;
		this.stopped = false;
	}
	
	public int find_valleys() {	
		int height=Integer.MAX_VALUE;
		Direction next = Direction.STAY_HERE;
		while(terrain.visited(pos_row, pos_col)==0) { // stop when hit existing path
			height=terrain.get_height(pos_row, pos_col);
			terrain.mark_visited(pos_row, pos_col, id); //mark current position as visited
			steps++;
			next = terrain.next_step(pos_row, pos_col);
			switch(next) {
				case STAY_HERE: return height; //found local valley
				case LEFT: 
					pos_row--;
					break;
				case RIGHT:
					pos_row=pos_row+1;
					break;
				case UP: 
					pos_col=pos_col-1;
					break;
				case DOWN: 
					pos_col=pos_col+1;
					break;
			}
		}
		stopped=true;
		return height;
	}

	public int getID() {
		return id;
	}

	public int getPos_row() {
		return pos_row;
	}

	public int getPos_col() {
		return pos_col;
	}

	public int getSteps() {
		return steps;
	}
	public boolean isStopped() {
		return stopped;
	}

}
