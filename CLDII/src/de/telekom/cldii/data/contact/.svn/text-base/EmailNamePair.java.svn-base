package de.telekom.cldii.data.contact;

public class EmailNamePair implements Comparable<EmailNamePair> {
    private String email;
    private String name;
    
    public EmailNamePair(String email, String name) {
        this.email = email;
        this.name = name;
    }
    
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EmailNamePair)) {
            return false;
        }
        
        EmailNamePair other = (EmailNamePair)o;
        return (this.email.equals(other.email) && this.name.equals(other.name));
    }


    @Override
    public int compareTo(EmailNamePair another) {
        return this.name.compareTo(another.name);
    }
}
