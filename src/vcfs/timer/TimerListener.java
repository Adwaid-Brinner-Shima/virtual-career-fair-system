package vcfs.timer;

import java.time.LocalDateTime;

/**
 * Interface implemented by any screen that needs to react to time changes.
 * The SystemTimer notifies all registered listeners when time advances.
 */
public interface TimerListener {
    void onTimeChanged(LocalDateTime newTime);
}
