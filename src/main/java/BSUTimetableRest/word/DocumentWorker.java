package BSUTimetableRest.word;


import BSUTimetableRest.entity.GroupTimetable;

import java.io.File;
import java.util.List;

public class DocumentWorker implements DocumentInterface {

    @Override
    public List<GroupTimetable> getGroupTimetables(File file) {
        return new WordController().parseWord(file);
    }
}
