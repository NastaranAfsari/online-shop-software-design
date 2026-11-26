package shop.model;

import java.util.UUID;

public class Category {
    private final String id;
    private String name;
    private String description;
    private boolean active = true;

    public Category(String name, String description) {
        this.id = UUID.randomUUID().toString();
        this.name = Validators.requireNonBlank(name, "Category name");
        this.description = description == null ? "" : description.trim();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        this.name = Validators.requireNonBlank(newName, "Category name");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
