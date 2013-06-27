package de.telekom.cldii.controller.mail;

import java.util.List;

import de.telekom.cldii.data.mail.ICompactMail;

/**
 * Provides mail functionality.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface IMailController {

	/**
	 * Check mails and send pending messages for all accounts
	 */
	public void checkMails();
	
	/**
	 * Sends a mail with the given subject and text to the recievers with the default account.
	 * 
	 * @param addressTo reciever
	 * @param addressesCC CC recievers
	 * @param addressesBCC BCC recievers
	 * @param subject subject of the mail
	 * @param text text of the mail
	 */
	public void sendMail(String addressTo, List<String> addressesCC, List<String> addressesBCC, String subject, String text);
	
	/**
	 * Sends a mail with the given subject and text to the recievers with the same account as the received mail.
	 * 
	 * @param addressTo reciever
	 * @param addressesCC CC recievers
	 * @param addressesBCC BCC recievers
	 * @param subject subject of the mail
	 * @param text text of the mail
	 * @param useAccountFromMailId id of a mail to determine the account to send the mail from
	 */
	public void sendMail(String addressTo, List<String> addressesCC, List<String> addressesBCC, String subject, String text, String useAccountFromMailId);
	
	/**
	 * Deletes the given mail.
	 * 
	 * @param mailToDelete mail to delete
	 */
	public void deleteMail(ICompactMail mailToDelete);
	
	/**
	 * Marks the given mail as read or unread.
	 * 
	 * @param mail mail to mark
	 * @param isRead true if the mail should be marked as read
	 */
	public void changeReadState(ICompactMail mail, boolean isRead);
	
	/**
	 * Marks the given mail as answered or not answered
	 * 
	 * @param mail mail to mark
	 * @param isAnswered true if the mail should be marked as answered
	 */
	public void changeAnsweredState(ICompactMail mail, boolean isAnswered);
}
