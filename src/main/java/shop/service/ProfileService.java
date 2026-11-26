package shop.service;

import shop.model.Address;
import shop.model.User;
import shop.model.Validators;
import shop.repository.UserRepository;

import java.util.List;

/** Profile editing and the address book of the logged-in user (stories 5-7). */
public class ProfileService {
    private final UserRepository userRepository;
    private final UserSession session;
    private final UniqueContactPolicy uniqueContactPolicy;

    public ProfileService(UserRepository userRepository, UserSession session) {
        this.userRepository = userRepository;
        this.session = session;
        this.uniqueContactPolicy = new UniqueContactPolicy(userRepository);
    }

    // ---------- profile ----------

    /** Blank arguments keep the current value. */
    public User updateProfile(String newFullName, String newEmail, String newPhone) {
        User user = currentUser();
        String name = Validators.isBlank(newFullName) ? user.getFullName() : newFullName;
        String email = Validators.isBlank(newEmail) ? user.getEmail() : newEmail;
        String phone = Validators.isBlank(newPhone) ? user.getPhone() : newPhone;
        // Uniqueness is checked BEFORE the entity is changed, so a rejected update leaves it untouched.
        uniqueContactPolicy.requireUnique(Validators.normalizeEmail(email), Validators.normalizePhone(phone), user);
        user.updateProfile(name, email, phone);
        return userRepository.save(user);
    }

    // ---------- addresses ----------

    public void addAddress(Address address) {
        User user = currentUser();
        user.addAddress(address);
        userRepository.save(user);
    }

    public void updateAddress(String oldTitle, Address updatedAddress) {
        User user = currentUser();
        user.updateAddress(oldTitle, updatedAddress);
        userRepository.save(user);
    }

    public void removeAddress(String title) {
        User user = currentUser();
        user.removeAddress(title);
        userRepository.save(user);
    }

    public void setDefaultAddress(String title) {
        User user = currentUser();
        user.setDefaultAddress(title);
        userRepository.save(user);
    }

    public List<Address> getAddresses() {
        return currentUser().getAddresses();
    }

    private User currentUser() {
        return Authorization.requireLoggedIn(session.getCurrentUser());
    }
}
