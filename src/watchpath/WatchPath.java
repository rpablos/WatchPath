//  Author: Ronald Pablos
//  Year: 2018

package watchpath;

import java.nio.file.Path;

/**
 * Represents an object for a watched path.
 * Over this object we can register listeners that can receive notifications
 * events about the watched path
 * @author Ronald Pablos
 */
interface WatchPath {
    /**
     * Returns the watched path.
     * @return the watched path
     */
    public Path getPath();
    /**
     * Registers listener for the watched path
     * @param listener the {@link WatchPathListener} to be registered
     */
    public void addWatchPathListener(WatchPathListener listener);
    /**
     * Unregisters listener for the watched path.
     * @param listener listener the {@link WatchPathListener} to be removed
     */
    public void removeWatchPathListener(WatchPathListener listener);
    /**
     * Close the WatchPath so that it will no longer notify any event.
     */
    public void close();
}
