package smartSystems.com.bloodBank.Model;

import java.io.Serializable;

public class User implements Serializable {

    private String username;
    private String address;
    private String phone;
    private String gender;
    private String bloodType;
    private String donor;

    public User(){

    }
    public User(String username, String address, String phone, String gender, String bloodType, String donor) {
        this.username = username;
        this.address = address;
        this.phone = phone;
        this.gender = gender;
        this.bloodType = bloodType;
        this.donor = donor;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public String getBloodType() {
        return bloodType;
    }

    public String isDonor() {
        return donor;
    }

}
