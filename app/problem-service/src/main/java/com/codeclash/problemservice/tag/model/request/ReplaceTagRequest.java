package com.codeclash.problemservice.tag.model.request;

import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Id;
import com.codeclash.problemservice.tag.model.Name;
import lombok.Builder;

@Builder
public record ReplaceTagRequest(Id id, Name name, Description description) {}
