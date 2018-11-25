//  Author: Ronald Pablos
//  Year: 2018

package watchpath;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Object for creating {@link WatchPath} objects.
 * 
 * @author Ronald Pablos
 */
public class WatchPathManager {
    Map<FileSystem,WatchService> FS2WatchService;
    Map<WatchKey,List<WatchPath_impl>> WK2WatchPathList;
    Thread watchServiceTask;
    Set<WatchPath_impl> nonExistingPaths;
    
    /**
     * Creates a new manager for creating {@link WatchPath} objects.
     * Polling time for non-existing paths defaults to 1 seg.
     */
    public WatchPathManager() {
        this(1000);
    }

    /**
     * Creates a new manager for creating {@link WatchPath} objects
     * @param pollingTime polling time in ms for checking non-existing paths
     */
    public WatchPathManager(int pollingTime) {
        this.FS2WatchService = new HashMap<>();
        this.WK2WatchPathList = new HashMap<>();
        this.watchServiceTask = new WatchServiceTask(pollingTime);
        this.nonExistingPaths = new HashSet<>();
    }
    /**
     * Starts the all the background machinery necessary for the 
     * notification of events associated to watched paths
     */
    public void start() {
         watchServiceTask.start();
    }
    /**
     * Creates a new {@link WathPath} object.
     * The path passed as paramteter can or cannot exist. 
     * In case the path does not exist, it will be checked periodically. 
     * The period for this is specified in the constructor of {@link WatchPathManager#WatchPathManager(int) }. 
     * By default, the period is 1 second.
     * 
     * @param path the path to be watched
     * @return the created {@link WatchPath} object
     * @throws IOException
     */
    public WatchPath createWatchPath(Path path) throws IOException{
        path = path.toAbsolutePath().normalize();
        WatchPath_impl wpi = registerPathandParent(path);
        if (wpi == null) {
            wpi = new WatchPath_impl(path);
            nonExistingPaths.add(wpi);
        }
        return wpi;
    }
    private WatchPath_impl registerPathandParent(Path pathtoregister) throws IOException {
        FileSystem fileSystem = pathtoregister.getFileSystem();
        WatchService watchService = getWatchService(fileSystem);
        WatchPath_impl wpi = null;
        if (pathtoregister.toFile().exists() && pathtoregister.toFile().isDirectory()) {
            WatchKey key = pathtoregister.register(watchService,StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_DELETE,StandardWatchEventKinds.ENTRY_MODIFY );
            List<WatchPath_impl> list = getWatchPath_impl_List(key);
            wpi = new WatchPath_impl(pathtoregister, key);
            list.add(wpi);
        }
        Path parentpathtoregister = pathtoregister.getParent();
        fileSystem = parentpathtoregister.getFileSystem();
        watchService = getWatchService(fileSystem);
        if (parentpathtoregister.toFile().exists() && parentpathtoregister.toFile().isDirectory()) {
            WatchKey parentkey = parentpathtoregister.register(watchService,StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_DELETE,StandardWatchEventKinds.ENTRY_MODIFY );
            List<WatchPath_impl> list = getWatchPath_impl_List(parentkey);
            if (wpi == null) 
                wpi = new WatchPath_impl(pathtoregister,null, parentkey);
            else
                wpi.parentkey = parentkey;
            list.add(wpi);
        }
        return wpi;
    }
            
    private List<WatchPath_impl> getWatchPath_impl_List(WatchKey key) {
        List<WatchPath_impl> list = WK2WatchPathList.get(key);
        if (list == null) {
            list = new LinkedList<>();
            WK2WatchPathList.put(key, list);
        }
        return list;
    }
    private WatchService getWatchService(FileSystem fileSystem) throws IOException {
        WatchService watchService = FS2WatchService.get(fileSystem);
        if (watchService == null) {
            watchService = fileSystem.newWatchService();
            FS2WatchService.put(fileSystem, watchService);
        }
        return watchService;
    }
    private class WatchPath_impl implements WatchPath {
        Path path;
        WatchKey key,parentkey = null;
        Set<WatchPathListener> listeners;
        public WatchPath_impl(Path path) {
            this(path,null);
        }
        public WatchPath_impl(Path path,WatchKey key) {
            this(path,key,null);
        }
        private WatchPath_impl(Path path,WatchKey key, WatchKey parentkey) {
            this.path = path;
            this.key = key;
            this.parentkey = parentkey;
            this.listeners = new HashSet<>();
        }

        @Override
        public Path getPath() {
            return path;
        }
        @Override
        public void close() {
            
            if (!nonExistingPaths.remove(this)) {
                removeWatchPathimplFromWK(key);
                removeWatchPathimplFromWK(parentkey);
            }
        }
        
        private void removeWatchPathimplFromWK(WatchKey key) {
            List<WatchPath_impl> list = WK2WatchPathList.get(key);
            list.remove(this);
            if (list.isEmpty()) {
                key.cancel();
                WK2WatchPathList.remove(key);
            }
        }
        
    

        private void notifyListeners(WatchEvent.Kind kind,Path context, Path watchable) {
            for (WatchPathListener listener: listeners) {
                
                if (watchable.equals(path)) {
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE)
                            listener.onFileCreation(path,context);
                    else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
                        listener.onFileModification(path,context);
                    else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
                        listener.onFileDeletion(path,context);
                }else {
                    Path createdpath = watchable.resolve(context);
                    if (createdpath.equals(path)) {
                        if ((kind == StandardWatchEventKinds.ENTRY_CREATE) && createdpath.toFile().isDirectory())  {
                            try {
                                WatchKey newkey = path.register(getWatchService(path.getFileSystem()),StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_DELETE,StandardWatchEventKinds.ENTRY_MODIFY );
                                if (newkey != key) {
                                    List<WatchPath_impl> list = getWatchPath_impl_List(newkey);

                                    key = newkey;
                                    list.add(this);
                                }
                            } catch (IOException e) {}
                        }
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE)
                            listener.onFileCreation(createdpath);
                        else if (kind == StandardWatchEventKinds.ENTRY_MODIFY)
                            listener.onFileModification(createdpath);
                        else if (kind == StandardWatchEventKinds.ENTRY_DELETE)
                            listener.onFileDeletion(createdpath);
                    }
                }
                
            }
        }

        @Override
        public void addWatchPathListener(WatchPathListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removeWatchPathListener(WatchPathListener listener) {
            listeners.remove(listener);
        }
    }
    
    volatile boolean fin = false;
    ExecutorService es = Executors.newSingleThreadExecutor();
    private class WatchServiceTask extends Thread {
        private WatchKey key;
        private  int polling_Time;

        private WatchServiceTask(int pollingTime) {
            this.polling_Time = pollingTime;
        }
        @Override
        public void run() {
           while (!fin) {
               try {
                    for (WatchService ws: FS2WatchService.values()) {
                        while ((key = ws.poll()) != null) {
                            if (key.isValid()){
                                for (final WatchEvent event : key.pollEvents()) {
                                    final WatchEvent.Kind<?> kind = event.kind();
                                    if (kind == StandardWatchEventKinds.OVERFLOW)
                                        continue;

                                    List<WatchPath_impl> list = WK2WatchPathList.get(key);
                                    for (final WatchPath_impl wpi: list) {
                                        final Path watchable = (Path)key.watchable();
                                         es.execute(new Runnable() {
                                             @Override
                                             public void run() {
                                                 wpi.notifyListeners(kind,(Path)event.context(),watchable);
                                             }
                                         });
                                    }
                                }


                            }
                            if (!key.reset()) {
                                List<WatchPath_impl> list = WK2WatchPathList.get(key);
                                for (WatchPath_impl wpi: list) {
                                    if (key == wpi.key)
                                        wpi.key = null;
                                    else if ((key == wpi.parentkey)) {
                                         if (wpi.key == null)
                                            nonExistingPaths.add(wpi);
                                         wpi.parentkey = null;
                                    }
                                }
                                WK2WatchPathList.remove(key);
                            }
                        }
                    }
                    Thread.sleep(polling_Time);
                    pollNonExistingFiles();
               } catch (Exception ex) { 
                   Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
               }
           }
        }
        
        
        private void pollNonExistingFiles() throws IOException {
            for (Iterator<WatchPath_impl> it = nonExistingPaths.iterator(); it.hasNext();) {
                WatchPath_impl wpi = it.next();
                try {
                    Path path = wpi.getPath();

                    WatchPath_impl rwpi = registerPathandParent(path);
                    if (rwpi != null) {
                        it.remove();
                        rwpi.listeners = wpi.listeners;
                    }
                } catch (IOException e) {
                    throw e;
                }
            }
        }
        
    } //class WatchServiceTask

    /**
     * Finnishes this WatchPath manager so that no longer events are notified.
     * Also, all the machinery created for event notification is closed
     */
    public void finnish() {
        fin = true;
        es.shutdown();
    }
}
