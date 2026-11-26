package shop.model;

import java.util.Objects;

/** Value Object: immutable; "changing" an address means creating a new one. */
public final class Address {
    private final String title;
    private final String recipientName;
    private final String phoneNumber;
    private final String country;
    private final String city;
    private final String street;
    private final String postalCode;
    private final boolean defaultAddress;

    public Address(String title, String recipientName, String phoneNumber, String country,
                   String city, String street, String postalCode, boolean isDefault) {
        this.title = Validators.requireNonBlank(title, "Address title");
        this.recipientName = Validators.requireNonBlank(recipientName, "Recipient name");
        if (!Validators.isValidPhone(phoneNumber)) {
            throw new IllegalArgumentException("Invalid phone number. Must start with 09 and contain 11 digits.");
        }
        this.phoneNumber = phoneNumber.trim();
        this.country = Validators.requireNonBlank(country, "Country");
        this.city = Validators.requireNonBlank(city, "City");
        this.street = Validators.requireNonBlank(street, "Street address");
        if (!Validators.isValidPostalCode(postalCode)) {
            throw new IllegalArgumentException("Postal code must be exactly 10 digits.");
        }
        this.postalCode = postalCode.trim();
        this.defaultAddress = isDefault;
    }

    public String getTitle() {
        return title;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public boolean isDefault() {
        return defaultAddress;
    }

    /** Returns a copy of this address with a different default flag. */
    public Address withDefault(boolean isDefault) {
        return new Address(title, recipientName, phoneNumber, country, city, street, postalCode, isDefault);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Address other = (Address) o;
        return defaultAddress == other.defaultAddress && title.equals(other.title)
                && recipientName.equals(other.recipientName) && phoneNumber.equals(other.phoneNumber)
                && country.equals(other.country) && city.equals(other.city)
                && street.equals(other.street) && postalCode.equals(other.postalCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, recipientName, phoneNumber, country, city, street, postalCode, defaultAddress);
    }

    @Override
    public String toString() {
        return title + " [" + (defaultAddress ? "Default" : "Secondary") + "] - Recipient: " + recipientName
                + " (" + phoneNumber + ") - " + country + ", " + city + ", " + street
                + " (Postal Code: " + postalCode + ")";
    }
}
