package com.codeclash.problemservice.question.model.tag;

import com.codeclash.problemservice.question.model.exception.NullTagsException;
import java.util.List;
import java.util.UUID;

public record Tags(List<TagId> ids) {

  public Tags {
    ids = ids.stream().distinct().toList();
  }

  public static Tags of(List<UUID> ids) {
    if (ids == null) throw new NullTagsException();
    return new Tags(ids.stream().map(TagId::of).toList());
  }

  public static Tags unsafe(List<UUID> ids) {
    return new Tags(ids.stream().map(TagId::unsafe).toList());
  }
}
