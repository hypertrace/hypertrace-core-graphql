package org.hypertrace.core.graphql.utils.export.span;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.Accessors;
import org.apache.commons.text.StringEscapeUtils;

@lombok.Value
@Accessors(fluent = true)
public class ExportSpanResponse {

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private static class MessageSerializer extends JsonSerializer<Message> {
    private static final JsonFormat.Printer PRINTER =
        JsonFormat.printer().omittingInsignificantWhitespace();

    @Override
    public void serialize(Message message, JsonGenerator generator, SerializerProvider serializers)
        throws IOException {
      generator.writeRawValue(PRINTER.print(message));
    }
  }

  @JsonSerialize(contentUsing = MessageSerializer.class)
  List<ResourceSpans> resourceSpans;

  public String toJson() throws JsonProcessingException {
    return StringEscapeUtils.unescapeJson(OBJECT_MAPPER.writeValueAsString(this));
  }

  public static class Builder {
    private List<ExportSpan> exportSpans;

    public Builder(List<ExportSpan> exportSpans) {
      this.exportSpans = exportSpans;
    }

    public ExportSpanResponse build() {
      List<ResourceSpans> spans =
          this.exportSpans.stream().map(s -> s.resourceSpans()).collect(Collectors.toList());
      return new ExportSpanResponse(spans);
    }
  }
}
