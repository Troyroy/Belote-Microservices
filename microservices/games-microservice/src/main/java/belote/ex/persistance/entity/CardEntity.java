package belote.ex.persistance.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class CardEntity {
    private Integer id;
    private Integer points;
    private String suit;
    private String rank;
}
