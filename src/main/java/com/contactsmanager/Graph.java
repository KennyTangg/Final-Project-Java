package com.contactsmanager;

import com.contactsmanager.interfaces.Graph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Graph<T> {
    private Map<T, LinkedList<T>> adj = new HashMap<>();
    private boolean directed;

    public Graph() {
        directed = false; // Default is undirected
    }

    public Graph(boolean d) {
        directed = d; // Can be true or false
    }

    // ADD NODE
    public void addNode(T a) {
        adj.putIfAbsent(a, new LinkedList<>());
    }

    // ADD CONNECTION (and NODE if any is missing)
    public void addEdge(T a, T b) {
        adj.putIfAbsent(a, new LinkedList<>()); // Add node a
        adj.putIfAbsent(b, new LinkedList<>()); // Add node b
        adj.get(a).add(b); // Add edge a->b
        if (!directed) {
            adj.get(b).add(a); // Add edge b->a
        }
    } // Time O(1), Space O(1)

    // DELETE CONNECTION
    public boolean removeEdge(T a, T b) {
        if (!adj.containsKey(a) || !adj.containsKey(b)) {
            return false; // Invalid input
        }
        LinkedList<T> neighborsOfA = adj.get(a);
        LinkedList<T> neighborsOfB = adj.get(b);
        if (neighborsOfA == null || neighborsOfB == null) {
            return false; // No neighbors found
        }
        boolean r1 = neighborsOfA.remove(b); // Remove a->b
        if (!directed) { // Undirected graph
            boolean r2 = neighborsOfB.remove(a); // Remove b->a
            return r1 && r2;
        }
        return r1;
    } // Time O(1), Space O(1)

    // DELETE NODE
    /*To delete a node,
    you must first remove all edges connected to the node
    and then remove the node from the key set of adj. */
    public boolean removeNode(T a) {
        if (!adj.containsKey(a)) {
            return false;
        }
        if (!directed) { // Undirected graph
            LinkedList<T> neighbors = adj.get(a);
            for (T node : neighbors) {
                adj.get(node).remove(a); // Remove edges
            }
        } else {
            for (T key : adj.keySet()) {
                adj.get(key).remove(a); // Remove edges
            }
        }
        adj.remove(a); // Remove node
        return true;
    } // Time O(V+E), Space O(1),
    // V is the number of nodes,
    // E is the number of edges

    // TRAVERSAL DEPTH FIRST SEARCH
    /* The variable visited is used to ensure each node is visited only once.
    The technique is called memoization,
    caching the result in a variable to prevent redundant computations.
    It resolves the issue of overlapping in recursion.*/
    public void dfsTraversal(T start) {
        if (!adj.containsKey(start)) {
            return;
        }
        HashMap<T, Boolean> visited = new HashMap<>();
        dfs(start, visited); // Call recursive method
    }

    private void dfs(T v, HashMap<T, Boolean> visited) {
        visited.put(v, true);
        System.out.print(v.toString() + " ");
        for (T neighbor : adj.get(v)) {
            if (visited.get(neighbor) == null) { // All neighbors
                dfs(neighbor, visited); // Call itself
            }
        }
    } // Time O(V+E), Space O(V)
    // V is the number of nodes,
    // E is the number of edges

    // TRAVERSAL BREADTH FIRST SEARCH utilizing a QUEUE
    /* when searching in a large graph, BFS is preferred.
    The BFS algorithm searches the nodes close to the starting point first,
    making it ideal for finding the shortest path.*/
    public void bfsTraversal(T start) {
        if (!adj.containsKey(start)) {
            return;
        }
        Queue<T> queue = new LinkedList<>();
        queue.add(start); // Enqueue root
        HashMap<T, Boolean> visited = new HashMap<>();
        visited.put(start, true);
        while (!queue.isEmpty()) {
            T v = queue.poll(); // Dequeue
            System.out.print(v.toString() + " ");
            for (T neighbor : adj.get(v)) {
                if (visited.get(neighbor) == null) {
                    queue.add(neighbor); // Enqueue neighbors
                    visited.put(neighbor, true);
                }
            }
        }
    } // Time O(V+E), Space O(V) used for queue
    // V is the number of nodes,
    // E is the number of edges

    // PRINT CONTACT
    public void printContact() {
        for (T node : adj.keySet()) {
            System.out.print(node + ":");
            System.out.print(adj.get(node).toString());
        }
    }
}
