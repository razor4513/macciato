/**
 * 
 */
package de.telekom.cldii.data.contact;

/**
 * @author chuebner
 *
 */
public class PhoneNamePair implements Comparable<PhoneNamePair> {
    
    private String phone;
    private String name;
    private Contact.PhoneType type;
    
    public PhoneNamePair(String phone, String name, Contact.PhoneType type) {
        this.phone = phone;
        this.name = name;
        this.setType(type);
    }
    
    
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Contact.PhoneType getType() {
        return type;
    }

    public void setType(Contact.PhoneType type) {
        this.type = type;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PhoneNamePair)) {
            return false;
        }
        
        PhoneNamePair other = (PhoneNamePair)o;
        return (this.phone.equals(other.phone) && this.name.equals(other.name));
    }
    
    @Override
    public int compareTo(PhoneNamePair another) {
        return this.name.compareTo(another.name);
    }
}
