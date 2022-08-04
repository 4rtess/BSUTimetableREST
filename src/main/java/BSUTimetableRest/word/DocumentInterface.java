package BSUTimetableRest.word;

import BSUTimetableRest.entity.GroupTimetable;

import java.io.File;
import java.util.List;

public interface DocumentInterface {

    public List<GroupTimetable> getGroupTimetables(File file);
}
