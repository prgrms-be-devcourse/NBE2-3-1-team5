package com.example.coffee.model.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.type.Alias;

import java.time.LocalDateTime;


@Alias(value = "review")
@AllArgsConstructor
@Getter
@Setter
public class ReviewDTO {
    private Long reviewId;

    @Schema(description = "리뷰 내용", example = "이 커피 정말 맛있네요!")
    private String content;

    private Long memberId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
