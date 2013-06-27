package de.telekom.cldii.data.mail.impl;

import java.util.Comparator;

import de.telekom.cldii.data.mail.ICompactMail;

public class ReceivedDateComparator implements Comparator<ICompactMail> {

    @Override
    public int compare(ICompactMail lhs, ICompactMail rhs) {
        return rhs.getRecievedDate().compareTo(lhs.getRecievedDate());
    }
}
