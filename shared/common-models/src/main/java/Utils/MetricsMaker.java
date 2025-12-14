package Utils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MetricsMaker {

    public static void MetricsCounter(String name, MeterRegistry registry){
        registry.counter(name).increment();
    }

    public static void MetricsCounterWithTag(String name, String key, String value, String description, MeterRegistry registry){
        registry.counter(name, key, StringUtils.isEmpty(value) ? "all" : value).increment();
    }
}
