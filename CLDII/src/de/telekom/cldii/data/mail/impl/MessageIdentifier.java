package de.telekom.cldii.data.mail.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import android.content.Context;
import android.util.Log;

import com.fsck.k9.Account;
import com.fsck.k9.Preferences;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mail.MessagingException;

import de.telekom.cldii.util.Base64Coder;

/**
 * Wrapper for a unique id of a mail.
 * We have to use java object serialization for the components because
 * content of the components is not documented in K9 so we couldn't use a delimiter like semicolon.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class MessageIdentifier implements Serializable{

	/**
	 * serial id
	 */
	private static final long serialVersionUID = -5640928243360550922L;
	
	private String messageId;
	private String folderName;
	private String accountId;
	
	private MessageIdentifier(String messageId, String folderName, String accountId) {
		this.messageId = messageId;
		this.folderName = folderName;
		this.accountId = accountId;
	}
	
	/**
	 * Factory for a unique message id
	 */
	public static String getMessageId(Message message) {
		MessageIdentifier messageIdentifier = new MessageIdentifier(message.getUid(), message.getFolder().getName(), message.getFolder().getAccount().getUuid());
		String messageId = null;
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(messageIdentifier);
			oos.flush();
			oos.close();
			messageId = new String(Base64Coder.encode(baos.toByteArray()));
			baos.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return messageId;
	}
	
	/**
	 * Conversion from a unique message id to message identifier
	 */
	public static MessageIdentifier getMessageId(String messageId) {
		MessageIdentifier messageIdentifier = null;
		try {
			byte [] data = Base64Coder.decode(messageId);
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(bais);
			messageIdentifier = (MessageIdentifier) ois.readObject();
			ois.close();
			bais.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return messageIdentifier;
	}
	
	/**
	 * Helper Method DONT USE THIS OUTSIDE of mail.impl package!
	 */
	public static Message getMessage(String messageId, Context context) {
		MessageIdentifier messageIdentifier = getMessageId(messageId);
		return getMessage(messageIdentifier, context);
	}
	
	/**
	 * Helper Method DONT USE THIS OUTSIDE of mail.impl package!
	 */
	public static Message getMessage(MessageIdentifier messageIdentifier, Context context) {
		try {
			if(messageIdentifier != null) {
				Account account = Preferences.getPreferences(context).getAccount(messageIdentifier.getAccountId());
				if(account != null) {
					Folder folder = account.getLocalStore().getFolder(messageIdentifier.getFolderName());
					if(folder != null) {
						Message message = folder.getMessage(messageIdentifier.getMessageId());
						return message;
					}
				}
			}
		} catch (MessagingException e) {
			Log.e("MessageIdentifier", "Failed to get message", e);
		}
		return null;
	}

	/**
	 * @return the messageId
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * @return the folderName
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * @return the accountId
	 */
	public String getAccountId() {
		return accountId;
	}
}
