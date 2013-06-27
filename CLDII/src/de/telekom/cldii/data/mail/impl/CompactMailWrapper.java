package de.telekom.cldii.data.mail.impl;

import java.util.Date;

import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.Message;

import de.telekom.cldii.data.mail.ICompactMail;

/**
 * Wrapper around {@link Message} to implement ICompactMail
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class CompactMailWrapper implements ICompactMail {

	protected Message message;
	protected String messageId;
	
	public CompactMailWrapper(Message message) {
		this.message = message;
	}
	
	@Override
	public String getId() {
		if(messageId == null) {
			messageId = MessageIdentifier.getMessageId(message);
		}
		return messageId;
	}

	@Override
	public String getFromAdress() {
		if(message.getFrom().length > 0) {
			return message.getFrom()[0].getAddress();
		}
		return "";
	}

	@Override
	public Date getRecievedDate() {
		return message.getSentDate();
	}

	@Override
	public String getSubject() {
		return message.getSubject();
	}

	@Override
	public boolean answered() {
		return message.isSet(Flag.ANSWERED);
	}
	
	@Override
	public boolean read() {
		return message.isSet(Flag.SEEN);
	}
	
	public Message getMessage() {
		return message;
	}

}
