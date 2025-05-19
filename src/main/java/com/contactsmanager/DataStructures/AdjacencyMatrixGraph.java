// Unlike adjacency list, this one has a fixed limit/ capacity.

package com.contactsmanager.DataStructures;

import com.contactsmanager.interfaces.ConnectionsManager;
import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class AdjacencyMatrixGraph implements ContactsManager, ConnectionsManager {
    int size;
    int maxSize;
    byte[][] matrix; // Where the connections are stored
    Contact[] contactsBook; // Where the contact information are stored
    boolean directed;

    public AdjacencyMatrixGraph(int maxSize) {
        this.size = 0;
        this.maxSize = maxSize;
        matrix = new byte[maxSize][maxSize];
        contactsBook = new Contact[maxSize];
        directed = false;
    }

    public AdjacencyMatrixGraph(int maxSize, boolean directed) {
        this.size = 0;
        this.maxSize = maxSize;
        matrix = new byte[maxSize][maxSize];
        contactsBook = new Contact[maxSize];
        this.directed = directed;
    }

    // SEARCH A CONTACT -OK
    @Override
    public Contact searchContact(String name) {
        for (Contact contact : contactsBook) {
            if (contact != null && contact.getName().equals(name)) {
                return contact;
            }
        }
        System.out.println("Contact not found: " + name);
        return null;
    }

    // INTERNALLY SEARCH CONTACT BY INDEX
    private int searchIndexOfContact(String name) {
        int i = 0;
        for (Contact contact : contactsBook) {
            if (contact != null && contact.getName().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    // INTERNALLY SEARCH BLANK SPACE IN CONTACT LIST
    private int searchIndexOfFree() {
        if (size == maxSize) {
            System.out.println("No space left in contacts book.");
            return -1;
        }
        int i = 0;
        for (Contact contact : contactsBook) {
            if (contact == null) {
                return i;
            }
            i++;
        }
        return -1;
    }

    // ADD A NODE -OK
    @Override
    public void addContact(Contact contact) {
        int free = searchIndexOfFree();
        if (free == -1) {
            return;
        }
        contactsBook[free] = contact;
        size++;
    }

    // DELETE NODE -OK
    @Override
    public void deleteContact(String name) {
        int target = searchIndexOfContact(name);

        // Check if contact exists
        if (target == -1) {
            System.out.println("Contact not found: " + name);
            return;
        }

        for (int i = 0; i < maxSize; i++){ // Delete connections
            matrix[i][target] = 0;
        }
        for (int j = 0; j < maxSize; j++){
            matrix[target][j] = 0;
        }

        contactsBook[target] = null; // Delete contact info
        size--;
    }

    // UPDATE CONTACT -OK
    @Override
    public void updateContact(Contact contact, String newName, int newStudentId) {
        String name = contact.getName();
        int target = searchIndexOfContact(name);

        // Check if contact exists
        if (target == -1) {
            System.out.println("Contact not found: " + name);
            return;
        }

        Contact newContact = new Contact(newName, newStudentId); // Make new contact to replace old one
        contactsBook[target] = newContact;
    }

    // LIST ALL CONTACTS -OK
    @Override
    public List<Contact> listAllContacts() {
        List<Contact> result = new ArrayList<>();
        for (Contact contact : contactsBook) {
            if (contact != null) {
                result.add(contact);
            }
        }
        return result;
    }

    // ADD CONNECTION -OK
    @Override
    public void addConnection(String contact1, String contact2) {
        int yIndex = searchIndexOfContact(contact1); // Find index of both strings for the matrix
        int xIndex = searchIndexOfContact(contact2);

        // Check if both contacts exist
        if (yIndex == -1 || xIndex == -1) {
            System.out.println("One or both contacts not found. Cannot add connection.");
            return;
        }

        matrix[yIndex][xIndex] = 1;
        if (!directed) { // Undirected graph
            matrix[xIndex][yIndex] = 1;
        }
    }

    // DELETE CONNECTION -OK
    @Override
    public void removeConnection(String contact1, String contact2) {
        int yIndex = searchIndexOfContact(contact1); // Find index of both strings for the matrix
        int xIndex = searchIndexOfContact(contact2);

        // Check if both contacts exist
        if (yIndex == -1 || xIndex == -1) {
            System.out.println("One or both contacts not found. Cannot remove connection.");
            return;
        }

        matrix[yIndex][xIndex] = 0;
        if (!directed) { // Undirected graph
            matrix[xIndex][yIndex] = 0;
        }
    }

    // SUGGEST CONTACTS -OK
    @Override
    public List<Contact> suggestContacts(String contact) {
        LinkedList<Integer> directConnectionsIndex = new LinkedList<>();
        HashSet<Integer> recommendedContactsIndex = new HashSet<>();
        LinkedList<Contact> recommendedContacts = new LinkedList<>();
        int target = searchIndexOfContact(contact);

        // Check if contact exists
        if (target == -1) {
            System.out.println("Contact not found: " + contact);
            return recommendedContacts; // Return empty list
        }

        for (int i = 0; i < maxSize; i++) { // Find person's friends (by index)
            if (matrix[target][i] == 1) {
                directConnectionsIndex.add(i);
            }
        }
        if (directConnectionsIndex.isEmpty()) {
            System.out.println("Unable to suggest contacts from not knowing anyone.");
            return recommendedContacts; // Return empty list instead of null
        }
        for (int directConnection : directConnectionsIndex) { // Find friend's friends (by index)
            for (int i = 0; i < maxSize; i++) {
                if ((matrix[directConnection][i] == 1) && (i != target)) {
                    recommendedContactsIndex.add(i);
                }
            }
        }
        for (int suggested : recommendedContactsIndex) { // Change indexes to contact
            Contact suggestedContact = contactsBook[suggested];
            if (suggestedContact != null) {
                recommendedContacts.add(suggestedContact);
            }
        }

        return recommendedContacts;
    }

    // PRINT CONTACT
    public void printContact() {
        for (int i = 0; i < size; i++) { // Print person
            System.out.print(i + ") " + contactsBook[i] + " : ");
            for (int j = 0; j < size; j++) {
                if (matrix[i][j] == 1) {
                    System.out.println(j + ", ");
                }
            }
            System.out.println();
        }
    }


    // LEGACY (modify or delete)

    // TRAVERSAL DEPTH FIRST SEARCH
    /* There could be multiple paths from the start cell to the destination cell*/
    /*
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
     */

    /*Both DFS and BFS for a matrix have a time and space complexity of O(m*n).*/
}
