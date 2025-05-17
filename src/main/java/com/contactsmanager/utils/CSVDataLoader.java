package com.contactsmanager.utils;

import com.contactsmanager.interfaces.ContactsManager;
import com.contactsmanager.model.Contact;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for loading contact data from CSV files.
 */
public class CSVDataLoader {
    
    /**
     * Loads contacts from a CSV file and adds them to the specified ContactsManager.
     * 
     * @param contactsManager The ContactsManager to add contacts to
     * @param filePath The path to the CSV file containing contact data
     * @return A list of the loaded contacts
     * @throws IOException If an I/O error occurs
     */
    public static List<Contact> loadContacts(ContactsManager contactsManager, String filePath) throws IOException {
        List<Contact> contacts = new ArrayList<>();
        Map<String, Contact> nameToContactMap = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header line
            String line = reader.readLine();
            
            // Read data lines
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1].trim();
                    
                    Contact contact = new Contact(name, id);
                    contacts.add(contact);
                    nameToContactMap.put(name, contact);
                    
                    // Add contact to the manager
                    contactsManager.addContact(contact);
                }
            }
        }
        
        System.out.println("Loaded " + contacts.size() + " contacts from " + filePath);
        return contacts;
    }
    
    /**
     * Loads connections between contacts from a CSV file and adds them to the specified ContactsManager.
     * 
     * @param contactsManager The ContactsManager to add connections to
     * @param filePath The path to the CSV file containing connection data
     * @throws IOException If an I/O error occurs
     */
    public static void loadConnections(ContactsManager contactsManager, String filePath) throws IOException {
        int connectionCount = 0;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header line
            String line = reader.readLine();
            
            // Read data lines
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name1 = parts[1].trim();
                    String name2 = parts[2].trim();
                    
                    // Add connection to the manager
                    try {
                        contactsManager.addConnection(name1, name2);
                        connectionCount++;
                    } catch (Exception e) {
                        System.err.println("Error adding connection between " + name1 + " and " + name2 + ": " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("Loaded " + connectionCount + " connections from " + filePath);
    }
    
    /**
     * Checks if a CSV file exists at the specified path.
     * 
     * @param filePath The path to check
     * @return true if the file exists, false otherwise
     */
    public static boolean fileExists(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }
    
    /**
     * Gets the available CSV files in the specified directory.
     * 
     * @param directoryPath The directory to search
     * @return A list of CSV file paths
     */
    public static List<String> getAvailableCSVFiles(String directoryPath) {
        List<String> csvFiles = new ArrayList<>();
        Path directory = Paths.get(directoryPath);
        
        if (Files.exists(directory) && Files.isDirectory(directory)) {
            try {
                Files.list(directory)
                    .filter(path -> path.toString().toLowerCase().endsWith(".csv"))
                    .forEach(path -> csvFiles.add(path.toString()));
            } catch (IOException e) {
                System.err.println("Error listing CSV files: " + e.getMessage());
            }
        }
        
        return csvFiles;
    }
}
