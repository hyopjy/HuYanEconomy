package cn.chahuyun.economy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuffProperty<T> {
    private String propertyKey;
    private T propertyValue;
}
