package BSUTimetableRest.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String lessonTime;
    private String lessonNumber;

    @OneToMany(targetEntity = LessonInfo.class, cascade = CascadeType.ALL)
    private List<LessonInfo> lessonInfo;


}
