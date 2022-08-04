package BSUTimetableRest.repo;

import BSUTimetableRest.entity.GroupTimetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Iterator;

public interface GroupTimetableRepository extends JpaRepository<GroupTimetable, Long> {
    GroupTimetable findByGroupName(String groupName);

}
