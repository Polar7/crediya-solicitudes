package co.com.pragma.creditapplication.model.page;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Page<T> {

    private List<T> content;

    private long totalElements;

}
