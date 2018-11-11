//  Author: Ronald Pablos
//  Year: 2018

package watchpath;

import java.nio.file.Path;

/**
 * Trivial implementation of {@link watchpath.WatchPathListener} which does nothing.
 * Can be overrided on those methods that we are interested on.
 * @author Ronald Pablos
 */
public class DefaultWatchPathListener implements WatchPathListener {

    @Override
    public void onFileCreation(Path file) {}

    @Override
    public void onFileModification(Path file) {}

    @Override
    public void onFileDeletion(Path File) {}

    @Override
    public void onFileCreation(Path dir, Path file) {}

    @Override
    public void onFileModification(Path dir, Path file) {}

    @Override
    public void onFileDeletion(Path dir, Path File) {}
    
}
