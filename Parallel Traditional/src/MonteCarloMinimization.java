//Isael Masters
//MSTISR001
//Assigmnet 1

import java.util.Random;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ForkJoinPool;

class ParaSearch extends RecursiveAction {
    private Search[] searches;
    private int start;//start of array in thread
    private int end;//end of array in thread
    private TerrainArea terrain;
    private static int globalMinValue = Integer.MAX_VALUE;
    private static int globalMinRow = -1;
    private static int globalMinCol = -1;

    public ParaSearch(Search[] searches, int start, int end, TerrainArea terrain) {
        this.searches = searches;
        this.start = start;
        this.end = end;
        this.terrain = terrain;
    }
    public int getGlobalMinCol() {
        return globalMinCol;
    }

    public int getGlobalMinRow() {
        return globalMinRow;
    }

    public int getGlobalMinValue() {
        return globalMinValue;
    }

        @Override
    protected void compute() {
        if ((end - start) <= 69) {//noice
            //searching within each thread
            for (int i = start; i < end; i++) {
                int local_min = searches[i].find_valleys();
                if ((!searches[i].isStopped()) && (local_min < globalMinValue)) {
                    globalMinValue = local_min;//to record the minimum value
                    globalMinRow = searches[i].getPos_row();//records the x-value of minimum
                    globalMinCol = searches[i].getPos_col();//records the y-value of minimum
                }
            }
        } else {
            int mid = (start + end) / 2;// splitting work into smaller groups for threads
            ParaSearch leftTask = new ParaSearch(searches, start, mid, terrain);
            ParaSearch rightTask = new ParaSearch(searches, mid, end, terrain);
            invokeAll(leftTask, rightTask);
        }
    }
}

public class MonteCarloMinimization {

    static long startTime = 0;
    static long endTime = 0;

    //timers - note milliseconds
    private static void tick() {
        startTime = System.currentTimeMillis();
    }

    private static void tock() {
        endTime = System.currentTimeMillis();
    }

    public static void main(String[] args) {

        int rows, columns; //grid size
        double xmin, xmax, ymin, ymax; //x and y terrain limits
        TerrainArea terrain;  //object to store the heights and grid points visited by searches
        double searches_density;    // Density - number of Monte Carlo  searches per grid position - usually less than 1!

        int num_searches;        // Number of searches
        Search[] searches;        // Array of searches
        Random rand = new Random();  //the random number generator

        if (args.length!=7) {
            System.out.println("Incorrect number of command line arguments provided.");
            System.exit(0);
        }
        /* Read argument values */
        rows =Integer.parseInt( args[0] );
        columns = Integer.parseInt( args[1] );
        xmin = Double.parseDouble(args[2] );
        xmax = Double.parseDouble(args[3] );
        ymin = Double.parseDouble(args[4] );
        ymax = Double.parseDouble(args[5] );
        searches_density = Double.parseDouble(args[6] );

        // Initialize
        terrain = new TerrainArea(rows, columns, xmin, xmax, ymin, ymax);
        num_searches = (int) (rows * columns * searches_density);
        searches = new Search[num_searches];
        for (int i = 0; i < num_searches; i++)
            searches[i] = new Search(i + 1, rand.nextInt(rows), rand.nextInt(columns), terrain);

        ParaSearch PS = new ParaSearch(searches, 0, num_searches, terrain);
        ForkJoinPool Deadpool = new ForkJoinPool();

        //start timer
        tick();
        //runs threads for search
        Deadpool.invoke(PS);
        //end timer
        tock();

        System.out.printf("Run parameters\n");
        System.out.printf("\t Rows: %d, Columns: %d\n", rows, columns);
        System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax);
        System.out.printf("\t Search density: %f (%d searches)\n", searches_density, num_searches);

        /*  Total computation time */
        System.out.printf("Time: %d ms\n", endTime - startTime);
        int tmp = terrain.getGrid_points_visited();
        System.out.printf("Grid points visited: %d  (%2.0f%s)\n", tmp, (tmp / (rows * columns * 1.0)) * 100.0, "%");
        tmp = terrain.getGrid_points_evaluated();
        System.out.printf("Grid points evaluated: %d  (%2.0f%s)\n", tmp, (tmp / (rows * columns * 1.0)) * 100.0, "%");

        /* Results */
        System.out.printf("Global minimum: %d at x=%.1f y=%.1f\n\n",PS.getGlobalMinValue(),terrain.getXcoord(PS.getGlobalMinRow()),terrain.getYcoord(PS.getGlobalMinCol()));
    }
}