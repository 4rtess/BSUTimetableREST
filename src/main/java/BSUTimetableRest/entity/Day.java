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
public class Day {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String dayName;

    @OneToMany(targetEntity = Lesson.class, cascade = CascadeType.ALL)
    private List<Lesson> lessons;


}
