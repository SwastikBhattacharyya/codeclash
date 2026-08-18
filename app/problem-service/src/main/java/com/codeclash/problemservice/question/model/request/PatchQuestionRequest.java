package com.codeclash.problemservice.question.model.request;

import com.codeclash.common.patch.PatchObject;
import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.Id;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.category.Categories;
import com.codeclash.problemservice.question.model.tag.Tags;

public record PatchQuestionRequest(
    Id id,
    PatchObject<Title> title,
    PatchObject<Summary> summary,
    PatchObject<Description> description,
    PatchObject<Visible> visible,
    PatchObject<MaxRunTime> maxRunTime,
    PatchObject<MaxRamLimit> maxRamLimit,
    PatchObject<Categories> categories,
    PatchObject<Tags> tags) {}
