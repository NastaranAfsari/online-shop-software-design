package shop.cli;

import shop.model.Address;
import shop.model.User;
import shop.service.ProfileService;
import shop.service.UserSession;

import java.util.List;

/** Profile viewing/editing and address management (stories 5-7). */
public class ProfileMenu {
    private final ConsoleIO io;
    private final ConsolePrinter printer;
    private final ProfileService profileService;
    private final UserSession session;

    public ProfileMenu(ConsoleIO io, ConsolePrinter printer, ProfileService profileService, UserSession session) {
        this.io = io;
        this.printer = printer;
        this.profileService = profileService;
        this.session = session;
    }

    public void viewProfile() {
        printer.printProfile(session.getCurrentUser());
    }

    public void manage() {
        io.println("\n=== Edit Profile & Manage Addresses ===");
        io.println("1. Edit personal info");
        io.println("2. Add an address");
        io.println("3. Edit an address");
        io.println("4. Delete an address");
        io.println("5. Set default address");
        io.println("0. Back");
        switch (io.readLine("Select an option: ")) {
            case "1": editPersonalInfo(); break;
            case "2": addAddress(); break;
            case "3": editAddress(); break;
            case "4": deleteAddress(); break;
            case "5": setDefaultAddress(); break;
            case "0": break;
            default: io.println("Invalid option.");
        }
    }

    private void editPersonalInfo() {
        User user = session.getCurrentUser();
        io.println("(Leave empty to keep the current value)");
        String name = io.readLine("New full name [" + user.getFullName() + "]: ");
        String email = io.readLine("New email [" + (user.getEmail() == null ? "" : user.getEmail()) + "]: ");
        String phone = io.readLine("New phone [" + (user.getPhone() == null ? "" : user.getPhone()) + "]: ");
        profileService.updateProfile(name, email, phone);
        io.println("Profile updated successfully!");
    }

    public void addAddress() {
        io.println("\n=== Add New Address ===");
        profileService.addAddress(readAddress(null));
        io.println("Address added successfully!");
    }

    private void editAddress() {
        Address old = chooseAddress("Select the address to edit");
        if (old == null) {
            return;
        }
        io.println("Enter the new details (all fields required).");
        profileService.updateAddress(old.getTitle(), readAddress(old.getTitle()));
        io.println("Address updated successfully.");
    }

    private void deleteAddress() {
        Address address = chooseAddress("Select the address to delete");
        if (address != null) {
            profileService.removeAddress(address.getTitle());
            io.println("Address removed successfully.");
        }
    }

    private void setDefaultAddress() {
        Address address = chooseAddress("Select the new default address");
        if (address != null) {
            profileService.setDefaultAddress(address.getTitle());
            io.println("Default address updated.");
        }
    }

    private Address chooseAddress(String prompt) {
        List<Address> addresses = profileService.getAddresses();
        if (addresses.isEmpty()) {
            io.println("You have no addresses.");
            return null;
        }
        printer.printAddresses(addresses);
        int index = io.chooseIndex(prompt, addresses.size());
        return index < 0 ? null : addresses.get(index);
    }

    private Address readAddress(String currentTitle) {
        String title = io.readLine("Address title (e.g. Home, Work)" + (currentTitle == null ? "" : " [" + currentTitle + "]") + ": ");
        String recipient = io.readLine("Recipient name: ");
        String phone = io.readLine("Phone number (09xxxxxxxxx): ");
        String country = io.readLine("Country: ");
        String city = io.readLine("City: ");
        String street = io.readLine("Street / full address: ");
        String postalCode = io.readLine("Postal code (10 digits): ");
        boolean isDefault = io.confirm("Make this your default address?");
        return new Address(title, recipient, phone, country, city, street, postalCode, isDefault);
    }
}
