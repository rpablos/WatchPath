# WatchPath
Library for receiving events related to file creation, modification and deletion.

You can receive events for a `Path` specified, it does not matter if it is a normal file or a directory. It does not matter even if the path does not exist. The path will be checked periodically and if it begins to exist, then it will start to notify events. Also, if a path ceases to exist, it will be checked periodically again. The watched path can even change from normal file to directory and the events are notified accordingly.

Cases:
  - Currently, the watched path is a normal file. Then you will receive the creation, modification and deletion events of this file.
  - Currently, the watched path is a directory. Then you will receive the creation, modification and deletion events of the directory itself and also the creation, modification and deletion of files inside this directory.


The usage is really easy:
  - First, create a `WatchPathManager` for creating `WatchPath` objects
  - Second, create `WatchPath` objects for the paths you want to receive event notifications
  - Third, add listeners `WathPathListener` to the created `WatchPath` objects
  - Fourth, start the manager with `WatchPathManager.start()` for start receiving events 

Example from source `Main.java`:

    public class Main {
      public static void main(String args[]) throws Exception {
        if (args.length < 1) {
            System.out.println("For usage specify parameters. Each parameter is a path to watch");
            System.exit(0);
        }
        WatchPathManager wpm = new WatchPathManager();
        for (String arg: args) {
            WatchPath wp = wpm.createWatchPath(Paths.get(arg));
            wp.addWatchPathListener(new MyWatchPathListener(wp));
            System.out.println("Watching path: "+wp.getPath());
        }
        wpm.start();
    }

    private static class MyWatchPathListener extends DefaultWatchPathListener {
        private final WatchPath wp;

        private MyWatchPathListener(WatchPath wp) {
            this.wp = wp;
        }
        public void onFileCreation(Path file) {
            System.out.println("{"+wp.getPath()+"}--> Creation of "+file);
        }
        public void onFileCreation(Path dir, Path file) {
            System.out.println("{"+wp.getPath()+"}--> Creation of "+file+" inside the directory "+dir);
        }
        public void onFileDeletion(Path file) {
            System.out.println("{"+wp.getPath()+"}--> Deletion of "+file);
        }
        public void onFileDeletion(Path dir, Path file) {
           System.out.println("{"+wp.getPath()+"}--> Deletion of "+file+" inside the directory "+dir);
        }
        public void onFileModification(Path dir, Path file) {
            System.out.println("{"+wp.getPath()+"}--> Modification of "+file+" inside the directory "+dir);
        }
        public void onFileModification(Path file) {
            System.out.println("{"+wp.getPath()+"}--> Modification of "+file);
        }
        
        
    }

