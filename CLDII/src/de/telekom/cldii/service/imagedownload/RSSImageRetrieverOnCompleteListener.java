package de.telekom.cldii.service.imagedownload;

/**
 * Interface that defines the callback method when completing a image retrieving
 * asynchronous task
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public interface RSSImageRetrieverOnCompleteListener<T> {

    /**
     * Callback method to increase the {@link NewsImageDownloaderService} progress notification 
     */
    public void onRssImageRetrieverProgress();
    
    /**
     * Callback method that should be triggered after the RSSImageRetriever
     * asynchronous task finishes
     * 
     * @param result
     *            the result of the task, usually the task itself
     */
    public void onRssImageRetrieverExecutionComplete(T result);

}
