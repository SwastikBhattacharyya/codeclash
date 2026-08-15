package com.codeclash.problemservice.tag.model.request;

import com.codeclash.problemservice.tag.model.Description;
import com.codeclash.problemservice.tag.model.Name;
import lombok.Builder;
import lombok.With;

@Builder
@With
public record CreateTagRequest(Name name, Description description) {}
