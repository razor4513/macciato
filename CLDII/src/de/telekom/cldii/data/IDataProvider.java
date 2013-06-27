package de.telekom.cldii.data;

import de.telekom.cldii.config.IConfigurationManager;
import android.content.Context;

/** 
 * Basic interface for data providers defining lifecycle methods for all data providers.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface IDataProvider {

	/**
	 * Called on creation of the data provider.
	 * Initialize data provider here.
	 * 
	 * @param context
	 */
	public void onCreate(Context context, IConfigurationManager configurationManager);
	
	
	/**
	 * Called by the {@link CldApplication} instance when it receives a onLowMemory() call from the system
	 */
	public void onLowMemoryWarning();
	
	
	/**
	 * Called by the {@link CldApplication} instance when the applications resumes acitivity
	 */
	public void onResume();
}
