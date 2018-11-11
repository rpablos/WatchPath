//  Author: Ronald Pablos
//  Year: 2018

package watchpath;

import java.nio.file.Path;

/**
 * Listener interface for receiving events related to a {@link java.nio.file.Path}
 * 
 * Watched path can be either a file or a directory. 
 * A directory is just a special file and can receive notifications for the 
 * different events. For example, a deletion of a directory will invoke a
 * {@link #onFileDeletion(java.nio.file.Path) }
 * @author Ronald Pablos
 */
public interface WatchPathListener {

    /**
     * Notification of file creation matching the watched path.
     * @param file
     */
    public void onFileCreation(Path file);

    /**
     * Notification of file modification matching the watched path.
     * @param file
     */
    public void onFileModification(Path file);

    /**
     * Notification of file deletion matching the watched path.
     * @param file
     */
    public void onFileDeletion(Path file);

    /**
     * Notification of file creation inside the directory matching the watched path.
     * @param dir directory matching the watched path
     * @param file the file created
     */
    public void onFileCreation(Path dir, Path file);

    /**
     * Notification of file modification inside the directory matching the watched path.
     * @param dir directory matching the watched path
     * @param file the file modified
     */
    public void onFileModification(Path dir, Path file);

    /**
     * Notification of file deletion inside the directory matching the watched path.
     * @param dir directory matching the watched path
     * @param file the file deleted
     */
    public void onFileDeletion(Path dir, Path file);
}
