package com.codeclash.problemservice.question.model.request;

import com.codeclash.problemservice.question.model.Description;
import com.codeclash.problemservice.question.model.MaxRamLimit;
import com.codeclash.problemservice.question.model.MaxRunTime;
import com.codeclash.problemservice.question.model.Summary;
import com.codeclash.problemservice.question.model.Title;
import com.codeclash.problemservice.question.model.Visible;
import com.codeclash.problemservice.question.model.category.Categories;
import com.codeclash.problemservice.question.model.tag.Tags;

public record CreateQuestionRequest(
    Title title,
    Summary summary,
    Description description,
    Visible visible,
    MaxRunTime maxRunTime,
    MaxRamLimit maxRamLimit,
    Categories categories,
    Tags tags) {}
