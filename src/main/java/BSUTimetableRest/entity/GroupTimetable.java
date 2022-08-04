package BSUTimetableRest.entity;

import lombok.*;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.*;
import java.util.List;

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

    @OneToMany(targetEntity = Day.class, cascade = CascadeType.ALL)
    private List<Day> days;


}
