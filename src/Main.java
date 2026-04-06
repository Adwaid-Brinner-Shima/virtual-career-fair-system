import vcfs.model.VCFSystem;
import vcfs.timer.SystemTimer;
import vcfs.ui.AdministratorScreen;
import vcfs.ui.SystemTimerScreen;

import javax.swing.*;
import java.time.LocalDateTime;

/**
 * Entry point for the Virtual Career Fair System.
 * Initialises the VCFSystem, SystemTimer, and launches all screens.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // 1. Create the core system
            VCFSystem system = new VCFSystem();

            // 2. Configure milestone times (adjust as needed for testing)
            LocalDateTime now = LocalDateTime.now();
            system.setBookingsOpenTime(now.plusMinutes(10));
            system.setBookingsCloseTime(now.plusMinutes(20));
            system.setStartTime(now.plusMinutes(30));
            system.setEndTime(now.plusMinutes(60));

            // 3. Move system from DORMANT to PREPARING
            system.advanceState(null);

            // 4. Create the SystemTimer starting from now
            SystemTimer timer = new SystemTimer(system, now);

            // 5. Launch SystemTimer screen (Adwaid)
            SystemTimerScreen timerScreen = new SystemTimerScreen(timer, system);
            timerScreen.setVisible(true);

            // 6. Launch Administrator screen (Adwaid)
            AdministratorScreen adminScreen = new AdministratorScreen(system, timer);
            adminScreen.setVisible(true);

            // TODO: Launch RecruiterScreens x3 (Lance)
            for (int i = 1; i <= 6; i++) {
                CandidateScreen candidateScreen = new CandidateScreen(system, timer);
                                candidateScreen.setLocation(100 + (i * 30), 100 + (i * 30)); 
                                candidateScreen.setVisible(true);
            }
        });
    }
}
