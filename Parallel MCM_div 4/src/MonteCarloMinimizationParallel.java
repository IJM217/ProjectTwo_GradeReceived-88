//Isael Masters
//MSTISR001
//Assigmnet 1

import java.util.Random; 
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MonteCarloMinimizationParallel extends RecursiveAction {
    static long startTime = 0;
    static long endTime = 0;

    static private int CutOff;//serial cutoff for threads
    private static int Finder;
    private static int Min;
    static private Search[] Searches;
    private int start,end;
    
    //timers - note milliseconds
    private static void tick() {
        startTime = System.currentTimeMillis();
    }

    private static void tock() {
        endTime = System.currentTimeMillis();
    }
    public MonteCarloMinimizationParallel( int Start,int End,Search[] Search){
        start = Start;
        end = End;
        Searches = Search;
    }
    public static void setCutOff(int C){
        CutOff = C;
    }

    public void setFinder(int F){
        Finder = F;
    }

    public int getFinder(){
        return Finder;
    }

    public void setMin(int m) {
        Min = m;
    }

    public int getMin() {
        return Min;
    }

    @Override
    protected void compute(){

        if((end - start) <= CutOff) {//threads start searching
            int min = Integer.MAX_VALUE;
            int local_min = Integer.MAX_VALUE;
            int finder = -1;

            for (int i = start; i < end; i++) {
                local_min = Searches[i].find_valleys();

                if ((!Searches[i].isStopped()) && (local_min < min)) { //don't look at those who stopped because hit existing path
                    min = local_min;
                    finder = i; //keep track of who found it
                    setMin(min);//records the min value
                    setFinder(finder);// records finder value to get coords later
                }
            }
        }
        else{
            //splits work into 4 parts for threads
            MonteCarloMinimizationParallel one = new MonteCarloMinimizationParallel(start,end/4,Searches);
            MonteCarloMinimizationParallel two = new MonteCarloMinimizationParallel(end/4,end/2,Searches);
            MonteCarloMinimizationParallel three = new MonteCarloMinimizationParallel(end/2,(end/4)+(end/2),Searches);
            MonteCarloMinimizationParallel four = new MonteCarloMinimizationParallel((end/4)+(end/2),end,Searches);

            one.fork();
            two.fork();
            three.fork();
            four.fork();
            one.join();
            two.join();
            three.join();
            four.join();
        }

    }

    public static void main(String[] args) {

        int rows, columns; //grid size
        double xmin, xmax, ymin, ymax; //x and y terrain limits
          //object to store the heights and grid points visited by searches
        double searches_density;    // Density - number of Monte Carlo  searches per grid position - usually less than 1!
        TerrainArea terrain;
        int num_s;        // Number of searches
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
        num_s = (int) (rows * columns * searches_density);
        searches = new Search[num_s];
        for (int i = 0; i < num_s; i++)
            searches[i] = new Search(i + 1, rand.nextInt(rows), rand.nextInt(columns), terrain);

        MonteCarloMinimizationParallel MCMP = new MonteCarloMinimizationParallel(0,num_s,searches);
        setCutOff(num_s/4);
        ForkJoinPool Deadpool = new ForkJoinPool();

        //start timer
        tick();
        //runs threads threads
        Deadpool.invoke(MCMP);
        //end timer
        tock();

        System.out.print("Run parameters\n");
        System.out.printf("\t Rows: %d, Columns: %d\n", rows, columns);
        System.out.printf("\t x: [%f, %f], y: [%f, %f]\n", xmin, xmax, ymin, ymax);
        System.out.printf("\t Search density: %f (%d searches)\n", searches_density, num_s);

        /*  Total computation time */
        System.out.printf("Time: %d ms\n", endTime - startTime);
        int tmp = terrain.getGrid_points_visited();
        System.out.printf("Grid points visited: %d  (%2.0f%s)\n", tmp, (tmp / (rows * columns * 1.0)) * 100.0, "%");
        tmp = terrain.getGrid_points_evaluated();
        System.out.printf("Grid points evaluated: %d  (%2.0f%s)\n", tmp, (tmp / (rows * columns * 1.0)) * 100.0, "%");

        /* Results*/
        System.out.printf("Global minimum: %d at x=%.1f y=%.1f\n\n", MCMP.getMin(), terrain.getXcoord(searches[MCMP.getFinder()].getPos_row()), terrain.getYcoord(searches[MCMP.getFinder()].getPos_col()));
    }
}