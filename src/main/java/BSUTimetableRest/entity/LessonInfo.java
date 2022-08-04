package BSUTimetableRest.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class LessonInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String lessonName;
    private String lessonType;
    private String lessonPeriod;
    private String lessonLecturer;
    private String lessonPlace;
}
