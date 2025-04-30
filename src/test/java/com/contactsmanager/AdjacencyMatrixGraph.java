import java.util.LinkedList;

public class AdjacencyMatrixGraph {
    int rows = 6;
    int columns = 6;
    int[][] matrix = new int[rows][columns];
    boolean directed;

    // ADD CONNECTION
    void addEdge(int a, int b) {
        matrix[a][b] = 1;
        if (!directed) {
            matrix[b][a] = 1;
        }
    }

    // TRAVERSAL DEPTH FIRST SEARCH
    /* There could be multiple paths from the start cell to the destination cell*/
    boolean dfsFindPath(int[][] matrix, int sx, int sy, int dx, int dy, int passable) {
        boolean[][] visited = new boolean[matrix.length][matrix[0].length];
        dfs(matrix, sx, sy, visited, passable); // Call dfs()
        if (!visited[dx][dy]) {
            return false; // Cannot find path
        }
        return true;
    } // Time O(m*n), Space(m*n) used for visited

    void dfs(int[][] matrix, int i, int j, boolean[][] visited, int passable) {
        if (i < 0 || i >= matrix.length || j < 0 || j >= matrix[0].length || matrix[i][j] != passable || visited[i][j]) {
            return; // Base case
        }
        visited[i][j] = true;
        dfs(matrix, i-1, j, visited, passable); // Move left
        dfs(matrix, i+1, j, visited, passable); // Move right
        dfs(matrix, i, j-1, visited, passable); // Move up
        dfs(matrix, i, j+1, visited, passable); // Move down
    } // Time O(m*n), Space O(d), d is depth of recursion

    // TRAVERSAL BREADTH FIRST SEARCH
    class Cell { // Subclass for breadth first search
        int x, y;
        int dist; // Distance from start
        Cell prev; // Previous cell visited

        Cell(int x, int y, int dist, Cell prev) {
            this.x = x;
            this.y = y;
            this.dist = dist;
            this.prev = prev;
        }
    }

    void bfsShortestPath(int[][] matrix, int sx, int sy, int dx, int dy, int passable) {
        int m = matrix.length;
        int n = matrix[0].length;
        Cell[][] cells = new Cell[m][n];
        for (int i = 0; i < m; i++) { // Initialize cells
            for (int j = 0; j < n; j++) {
                cells[i][j] = new Cell(i, j, Integer.MAX_VALUE, null);
            }
        }
        bfs(cells, sx, sy, dx, dy); // Call bfs
    } // Time O(m*n), Space O(m*n) used for cells

    void bfs(Cell[][] cells, int sx, int sy, int dx, int dy) {
        LinkedList<Cell> queue = new LinkedList<>();
        Cell start = cells[sx][sy];
        start.dist = 0;
        queue.add(start); // Enqueue start
        Cell dest = null;
        Cell curr;
        while ((curr = queue.poll()) != null) {
            if (curr.x == dx && curr.y == dy) { // Destination reached
                dest = curr;
                break;
            }
            visit(cells, queue, curr.x - 1, curr.y, curr); // Move left
            visit(cells, queue, curr.x + 1, curr.y, curr); // Move right
            visit(cells, queue, curr.x, curr.y - 1, curr); // Move up
            visit(cells, queue, curr.x, curr.y + 1, curr); // Move down
        }
        if (dest == null) {
            System.out.println("There is no path.");
            return;
        } else { // Compose path if it exists
            LinkedList<Cell> path = new LinkedList<>();
            curr = dest;
            do {
                path.addFirst(curr);
            } while ((curr = curr.prev) != null);
            System.out.println(path);
        }
    } // Time O(m*n), Space O(q), q is queue length

    // HELPER FUNC for BFS
    void visit(Cell[][] cells, LinkedList<Cell> queue, int x, int y, Cell prev) {
        if (x < 0 || x >= cells.length || y < 0 || y >= cells[0].length || cells[x][y] == null) {
            return; // Cell is our of bounds or impassable
        }
        int dist = prev.dist + 1;
        Cell cell = cells[x][y];
        if (dist < cell.dist) { // Update info in cell
            cell.dist = dist;
            cell.prev = prev;
            queue.add(cell); // Enqueue cell
        }
    } // Time O(1), Space O(1)

    /*Both DFS and BFS for a matrix have a time and space complexity of O(m*n).*/
}
