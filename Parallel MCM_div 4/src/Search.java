public class Search extends Dir {
    private int id;                // Searcher identifier
    private int pos_row, pos_col;        // Position in the grid
    private int steps; //number of steps to end of search
    private boolean stopped;            // Did the search hit a previous trail?

    private TerrainArea terrain;

    public Search(int id, int pos_row, int pos_col, TerrainArea terrain) {
        this.id = id;
        this.pos_row = pos_row; //randomly allocated
        this.pos_col = pos_col; //randomly allocated
        this.terrain = terrain;
        this.stopped = false;
    }

    public int find_valleys() {
        int height = Integer.MAX_VALUE;
        Direction next = Direction.STAY_HERE;
        while (terrain.visited(pos_row, pos_col) == 0) { // stop when hit existing path
            height = terrain.get_height(pos_row, pos_col);
            terrain.mark_visited(pos_row, pos_col, id); // mark current position as visited
            steps++;
            next = terrain.next_step(pos_row, pos_col);

            int new_row = pos_row;
            int new_col = pos_col;

            switch (next) {
                case STAY_HERE:
                    return height; // found local valley
                case LEFT:
                    new_row--;
                    break;
                case RIGHT:
                    new_row++;
                    break;
                case UP:
                    new_col--;
                    break;
                case DOWN:
                    new_col++;
                    break;
            }

            // Check if new coordinates are within bounds
            if (new_row >= 0 && new_row < terrain.getRows() && new_col >= 0 && new_col < terrain.getColumns()) {
                pos_row = new_row;
                pos_col = new_col;
            } else {
                // Handle moving out of bounds, e.g., stop the search or wrap around if appropriate
                stopped = true;
                return height; // or do something else
            }
        }
        stopped = true;
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