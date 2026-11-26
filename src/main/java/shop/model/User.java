package shop.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Entity: an account (customer or admin). It owns its addresses and its cart. */
public class User {
    private final String id;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private final Role role;
    private final List<Address> addresses = new ArrayList<>();
    private final Cart cart;

    /** Email or phone may be null, but not both (story 1). Only a password HASH is accepted. */
    public User(String fullName, String email, String phone, String passwordHash, Role role) {
        this.id = UUID.randomUUID().toString();
        if (role == null) {
            throw new IllegalArgumentException("Role is required.");
        }
        this.role = role;
        this.passwordHash = Validators.requireNonBlank(passwordHash, "Password");
        applyProfile(fullName, email, phone);
        this.cart = new Cart(id);
    }

    // ---------- profile ----------

    public void updateProfile(String newFullName, String newEmail, String newPhone) {
        applyProfile(newFullName, newEmail, newPhone);
    }

    private void applyProfile(String newFullName, String newEmail, String newPhone) {
        String validName = Validators.requireNonBlank(newFullName, "Full name");
        String validEmail = Validators.normalizeEmail(newEmail);
        String validPhone = Validators.normalizePhone(newPhone);
        if (validEmail == null && validPhone == null) {
            throw new IllegalArgumentException("An email or a phone number is required.");
        }
        if (validEmail != null && !Validators.isValidEmail(validEmail)) {
            throw new IllegalArgumentException("Invalid email format (e.g., example@domain.com).");
        }
        if (validPhone != null && !Validators.isValidPhone(validPhone)) {
            throw new IllegalArgumentException("Invalid phone number. Must start with 09 and contain 11 digits.");
        }
        this.fullName = validName;
        this.email = validEmail;
        this.phone = validPhone;
    }

    public void changePasswordHash(String newHash) {
        this.passwordHash = Validators.requireNonBlank(newHash, "Password");
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isCustomer() {
        return role == Role.CUSTOMER;
    }

    // ---------- addresses (exactly one default address when the list is not empty) ----------

    public void addAddress(Address address) {
        if (findAddress(address.getTitle()).isPresent()) {
            throw new IllegalArgumentException("An address with the title '" + address.getTitle() + "' already exists.");
        }
        Address toAdd = address;
        if (addresses.isEmpty()) {
            toAdd = address.withDefault(true);
        } else if (address.isDefault()) {
            clearDefaultFlag();
        }
        addresses.add(toAdd);
    }

    public void updateAddress(String oldTitle, Address newData) {
        int index = indexOfAddress(oldTitle);
        if (index < 0) {
            throw new IllegalArgumentException("Address with title '" + oldTitle + "' not found.");
        }
        boolean titleTaken = findAddress(newData.getTitle()).isPresent()
                && !addresses.get(index).getTitle().equalsIgnoreCase(newData.getTitle());
        if (titleTaken) {
            throw new IllegalArgumentException("An address with the title '" + newData.getTitle() + "' already exists.");
        }
        boolean wasDefault = addresses.get(index).isDefault();
        Address replacement = newData;
        if (newData.isDefault()) {
            clearDefaultFlag();
        } else if (wasDefault) {
            replacement = newData.withDefault(true);
        }
        addresses.set(index, replacement);
    }

    public void removeAddress(String title) {
        int index = indexOfAddress(title);
        if (index < 0) {
            throw new IllegalArgumentException("Address with title '" + title + "' not found.");
        }
        boolean wasDefault = addresses.remove(index).isDefault();
        if (wasDefault && !addresses.isEmpty()) {
            addresses.set(0, addresses.get(0).withDefault(true));
        }
    }

    public void setDefaultAddress(String title) {
        int index = indexOfAddress(title);
        if (index < 0) {
            throw new IllegalArgumentException("Address with title '" + title + "' not found.");
        }
        clearDefaultFlag();
        addresses.set(index, addresses.get(index).withDefault(true));
    }

    public Optional<Address> findAddress(String title) {
        int index = indexOfAddress(title);
        return index < 0 ? Optional.empty() : Optional.of(addresses.get(index));
    }

    public Optional<Address> getDefaultAddress() {
        return addresses.stream().filter(Address::isDefault).findFirst();
    }

    private int indexOfAddress(String title) {
        if (title == null) {
            return -1;
        }
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).getTitle().equalsIgnoreCase(title.trim())) {
                return i;
            }
        }
        return -1;
    }

    private void clearDefaultFlag() {
        for (int i = 0; i < addresses.size(); i++) {
            if (addresses.get(i).isDefault()) {
                addresses.set(i, addresses.get(i).withDefault(false));
            }
        }
    }

    // ---------- getters ----------

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    /** May be null if the user registered with a phone number only. */
    public String getEmail() {
        return email;
    }

    /** May be null if the user registered with an email only. */
    public String getPhone() {
        return phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    public Cart getCart() {
        return cart;
    }
}
