package de.telekom.cldii.service.mail;

import de.telekom.cldii.controller.IControllerManager;
import android.app.IntentService;
import android.content.Intent;

/**
 * Service for updating mails.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class MailUpdateService extends IntentService {

	public MailUpdateService() {
		super("MailUpdateService");
	}
	
	public MailUpdateService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		getControllerManager().getMailController().checkMails();
	}

	private IControllerManager getControllerManager() {
		return (IControllerManager) getApplication();
	}
}
