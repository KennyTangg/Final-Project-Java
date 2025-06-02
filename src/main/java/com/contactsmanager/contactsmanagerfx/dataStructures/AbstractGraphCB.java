package com.contactsmanager.contactsmanagerfx.dataStructures;

import com.contactsmanager.contactsmanagerfx.model.Contact;

/**
 * Abstract base class for graph-based contact book implementations.
 * Provides common functionality shared between AdjacencyListGraphCB and AdjacencyMatrixGraphCB.
 *
 * This class encapsulates common graph patterns like:
 * - Directionality management
 * - Connection validation
 * - Common error messages for graph operations
 * - Shared utility methods for graph operations
 */
public abstract class AbstractGraphCB {
    
    /**
     * Indicates whether this graph is directed or undirected.
     * - true: directed graph (one-way connections)
     * - false: undirected graph (two-way connections)
     */
    protected final boolean directed;
    
    /**
     * Constructor for graph-based contact books.
     * 
     * @param directed true for directed graph, false for undirected graph
     */
    protected AbstractGraphCB(boolean directed) {
        this.directed = directed;
    }
    
    /**
     * Default constructor creates an undirected graph.
     */
    protected AbstractGraphCB() {
        this(false); // Default to undirected
    }
    
    /**
     * Validates that contact names are not null or empty for connection operations.
     * 
     * @param contact1 First contact name
     * @param contact2 Second contact name
     * @return true if both names are valid, false otherwise
     */
    protected boolean validateConnectionNames(String contact1, String contact2) {
        if (contact1 == null || contact1.trim().isEmpty()) {
            System.out.println("Contact names cannot be null or empty");
            return false;
        }
        if (contact2 == null || contact2.trim().isEmpty()) {
            System.out.println("Contact names cannot be null or empty");
            return false;
        }
        return true;
    }
    
    /**
     * Prints a standardized error message when contacts are not found for connection operations.
     * 
     * @param contact1 First contact name
     * @param contact2 Second contact name
     */
    protected void printContactsNotFoundError(String contact1, String contact2) {
        System.out.println("One or both contacts not found. Cannot perform connection operation.");
    }
    
    /**
     * Prints a standardized success message for adding a connection.
     * 
     * @param contact1 First contact name
     * @param contact2 Second contact name
     */
    protected void printConnectionAddedSuccess(String contact1, String contact2) {
        System.out.println("Connection added between " + contact1 + " and " + contact2);
    }
    
    /**
     * Prints a standardized error message when a connection already exists.
     * 
     * @param contact1 First contact name
     * @param contact2 Second contact name
     */
    protected void printConnectionExistsError(String contact1, String contact2) {
        System.out.println("Connection between " + contact1 + " and " + contact2 + " already exists.");
    }
    
    /**
     * Prints a standardized error message when trying to remove a non-existent connection.
     * 
     * @param contact1 First contact name
     * @param contact2 Second contact name
     */
    protected void printConnectionNotFoundError(String contact1, String contact2) {
        System.out.println("Connection between " + contact1 + " and " + contact2 + " does not exist.");
    }
    
    /**
     * Prints a standardized message when a contact is not found for suggestion operations.
     * 
     * @param contactName The contact name that was not found
     */
    protected void printContactNotFoundForSuggestion(String contactName) {
        System.out.println("Contact not found: " + contactName);
    }
    
    /**
     * Prints a standardized message when no suggestions can be made.
     * 
     * @param contactName The contact name for which suggestions were requested
     */
    protected void printNoSuggestionsAvailable(String contactName) {
        System.out.println("Unable to suggest contacts for " + contactName + " - no connections found.");
    }
    
    /**
     * Returns whether this graph is directed.
     * 
     * @return true if directed, false if undirected
     */
    public boolean isDirected() {
        return directed;
    }
    
    /**
     * Prints information about the graph type.
     * Subclasses can override this to provide more specific information.
     */
    public void printGraphInfo() {
        System.out.println("Graph Type: " + (directed ? "Directed" : "Undirected"));
    }
    
    /**
     * Abstract method for checking if a connection exists between two contacts.
     * Each graph implementation will define this based on their internal structure.
     * 
     * @param contact1 First contact name
     * @param contact2 Second contact name
     * @return true if connection exists, false otherwise
     */
    protected abstract boolean connectionExists(String contact1, String contact2);
    
    /**
     * Abstract method for getting the actual Contact objects by name.
     * This is needed for connection operations that work with Contact objects.
     * 
     * @param contact1Name First contact name
     * @param contact2Name Second contact name
     * @return array with [contact1, contact2] or null if either not found
     */
    protected abstract Contact[] getContactPair(String contact1Name, String contact2Name);
}
