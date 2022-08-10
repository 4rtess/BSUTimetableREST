package BSUTimetableRest.entity;

import lombok.*;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GroupTimetable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;


    private String groupName;
    private String directionOfEducation;
    private String speciality;
    private String form;
    private Integer course;
    private Integer groupNumber;

    @OneToMany(targetEntity = Day.class, cascade = CascadeType.ALL)
    private List<Day> days;


    public GroupTimetable(String groupName, List<Day> days, Map<String, String> abbreviation) {
        this.groupName=groupName;
        this.days=days;
        //Спарсить word и через MAP сделать поиск
        this.directionOfEducation= abbreviation.get(groupName.substring(0, groupName.indexOf("(")).toUpperCase());
        this.speciality = abbreviation.get(getTextByRegex(groupName,"\\([а-яА-Я]{1,4}\\)").toUpperCase()
                .replace("(","").replace(")","").toUpperCase());
        this.form = abbreviation.get(groupName.substring(groupName.indexOf(")")+1, groupName.indexOf("-")).toUpperCase());
        //

        this.course = Integer.parseInt(groupName.substring(groupName.indexOf("-")+1, groupName.indexOf(".")));
        this.groupNumber = Integer.parseInt(groupName.substring(groupName.indexOf(".")+1));
    }

    public String getTextByRegex(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find())
            return text.substring(matcher.start(), matcher.end());
        return text;
    }

}
