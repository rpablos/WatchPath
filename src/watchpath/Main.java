//  Author: Ronald Pablos
//  Year: 2018

package watchpath;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Ronald Pablos
 */
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

        @Override
        public void onFileCreation(Path file) {
            System.out.println("{"+wp.getPath()+"}--> Creation of "+file);
        }

        @Override
        public void onFileCreation(Path dir, Path file) {
            System.out.println("{"+wp.getPath()+"}--> Creation of "+file+" inside the directory "+dir);
        }

        @Override
        public void onFileDeletion(Path file) {
            System.out.println("{"+wp.getPath()+"}--> Deletion of "+file);
        }

        @Override
        public void onFileDeletion(Path dir, Path file) {
           System.out.println("{"+wp.getPath()+"}--> Deletion of "+file+" inside the directory "+dir);
        }

        @Override
        public void onFileModification(Path dir, Path file) {
            System.out.println("{"+wp.getPath()+"}--> Modification of "+file+" inside the directory "+dir);
        }

        @Override
        public void onFileModification(Path file) {
            System.out.println("{"+wp.getPath()+"}--> Modification of "+file);
        }
        
        
    }
}
