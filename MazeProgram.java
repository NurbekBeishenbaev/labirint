public class MazeProgram {
    public static void main(String[] args) {
        // Default maze dimensions
        int rows = 15;
        int cols = 15;

        // Create a new maze generator
        MazeGenerator generator = new MazeGenerator(rows, cols);

        // Generate the maze
        int[][] maze = generator.generateMaze();

        // Define start and end points (top-left and bottom-right)
        Point start = new Point(1, 1);
        Point end = new Point(rows - 2, cols - 2);

        // Create pathfinder (for verification that maze is solvable)
        MazePathfinder pathfinder = new MazePathfinder(maze);
        boolean solvable = pathfinder.findPath(start, end);

        if (!solvable) {
            System.out.println("Error: Generated maze is not solvable!");
            return;
        }

        // Start the interactive game
        MazeGame game = new MazeGame(maze, start, end);
        game.play();
    }
}

/**
 * Point.java
 * Simple class to represent a point in the maze
 */
class Point {
    int x;
    int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point point = (Point) obj;
        return x == point.x && y == point.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}

/**
 * MazeGenerator.java
 * Class to generate a random maze using recursive backtracking
 */
class MazeGenerator {
    // Constants for cell types
    public static final int WALL = 1;
    public static final int PATH = 0;

    private int rows;
    private int cols;
    private int[][] maze;
    private java.util.Random random;

    /**
     * Constructor for MazeGenerator
     *
     * @param rows Number of rows in the maze
     * @param cols Number of columns in the maze
     */
    public MazeGenerator(int rows, int cols) {
        // Ensure odd dimensions for the maze
        this.rows = (rows % 2 == 0) ? rows + 1 : rows;
        this.cols = (cols % 2 == 0) ? cols + 1 : cols;
        this.maze = new int[this.rows][this.cols];
        this.random = new java.util.Random();
    }

    /**
     * Generates a random maze using recursive backtracking
     *
     * @return The generated maze
     */
    public int[][] generateMaze() {
        // Initialize maze with all walls
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                maze[i][j] = WALL;
            }
        }

        // Start recursive maze carving from point (1,1)
        carvePassages(1, 1);

        // Ensure start and end points are accessible
        maze[1][1] = PATH;
        maze[rows - 2][cols - 2] = PATH;

        return maze;
    }

    /**
     * Recursively carves passages through the maze
     *
     * @param x Current x position
     * @param y Current y position
     */
    private void carvePassages(int x, int y) {
        // Mark current cell as a path
        maze[x][y] = PATH;

        // Define the four possible directions (dx, dy)
        int[][] directions = {
                {0, 2},  // right
                {2, 0},  // down
                {0, -2}, // left
                {-2, 0}  // up
        };

        // Shuffle directions for randomness
        shuffleArray(directions);

        // Try each direction
        for (int[] dir : directions) {
            int dx = dir[0];
            int dy = dir[1];

            int newX = x + dx;
            int newY = y + dy;

            // Check if the new position is valid and still a wall
            if (newX > 0 && newX < rows - 1 && newY > 0 && newY < cols - 1 && maze[newX][newY] == WALL) {
                // Carve a path by setting the cell between the current and new position to PATH
                maze[x + dx/2][y + dy/2] = PATH;

                // Recursively continue from the new position
                carvePassages(newX, newY);
            }
        }
    }

    /**
     * Shuffles an array using Fisher-Yates algorithm
     *
     * @param array The array to shuffle
     */
    private void shuffleArray(int[][] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);

            // Swap
            int[] temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }
}

/**
 * MazePathfinder.java
 * Class to find a path through the maze using recursion
 */
class MazePathfinder {
    private int[][] maze;
    private boolean[][] visited;
    private java.util.List<Point> solutionPath;

    /**
     * Constructor for MazePathfinder
     *
     * @param maze The maze to solve
     */
    public MazePathfinder(int[][] maze) {
        this.maze = maze;
        this.visited = new boolean[maze.length][maze[0].length];
        this.solutionPath = new java.util.ArrayList<>();
    }

    /**
     * Finds a path from start to end recursively
     *
     * @param start Starting point
     * @param end Ending point
     * @return true if a path is found, false otherwise
     */
    public boolean findPath(Point start, Point end) {
        // Initialize the solution path with the start point
        solutionPath.clear();

        // Initialize visited array
        for (int i = 0; i < visited.length; i++) {
            for (int j = 0; j < visited[0].length; j++) {
                visited[i][j] = false;
            }
        }

        // Start the recursive search
        boolean found = findPathRecursive(start.x, start.y, end.x, end.y);

        // If path found, add the start point to the beginning of the path
        if (found) {
            solutionPath.add(0, start);
        }

        return found;
    }

    /**
     * Recursive method to find a path from current position to the end
     *
     * @param x Current x position
     * @param y Current y position
     * @param endX End x position
     * @param endY End y position
     * @return true if a path is found, false otherwise
     */
    private boolean findPathRecursive(int x, int y, int endX, int endY) {
        // Base case 1: Out of bounds
        if (x < 0 || x >= maze.length || y < 0 || y >= maze[0].length) {
            return false;
        }

        // Base case 2: Wall or already visited
        if (maze[x][y] == MazeGenerator.WALL || visited[x][y]) {
            return false;
        }

        // Base case 3: Reached the end
        if (x == endX && y == endY) {
            solutionPath.add(new Point(x, y));
            return true;
        }

        // Mark current cell as visited
        visited[x][y] = true;

        // Define four possible moves (right, down, left, up)
        int[][] directions = {
                {0, 1},  // right
                {1, 0},  // down
                {0, -1}, // left
                {-1, 0}  // up
        };

        // Try each direction
        for (int[] dir : directions) {
            int newX = x + dir[0];
            int newY = y + dir[1];

            // Recursively try to find a path
            if (findPathRecursive(newX, newY, endX, endY)) {
                // If path found, add current position to the solution path
                solutionPath.add(0, new Point(x, y));
                return true;
            }
        }

        // No path found from this cell
        return false;
    }

    /**
     * Gets the solution path
     *
     * @return List of points in the solution path
     */
    public java.util.List<Point> getSolutionPath() {
        return solutionPath;
    }
}

/**
 * MazeGame.java
 * Class to handle interactive maze gameplay
 */
class MazeGame {
    private int[][] maze;
    private Point playerPosition;
    private Point exit;
    private int moveCount;
    private boolean gameWon;
    private java.util.Scanner scanner;

    // Constants for display
    private static final char WALL_CHAR = '#';
    private static final char PATH_CHAR = ' ';
    private static final char PLAYER_CHAR = 'P';
    private static final char EXIT_CHAR = 'E';
    private static final char VISITED_CHAR = '.';

    /**
     * Constructor for MazeGame
     *
     * @param maze The maze to play
     * @param start Starting position
     * @param exit Exit position
     */
    public MazeGame(int[][] maze, Point start, Point exit) {
        this.maze = maze;
        this.playerPosition = new Point(start.x, start.y);
        this.exit = exit;
        this.moveCount = 0;
        this.gameWon = false;
        this.scanner = new java.util.Scanner(System.in);
    }

    /**
     * Starts the interactive maze game
     */
    public void play() {
        displayInstructions();

        boolean[][] visited = new boolean[maze.length][maze[0].length];
        visited[playerPosition.x][playerPosition.y] = true;

        while (!gameWon) {
            displayMaze(visited);

            char move = getPlayerMove();
            boolean validMove = processMove(move, visited);

            if (validMove) {
                moveCount++;

                // Check if player reached the exit
                if (playerPosition.equals(exit)) {
                    gameWon = true;
                }
            }
        }

        // Display final maze and victory message
        displayMaze(visited);
        System.out.println("\nCongratulations! You reached the exit in " + moveCount + " moves!");
        scanner.close();
    }

    /**
     * Displays game instructions
     */
    private void displayInstructions() {
        System.out.println("=== MAZE GAME ===");
        System.out.println("Find your way from the starting position (P) to the exit (E)");
        System.out.println("Controls:");
        System.out.println("  W or w: Move up");
        System.out.println("  A or a: Move left");
        System.out.println("  S or s: Move down");
        System.out.println("  D or d: Move right");
        System.out.println("  Q or q: Quit game");
        System.out.println("Legend:");
        System.out.println("  P: Player");
        System.out.println("  E: Exit");
        System.out.println("  #: Wall");
        System.out.println("  .: Visited path");
        System.out.println("  [space]: Unvisited path");
        System.out.println("\nPress Enter to start the game...");
        scanner.nextLine();
    }

    /**
     * Displays the current state of the maze
     *
     * @param visited Array tracking visited cells
     */
    private void displayMaze(boolean[][] visited) {
        System.out.println("\nMoves: " + moveCount);

        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze[0].length; j++) {
                if (i == playerPosition.x && j == playerPosition.y) {
                    System.out.print(PLAYER_CHAR + " ");
                } else if (i == exit.x && j == exit.y) {
                    System.out.print(EXIT_CHAR + " ");
                } else if (maze[i][j] == MazeGenerator.WALL) {
                    System.out.print(WALL_CHAR + " ");
                } else if (visited[i][j]) {
                    System.out.print(VISITED_CHAR + " ");
                } else {
                    System.out.print(PATH_CHAR + " ");
                }
            }
            System.out.println();
        }
    }

    /**
     * Gets the player's next move
     *
     * @return Character representing the move direction
     */
    private char getPlayerMove() {
        System.out.print("\nEnter your move (W/A/S/D or Q to quit): ");
        String input = scanner.nextLine().trim().toLowerCase();

        if (input.isEmpty()) {
            return ' ';
        }

        return input.charAt(0);
    }

    /**
     * Processes the player's move
     *
     * @param move Character representing the move direction
     * @param visited Array tracking visited cells
     * @return true if move was valid, false otherwise
     */
    private boolean processMove(char move, boolean[][] visited) {
        int newX = playerPosition.x;
        int newY = playerPosition.y;

        switch (move) {
            case 'w': // Up
                newX--;
                break;
            case 'a': // Left
                newY--;
                break;
            case 's': // Down
                newX++;
                break;
            case 'd': // Right
                newY++;
                break;
            case 'q': // Quit
                System.out.println("\nGame ended. Thanks for playing!");
                System.exit(0);
                return false;
            default:
                System.out.println("Invalid move! Use W/A/S/D to move or Q to quit.");
                return false;
        }

        // Check if the new position is valid
        if (newX < 0 || newX >= maze.length || newY < 0 || newY >= maze[0].length ||
                maze[newX][newY] == MazeGenerator.WALL) {
            System.out.println("You can't move there! That's a wall or out of bounds.");
            return false;
        }

        // Update player position and mark as visited
        playerPosition.x = newX;
        playerPosition.y = newY;
        visited[newX][newY] = true;

        return true;
    }
}

