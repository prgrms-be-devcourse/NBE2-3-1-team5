package com.example.coffee.model.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.ibatis.type.Alias;


//@Alias(value = "review")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReviewRequestDTO {
    @Schema(description = "productId", example = "1")
    private Long productId;

    @Schema(description = "리뷰 내용", example = "이 커피 정말 맛있네요!")
    private String content;
}