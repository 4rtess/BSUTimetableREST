package BSUTimetableRest.repo;

import BSUTimetableRest.entity.GroupTimetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Iterator;
import java.util.List;

public interface GroupTimetableRepository extends JpaRepository<GroupTimetable, Long> {
    GroupTimetable findByGroupName(String groupName);
    List<GroupTimetable> findAllByDirectionOfEducation(String directionOfEducation);
    List<GroupTimetable> findAllBySpeciality(String speciality);
    List<GroupTimetable> findAllByForm(String form);
}
