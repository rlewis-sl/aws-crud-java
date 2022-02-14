package com.algopop.awscrud;

public class ItemNotFoundException extends Exception {
    public ItemNotFoundException() {
        super();
    }
    
    public ItemNotFoundException(String id) {
        super("Item with id='" + id + "' not found.");
    }
}
