package BSUTimetableRest.controller;

import BSUTimetableRest.entity.GroupTimetable;
import BSUTimetableRest.word.DocumentInterface;
import BSUTimetableRest.word.DocumentWorker;
import BSUTimetableRest.word.ExcelController;
import BSUTimetableRest.repo.GroupTimetableRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Обработчик запросов для мобильного приложения",
        description = "Контроллер, отвечающий за показ расписания Института права Башкирского государственного университета")
@RestController
@EnableScheduling
@EnableTransactionManagement
public class TimetableController {

    @Autowired
    private GroupTimetableRepository groupTimetableRepository;

    @Operation(summary = "Показ всех данных о groupTimetable",
                deprecated = true)
    @GetMapping("/groupTimetables")
    public Iterable<GroupTimetable> groupTimetables () {
        return groupTimetableRepository.findAll();
    }

    @Operation(summary = "Показывает все groupNames, сохраненных в базе данных")
    @GetMapping("/groupTimetables/groupNames")
    public List<String> groupNames() {
        Iterable<GroupTimetable> groupTimetables = groupTimetableRepository.findAll();
        List<String> groupNames = new ArrayList<>();

        groupTimetables.forEach(gt -> {
            groupNames.add(gt.getGroupName());
        });
        return groupNames;
    }

    @Operation(summary = "Показывает все о конкретной группе")
    @GetMapping("/groupTimetables/groupNames/{groupName}")
    public GroupTimetable getTimetableByGroupName(
            @Parameter(description = "Название группы, расписание которой нужно показать",
                    required = true,
                    schema = @Schema(implementation = String.class)) @PathVariable String groupName) {
        return groupTimetableRepository.findByGroupName(groupName);
    }



    @Scheduled(fixedRate = 24*60*60*1000)
    public void updateExcel() {
        //код со скачкой excel
            DocumentInterface document = new DocumentWorker();
            document.getGroupTimetables(new File("a.docx")).forEach(gt -> {
                GroupTimetable groupTimetable = groupTimetableRepository.findByGroupName(gt.getGroupName());
                if (groupTimetable == null) {
                    groupTimetableRepository.save(gt);
                    System.out.println("Save new groupTimetable");
                } else {
                    groupTimetableRepository.delete(groupTimetable);
                    groupTimetableRepository.save(gt);
                    System.out.println("Replace groupTimetable: " + gt.getGroupName());
                }
            });
        System.out.println("scheduled");
        }
}
