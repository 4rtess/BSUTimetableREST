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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Operation(summary = "Показывает все названия групп")
    @GetMapping("/groupTimetables/groupNames")
    public List<String> groupNames() {
        Iterable<GroupTimetable> groupTimetables = groupTimetableRepository.findAll();
        List<String> groupNames = new ArrayList<>();

        groupTimetables.forEach(gt -> {
            groupNames.add(gt.getGroupName());
        });
        return groupNames;
    }

    @Operation(summary = "Показывает все о конкретной группе по названию группы")
    @GetMapping("/groupTimetables/groupNames/{groupName}")
    public GroupTimetable getTimetableByGroupName(
            @Parameter(description = "Название группы, расписание которой нужно показать",
                    required = true,
                    schema = @Schema(implementation = String.class)) @PathVariable String groupName) {
        return groupTimetableRepository.findByGroupName(groupName);
    }

    @Operation(summary = "Показывает все направления подготовки")
    @GetMapping("/groupTimetables/directionsOfEducation")
    public Set<String> directionsOfEducation() {
        Iterable<GroupTimetable> groupTimetables = groupTimetableRepository.findAll();
        Set<String> directionsOfEducation = new HashSet<>();

        groupTimetables.forEach(gt -> directionsOfEducation.add(gt.getDirectionOfEducation()));
        return directionsOfEducation;
    }

    @Operation(summary = "Показывает все группы по конкретному направлению подготовки")
    @GetMapping("/groupTimetables/directionsOfEducation/{directionOfEducation}")
    public List<GroupTimetable> getTimetablesByDirectionOfEducation(
            @Parameter(description = "Направление подготовки в несокращенном виде",
            required = true,
            schema = @Schema(implementation = String.class)) @PathVariable String directionOfEducation
    ) {
        return groupTimetableRepository.findAllByDirectionOfEducation(directionOfEducation);
    }

    @Operation(summary = "Показывает все специальности")
    @GetMapping("/groupTimetables/specialities")
    public Set<String> specialities() {
        Iterable<GroupTimetable> groupTimetables = groupTimetableRepository.findAll();
        Set<String> specialities = new HashSet<>();

        groupTimetables.forEach(gt -> specialities.add(gt.getSpeciality()));
        return specialities;
    }

    @Operation(summary = "Показывает все группы по конкретному направлению подготовки")
    @GetMapping("/groupTimetables/specialities/{speciality}")
    public List<GroupTimetable> getTimetablesBySpeciality(
            @Parameter(description = "Специальность",
                    required = true,
                    schema = @Schema(implementation = String.class)) @PathVariable String speciality
    ) {
        return groupTimetableRepository.findAllBySpeciality(speciality);
    }

    @Operation(summary = "Показывает все формы обученгия")
    @GetMapping("/groupTimetables/forms")
    public Set<String> forms() {
        Iterable<GroupTimetable> groupTimetables = groupTimetableRepository.findAll();
        Set<String> forms = new HashSet<>();

        groupTimetables.forEach(gt -> forms.add(gt.getForm()));
        return forms;
    }

    @Operation(summary = "Показывает все группы по конкретной форме обучения")
    @GetMapping("/groupTimetables/forms/{form}")
    public List<GroupTimetable> getTimetablesByForm(
            @Parameter(description = "Форма обучения",
                    required = true,
                    schema = @Schema(implementation = String.class)) @PathVariable String form
    ) {
        return groupTimetableRepository.findAllByForm(form);
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
